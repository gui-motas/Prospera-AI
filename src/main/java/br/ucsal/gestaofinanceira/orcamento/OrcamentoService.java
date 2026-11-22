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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        UUID usuarioId = usuarioAutenticado.obterId();
        List<Orcamento> orcamentos = orcamentoRepository
                .findByCategoriaUsuarioIdOrderByInicioPeriodoDesc(usuarioId);
        if (orcamentos.isEmpty()) {
            return List.of();
        }

        // Uma consulta agrupada por categoria e mês cobre todos os orçamentos, em vez de
        // um "sum" por orçamento. Cada orçamento depois soma só os meses do seu período.
        LocalDate inicio = orcamentos.stream().map(Orcamento::getInicioPeriodo).min(LocalDate::compareTo).orElseThrow();
        LocalDate fim = orcamentos.stream().map(Orcamento::getFimPeriodo).max(LocalDate::compareTo).orElseThrow();
        Map<UUID, Map<YearMonth, BigDecimal>> gastoPorCategoriaMes = new HashMap<>();
        for (Object[] linha : movimentacaoRepository.somarPorCategoriaEMes(
                usuarioId, TipoMovimentacao.DESPESA, inicio, fim)) {
            YearMonth mes = YearMonth.of((Integer) linha[1], (Integer) linha[2]);
            gastoPorCategoriaMes
                    .computeIfAbsent((UUID) linha[0], k -> new HashMap<>())
                    .merge(mes, (BigDecimal) linha[3], BigDecimal::add);
        }

        return orcamentos.stream()
                .map(o -> {
                    BigDecimal total = totalEmMesesCheios(gastoPorCategoriaMes, o);
                    // períodos parciais (ex.: 10/01 a 20/01) precisam da consulta exata
                    return paraResposta(o, total != null ? total : calcularTotalGasto(o));
                })
                .toList();
    }

    /** Soma os meses que caem dentro do período do orçamento; null quando o período não é composto de meses cheios. */
    private static BigDecimal totalEmMesesCheios(Map<UUID, Map<YearMonth, BigDecimal>> gastos, Orcamento orcamento) {
        boolean comecaNoPrimeiroDia = orcamento.getInicioPeriodo()
                .equals(orcamento.getInicioPeriodo().withDayOfMonth(1));
        boolean terminaNoUltimoDia = orcamento.getFimPeriodo()
                .equals(YearMonth.from(orcamento.getFimPeriodo()).atEndOfMonth());
        if (!comecaNoPrimeiroDia || !terminaNoUltimoDia) {
            return null;
        }
        Map<YearMonth, BigDecimal> porMes = gastos.get(orcamento.getCategoria().getId());
        if (porMes == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        YearMonth atual = YearMonth.from(orcamento.getInicioPeriodo());
        YearMonth ultimo = YearMonth.from(orcamento.getFimPeriodo());
        while (!atual.isAfter(ultimo)) {
            total = total.add(porMes.getOrDefault(atual, BigDecimal.ZERO));
            atual = atual.plusMonths(1);
        }
        return total;
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
        return paraResposta(o, calcularTotalGasto(o));
    }

    private OrcamentoResponse paraResposta(Orcamento o, BigDecimal totalGasto) {
        return new OrcamentoResponse(o.getId(), o.getCategoria().getId(), o.getCategoria().getNome(), o.getLimite(),
                o.getInicioPeriodo(), o.getFimPeriodo(), totalGasto, o.calcularSaldoRestante(totalGasto),
                o.calcularPercentualUtilizado(totalGasto), o.avaliarSituacao(totalGasto));
    }
}
