package br.ucsal.gestaofinanceira.orcamento;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.categoria.CategoriaService;
import br.ucsal.gestaofinanceira.comum.RecursoNaoEncontradoException;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoRepository;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import br.ucsal.gestaofinanceira.orcamento.OrcamentoDtos.OrcamentoRequest;
import br.ucsal.gestaofinanceira.orcamento.OrcamentoDtos.OrcamentoResponse;
import br.ucsal.gestaofinanceira.seguranca.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Definição de limites de gastos e acompanhamento do orçamento pessoal (escopo 1.2, RN-005). */
@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final CategoriaService categoriaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public OrcamentoService(OrcamentoRepository orcamentoRepository,
                            MovimentacaoRepository movimentacaoRepository,
                            CategoriaService categoriaService,
                            UsuarioAutenticado usuarioAutenticado) {
        this.orcamentoRepository = orcamentoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.categoriaService = categoriaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listar() {
        return orcamentoRepository.findByCategoriaUsuarioIdOrderByInicioPeriodoDesc(usuarioAutenticado.obterId())
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest req) {
        Categoria categoria = categoriaService.buscarDoUsuario(req.categoriaId(), usuarioAutenticado.obterId());
        Orcamento orcamento = new Orcamento(categoria, req.limite(), req.inicioPeriodo(), req.fimPeriodo());
        verificarSobreposicao(orcamento, null);
        return paraResposta(orcamentoRepository.save(orcamento));
    }

    @Transactional
    public OrcamentoResponse editar(UUID id, OrcamentoRequest req) {
        UUID usuarioId = usuarioAutenticado.obterId();
        Orcamento orcamento = buscarDoUsuario(id, usuarioId);
        Categoria categoria = categoriaService.buscarDoUsuario(req.categoriaId(), usuarioId);
        orcamento.editar(categoria, req.limite(), req.inicioPeriodo(), req.fimPeriodo());
        verificarSobreposicao(orcamento, id);
        return paraResposta(orcamento);
    }

    @Transactional
    public void excluir(UUID id) {
        orcamentoRepository.delete(buscarDoUsuario(id, usuarioAutenticado.obterId()));
    }

    /** Orcamento.calcularTotalGasto(): despesas da categoria dentro do período do limite. */
    public BigDecimal calcularTotalGasto(Orcamento orcamento) {
        BigDecimal total = movimentacaoRepository.somarPorCategoriaNoPeriodo(
                orcamento.getCategoria().getId(), TipoMovimentacao.DESPESA,
                orcamento.getInicioPeriodo(), orcamento.getFimPeriodo());
        return total == null ? BigDecimal.ZERO : total;
    }

    private void verificarSobreposicao(Orcamento orcamento, UUID idAtual) {
        boolean sobrepoe = orcamentoRepository.findByCategoriaId(orcamento.getCategoria().getId()).stream()
                .filter(o -> idAtual == null || !o.getId().equals(idAtual))
                .anyMatch(o -> o.sobrepoe(orcamento.getInicioPeriodo(), orcamento.getFimPeriodo()));
        if (sobrepoe) {
            throw new RegraNegocioException("Já existe um limite para essa categoria em um período que se sobrepõe a este.");
        }
    }

    private Orcamento buscarDoUsuario(UUID id, UUID usuarioId) {
        return orcamentoRepository.findByIdAndCategoriaUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Limite de gastos não encontrado."));
    }

    private OrcamentoResponse paraResposta(Orcamento o) {
        BigDecimal totalGasto = calcularTotalGasto(o);
        return new OrcamentoResponse(o.getId(), o.getCategoria().getId(), o.getCategoria().getNome(), o.getLimite(),
                o.getInicioPeriodo(), o.getFimPeriodo(), totalGasto, o.calcularSaldoRestante(totalGasto),
                o.calcularPercentualUtilizado(totalGasto), o.avaliarSituacao(totalGasto));
    }
}
