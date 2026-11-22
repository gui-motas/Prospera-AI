package br.ucsal.gestaofinanceira.planejamento;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.categoria.CategoriaService;
import br.ucsal.gestaofinanceira.comum.RecursoNaoEncontradoException;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.financeiro.ServicoFinanceiro;
import br.ucsal.gestaofinanceira.meta.MetaFinanceira;
import br.ucsal.gestaofinanceira.meta.MetaFinanceiraRepository;
import br.ucsal.gestaofinanceira.meta.MetaFinanceiraService;
import br.ucsal.gestaofinanceira.meta.StatusMeta;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.AnaliseResponse;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.CenarioRequest;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.CenarioResponse;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.DiagnosticoResponse;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.StatusIA;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.ViabilidadeMeta;
import br.ucsal.gestaofinanceira.seguranca.UsuarioAutenticado;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orquestra o módulo de Planejamento Inteligente de Metas (RF-010), seguindo o fluxo do BPMN:
 * calcular saldo e progresso (RN-004, RN-012) → padronizar dados (RN-009) → enviar à API de IA →
 * exibir análise em linguagem natural (RN-010) → o usuário decide se ajusta a meta (RN-011).
 *
 * <p>Os métodos que chamam a IA não abrem transação, para não manter conexão com o banco
 * aberta durante a chamada externa.</p>
 */
@Service
public class PlanejamentoService {

    private final ServicoIA servicoIA;
    private final ServicoFinanceiro servicoFinanceiro;
    private final MetaFinanceiraService metaService;
    private final MetaFinanceiraRepository metaRepository;
    private final CenarioFinanceiroRepository cenarioRepository;
    private final AnaliseFinanceiraRepository analiseRepository;
    private final CategoriaService categoriaService;
    private final UsuarioAutenticado usuarioAutenticado;

    public PlanejamentoService(ServicoIA servicoIA,
                               ServicoFinanceiro servicoFinanceiro,
                               MetaFinanceiraService metaService,
                               MetaFinanceiraRepository metaRepository,
                               CenarioFinanceiroRepository cenarioRepository,
                               AnaliseFinanceiraRepository analiseRepository,
                               CategoriaService categoriaService,
                               UsuarioAutenticado usuarioAutenticado) {
        this.servicoIA = servicoIA;
        this.servicoFinanceiro = servicoFinanceiro;
        this.metaService = metaService;
        this.metaRepository = metaRepository;
        this.cenarioRepository = cenarioRepository;
        this.analiseRepository = analiseRepository;
        this.categoriaService = categoriaService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    public StatusIA status() {
        return new StatusIA(servicoIA.estaDisponivel(), ServicoIA.AVISO);
    }

    /** Viabilidade de cada meta com a capacidade de economia atual — sem depender da IA. */
    @Transactional(readOnly = true)
    public DiagnosticoResponse diagnosticar() {
        UUID usuarioId = usuarioAutenticado.obterId();
        LocalDate hoje = LocalDate.now();
        BigDecimal capacidade = servicoFinanceiro.calcularCapacidadeMensalEconomia(usuarioId, hoje);

        List<ViabilidadeMeta> metas = new ArrayList<>();
        for (MetaFinanceira meta : metaRepository.findByUsuarioIdOrderByPrazoAsc(usuarioId)) {
            StatusMeta status = meta.statusEm(hoje);
            BigDecimal necessario = meta.calcularValorMensalNecessario(hoje);
            boolean concluida = status == StatusMeta.CONCLUIDA;
            Long meses = null;
            LocalDate projetado = null;
            if (!concluida && capacidade.signum() > 0) {
                meses = CenarioFinanceiro.calcularMesesNecessarios(meta.calcularValorRestante(), capacidade);
                projetado = hoje.plusMonths(meses);
            }
            boolean viavel = concluida
                    || (status == StatusMeta.EM_ANDAMENTO && capacidade.compareTo(necessario) >= 0);
            metas.add(new ViabilidadeMeta(meta.getId(), meta.getNome(), status, meta.getPrazo(),
                    meta.calcularValorRestante(), necessario, meta.calcularMesesRestantes(hoje),
                    viavel, meses, projetado));
        }
        return new DiagnosticoResponse(hoje, servicoFinanceiro.calcularSaldo(usuarioId), capacidade,
                ServicoFinanceiro.MESES_BASE_CAPACIDADE, metas);
    }

    // ---------------- Análises com IA (RF-010) ----------------

    public AnaliseResponse analisarSituacao() {
        Usuario usuario = usuarioAutenticado.obter();
        ResultadoAnalise resultado = servicoIA.gerarAnalise(usuario);
        return AnaliseResponse.de(resultado.analise(), resultado.dadosEnviados());
    }

    public AnaliseResponse analisarMeta(UUID metaId) {
        Usuario usuario = usuarioAutenticado.obter();
        // Não abre transação: a meta é lida e enviada à IA sem gravar nada (RN-011)
        MetaFinanceira meta = metaService.buscarDoUsuario(metaId, usuario.getId());
        ResultadoAnalise resultado = servicoIA.planejarMeta(usuario, meta);
        return AnaliseResponse.de(resultado.analise(), resultado.dadosEnviados());
    }

    public AnaliseResponse interpretarCenario(UUID cenarioId) {
        Usuario usuario = usuarioAutenticado.obter();
        CenarioFinanceiro cenario = buscarCenario(cenarioId, usuario.getId());
        ResultadoAnalise resultado = servicoIA.interpretarCenario(usuario, cenario);
        return AnaliseResponse.de(resultado.analise(), resultado.dadosEnviados());
    }

    @Transactional(readOnly = true)
    public List<AnaliseResponse> historico() {
        return analiseRepository.findTop10ByUsuarioIdOrderByDataGeracaoDesc(usuarioAutenticado.obterId()).stream()
                .map(a -> AnaliseResponse.de(a, null))
                .toList();
    }

    // ---------------- Simulação de cenários (determinística) ----------------

    @Transactional
    public CenarioResponse simularCenario(UUID metaId, CenarioRequest req) {
        UUID usuarioId = usuarioAutenticado.obterId();
        LocalDate hoje = LocalDate.now();
        MetaFinanceira meta = metaService.buscarDoUsuario(metaId, usuarioId);
        if (meta.statusEm(hoje) == StatusMeta.CONCLUIDA) {
            throw new RegraNegocioException("Esta meta já foi concluída.");
        }

        BigDecimal reducao = BigDecimal.ZERO;
        if (req.categoriaReducaoId() != null && req.percentualReducao() != null) {
            Categoria categoria = categoriaService.buscarDoUsuario(req.categoriaReducaoId(), usuarioId);
            if (categoria.getTipo() != TipoMovimentacao.DESPESA) {
                throw new RegraNegocioException("A redução de gastos deve ser aplicada a uma categoria de despesa.");
            }
            BigDecimal mediaMensal = servicoFinanceiro.calcularMediaMensalCategoria(usuarioId, categoria.getId(), hoje);
            reducao = mediaMensal.multiply(BigDecimal.valueOf(req.percentualReducao()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal economiaSimulada = req.economiaMensal().add(reducao);
        CenarioFinanceiro cenario = cenarioRepository.save(
                new CenarioFinanceiro(meta, req.descricao(), economiaSimulada, hoje));
        BigDecimal capacidade = servicoFinanceiro.calcularCapacidadeMensalEconomia(usuarioId, hoje);
        return paraResposta(cenario, reducao, capacidade, hoje);
    }

    @Transactional(readOnly = true)
    public List<CenarioResponse> listarCenarios(UUID metaId) {
        UUID usuarioId = usuarioAutenticado.obterId();
        LocalDate hoje = LocalDate.now();
        MetaFinanceira meta = metaService.buscarDoUsuario(metaId, usuarioId);
        BigDecimal capacidade = servicoFinanceiro.calcularCapacidadeMensalEconomia(usuarioId, hoje);
        return cenarioRepository.findByMetaIdOrderByCriadoEmDesc(meta.getId()).stream()
                .map(c -> paraResposta(c, null, capacidade, hoje))
                .toList();
    }

    @Transactional
    public void excluirCenario(UUID cenarioId) {
        CenarioFinanceiro cenario = buscarCenario(cenarioId, usuarioAutenticado.obterId());
        analiseRepository.deleteByCenarioId(cenario.getId());
        cenarioRepository.delete(cenario);
    }

    private CenarioFinanceiro buscarCenario(UUID cenarioId, UUID usuarioId) {
        return cenarioRepository.findByIdAndMetaUsuarioId(cenarioId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cenário não encontrado."));
    }

    private CenarioResponse paraResposta(CenarioFinanceiro c, BigDecimal reducao, BigDecimal capacidade, LocalDate hoje) {
        return new CenarioResponse(c.getId(), c.getMeta().getId(), c.getMeta().getNome(), c.getDescricao(),
                c.getEconomiaMensalSimulada(), reducao, c.getPrazoProjetado(), c.getCriadoEm(),
                c.compararComSituacaoAtual(capacidade, hoje));
    }
}
