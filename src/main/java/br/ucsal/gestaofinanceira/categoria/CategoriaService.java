package br.ucsal.gestaofinanceira.categoria;

import br.ucsal.gestaofinanceira.categoria.CategoriaDtos.CategoriaRequest;
import br.ucsal.gestaofinanceira.categoria.CategoriaDtos.CategoriaResponse;
import br.ucsal.gestaofinanceira.comum.RecursoNaoEncontradoException;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoRepository;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import br.ucsal.gestaofinanceira.orcamento.OrcamentoRepository;
import br.ucsal.gestaofinanceira.seguranca.UsuarioAutenticado;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** RF-004 – Gerenciamento de categorias (criar, consultar, editar, excluir), respeitando a RN-003. */
@Service
public class CategoriaService {

    private static final List<String> DESPESAS_PADRAO = List.of(
            "Alimentação", "Moradia", "Transporte", "Saúde", "Educação",
            "Lazer", "Vestuário", "Contas e serviços", "Outras despesas");

    private static final List<String> RECEITAS_PADRAO = List.of(
            "Salário", "Renda extra", "Rendimentos", "Outras receitas");

    private final CategoriaRepository categoriaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final UsuarioAutenticado usuarioAutenticado;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            MovimentacaoRepository movimentacaoRepository,
                            OrcamentoRepository orcamentoRepository,
                            UsuarioAutenticado usuarioAutenticado) {
        this.categoriaRepository = categoriaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findByUsuarioIdOrderByTipoAscNomeAsc(usuarioAutenticado.obterId()).stream()
                .map(CategoriaResponse::de)
                .toList();
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest req) {
        Usuario usuario = usuarioAutenticado.obter();
        if (categoriaRepository.existsByUsuarioIdAndNomeIgnoreCaseAndTipo(usuario.getId(), req.nome().trim(), req.tipo())) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome e tipo.");
        }
        try {
            return CategoriaResponse.de(categoriaRepository.save(new Categoria(usuario, req.nome(), req.tipo())));
        } catch (DataIntegrityViolationException e) {
            // duas requisições iguais ao mesmo tempo: a constraint do banco decide, com a mesma mensagem
            throw new RegraNegocioException("Já existe uma categoria com esse nome e tipo.");
        }
    }

    @Transactional
    public CategoriaResponse editar(UUID id, CategoriaRequest req) {
        UUID usuarioId = usuarioAutenticado.obterId();
        Categoria categoria = buscarDoUsuario(id, usuarioId);

        if (categoriaRepository.existsByUsuarioIdAndNomeIgnoreCaseAndTipoAndIdNot(usuarioId, req.nome().trim(), req.tipo(), id)) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome e tipo.");
        }
        if (categoria.getTipo() != req.tipo()) {
            // RN-003: a classificação precisa continuar compatível com o que já está vinculado
            if (movimentacaoRepository.existsByCategoriaIdAndTipoNot(id, req.tipo())) {
                throw new RegraNegocioException(
                        "Não é possível mudar o tipo: a categoria já possui movimentações do tipo atual.");
            }
            if (req.tipo() == TipoMovimentacao.RECEITA && orcamentoRepository.existsByCategoriaId(id)) {
                throw new RegraNegocioException(
                        "Não é possível mudar para receita: a categoria possui limites de gastos definidos.");
            }
        }
        categoria.editar(req.nome(), req.tipo());
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluir(UUID id) {
        Categoria categoria = buscarDoUsuario(id, usuarioAutenticado.obterId());
        if (movimentacaoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException(
                    "Esta categoria possui movimentações. Reclassifique-as antes de excluir a categoria.");
        }
        if (orcamentoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException(
                    "Esta categoria possui limites de gastos. Exclua-os antes de excluir a categoria.");
        }
        categoriaRepository.delete(categoria);
    }

    /** Busca garantindo a propriedade dos dados (RN-001). */
    public Categoria buscarDoUsuario(UUID id, UUID usuarioId) {
        return categoriaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada."));
    }

    /** Categorias iniciais criadas no cadastro do usuário (podem ser editadas ou excluídas). */
    @Transactional
    public void criarCategoriasPadrao(Usuario usuario) {
        DESPESAS_PADRAO.forEach(nome -> categoriaRepository.save(new Categoria(usuario, nome, TipoMovimentacao.DESPESA)));
        RECEITAS_PADRAO.forEach(nome -> categoriaRepository.save(new Categoria(usuario, nome, TipoMovimentacao.RECEITA)));
    }
}
