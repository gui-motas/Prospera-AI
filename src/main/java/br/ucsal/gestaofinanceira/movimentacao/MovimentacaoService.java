package br.ucsal.gestaofinanceira.movimentacao;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.categoria.CategoriaService;
import br.ucsal.gestaofinanceira.comum.RecursoNaoEncontradoException;
import br.ucsal.gestaofinanceira.conta.ContaFinanceira;
import br.ucsal.gestaofinanceira.conta.ContaFinanceiraService;
import br.ucsal.gestaofinanceira.financeiro.ServicoFinanceiro;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoDtos.MovimentacaoRequest;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoDtos.MovimentacaoResponse;
import br.ucsal.gestaofinanceira.seguranca.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * RF-005 – Registro de despesas (e receitas) e
 * RF-006 – Consulta, edição e exclusão de movimentações.
 */
@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ContaFinanceiraService contaService;
    private final CategoriaService categoriaService;
    private final ServicoFinanceiro servicoFinanceiro;
    private final UsuarioAutenticado usuarioAutenticado;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository,
                               ContaFinanceiraService contaService,
                               CategoriaService categoriaService,
                               ServicoFinanceiro servicoFinanceiro,
                               UsuarioAutenticado usuarioAutenticado) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.contaService = contaService;
        this.categoriaService = categoriaService;
        this.servicoFinanceiro = servicoFinanceiro;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponse> listar(FiltroMovimentacao filtros) {
        return servicoFinanceiro.filtrarMovimentacoes(usuarioAutenticado.obterId(), filtros).stream()
                .map(MovimentacaoResponse::de)
                .toList();
    }

    @Transactional
    public MovimentacaoResponse registrar(MovimentacaoRequest req) {
        UUID usuarioId = usuarioAutenticado.obterId();
        ContaFinanceira conta = contaService.buscarDoUsuario(req.contaId(), usuarioId);
        Categoria categoria = categoriaService.buscarDoUsuario(req.categoriaId(), usuarioId);
        Movimentacao movimentacao = new Movimentacao(conta, categoria, req.tipo(), req.descricao(), req.valor(), req.data());
        return MovimentacaoResponse.de(movimentacaoRepository.save(movimentacao));
    }

    @Transactional
    public MovimentacaoResponse editar(UUID id, MovimentacaoRequest req) {
        UUID usuarioId = usuarioAutenticado.obterId();
        Movimentacao movimentacao = buscarDoUsuario(id, usuarioId);
        ContaFinanceira conta = contaService.buscarDoUsuario(req.contaId(), usuarioId);
        Categoria categoria = categoriaService.buscarDoUsuario(req.categoriaId(), usuarioId);
        movimentacao.editar(conta, categoria, req.tipo(), req.descricao(), req.valor(), req.data());
        return MovimentacaoResponse.de(movimentacao);
    }

    @Transactional
    public void excluir(UUID id) {
        movimentacaoRepository.delete(buscarDoUsuario(id, usuarioAutenticado.obterId()));
    }

    private Movimentacao buscarDoUsuario(UUID id, UUID usuarioId) {
        return movimentacaoRepository.findByIdAndContaUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimentação não encontrada."));
    }
}
