package br.ucsal.gestaofinanceira.conta;

import br.ucsal.gestaofinanceira.comum.RecursoNaoEncontradoException;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.conta.ContaDtos.ContaRequest;
import br.ucsal.gestaofinanceira.conta.ContaDtos.ContaResponse;
import br.ucsal.gestaofinanceira.financeiro.ServicoFinanceiro;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoRepository;
import br.ucsal.gestaofinanceira.seguranca.UsuarioAutenticado;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** RF-003 – Gerenciamento de contas financeiras (cadastrar, consultar, editar, excluir). */
@Service
public class ContaFinanceiraService {

    private final ContaFinanceiraRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ServicoFinanceiro servicoFinanceiro;
    private final UsuarioAutenticado usuarioAutenticado;

    public ContaFinanceiraService(ContaFinanceiraRepository contaRepository,
                                  MovimentacaoRepository movimentacaoRepository,
                                  ServicoFinanceiro servicoFinanceiro,
                                  UsuarioAutenticado usuarioAutenticado) {
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.servicoFinanceiro = servicoFinanceiro;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Transactional(readOnly = true)
    public List<ContaResponse> listar() {
        UUID usuarioId = usuarioAutenticado.obterId();
        Map<UUID, BigDecimal> resultados = servicoFinanceiro.calcularResultadoPorConta(usuarioId);
        Map<UUID, Long> quantidades = contarMovimentacoesPorConta(usuarioId);
        return contaRepository.findByUsuarioIdOrderByNomeAsc(usuarioId).stream()
                .map(c -> paraResposta(c, resultados.get(c.getId()), quantidades.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    @Transactional
    public ContaResponse criar(ContaRequest req) {
        Usuario usuario = usuarioAutenticado.obter();
        if (contaRepository.existsByUsuarioIdAndNomeIgnoreCase(usuario.getId(), req.nome().trim())) {
            throw new RegraNegocioException("Você já tem uma conta com esse nome.");
        }
        try {
            ContaFinanceira conta = contaRepository.save(
                    new ContaFinanceira(usuario, req.nome(), req.tipo(), req.saldoInicial()));
            return paraResposta(conta, BigDecimal.ZERO, 0L);
        } catch (DataIntegrityViolationException e) {
            // duas requisições iguais ao mesmo tempo: a constraint do banco decide
            throw new RegraNegocioException("Você já tem uma conta com esse nome.");
        }
    }

    @Transactional
    public ContaResponse editar(UUID id, ContaRequest req) {
        UUID usuarioId = usuarioAutenticado.obterId();
        ContaFinanceira conta = buscarDoUsuario(id, usuarioId);
        if (!conta.getNome().equalsIgnoreCase(req.nome().trim())
                && contaRepository.existsByUsuarioIdAndNomeIgnoreCase(usuarioId, req.nome().trim())) {
            throw new RegraNegocioException("Você já tem uma conta com esse nome.");
        }
        conta.editar(req.nome(), req.tipo(), req.saldoInicial());
        return paraResposta(conta, servicoFinanceiro.calcularResultadoPorConta(usuarioId).get(conta.getId()),
                movimentacaoRepository.countByContaId(conta.getId()));
    }

    /** Exclui a conta e as movimentações registradas nela (o usuário confirma antes na interface). */
    @Transactional
    public void excluir(UUID id) {
        ContaFinanceira conta = buscarDoUsuario(id, usuarioAutenticado.obterId());
        movimentacaoRepository.deleteByContaId(conta.getId());
        contaRepository.delete(conta);
    }

    /** Busca garantindo a propriedade dos dados (RN-001). */
    public ContaFinanceira buscarDoUsuario(UUID id, UUID usuarioId) {
        return contaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));
    }

    private ContaResponse paraResposta(ContaFinanceira conta, BigDecimal resultadoMovimentacoes, long quantidade) {
        return new ContaResponse(conta.getId(), conta.getNome(), conta.getTipo(), conta.getSaldoInicial(),
                conta.calcularSaldo(resultadoMovimentacoes), quantidade);
    }

    /** Contagem por conta em uma única consulta (antes era um count por conta). */
    private Map<UUID, Long> contarMovimentacoesPorConta(UUID usuarioId) {
        Map<UUID, Long> quantidades = new HashMap<>();
        for (Object[] linha : movimentacaoRepository.contarPorConta(usuarioId)) {
            quantidades.put((UUID) linha[0], (Long) linha[1]);
        }
        return quantidades;
    }
}
