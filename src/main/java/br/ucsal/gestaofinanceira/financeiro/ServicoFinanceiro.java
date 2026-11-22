package br.ucsal.gestaofinanceira.financeiro;

import br.ucsal.gestaofinanceira.conta.ContaFinanceira;
import br.ucsal.gestaofinanceira.conta.ContaFinanceiraRepository;
import br.ucsal.gestaofinanceira.financeiro.ResumoFinanceiro.GastoCategoria;
import br.ucsal.gestaofinanceira.financeiro.ResumoFinanceiro.ResumoMensal;
import br.ucsal.gestaofinanceira.movimentacao.FiltroMovimentacao;
import br.ucsal.gestaofinanceira.movimentacao.Movimentacao;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoEspecificacoes;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoRepository;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço {@code ServicoFinanceiro} do diagrama de classes.
 *
 * <p>Concentra os cálculos determinísticos da plataforma: saldo, totais, distribuição de
 * gastos e capacidade de economia. Esses valores são a fonte oficial dos números exibidos
 * ao usuário e enviados, já consolidados, à API de IA (RN-004, RN-012).</p>
 */
@Service
@Transactional(readOnly = true)
public class ServicoFinanceiro {

    /** Quantidade de meses completos usada para estimar a capacidade mensal de economia. */
    public static final int MESES_BASE_CAPACIDADE = 3;

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final ContaFinanceiraRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public ServicoFinanceiro(ContaFinanceiraRepository contaRepository,
                             MovimentacaoRepository movimentacaoRepository) {
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    /** Receitas − despesas de cada conta do usuário (chave: id da conta). */
    public Map<UUID, BigDecimal> calcularResultadoPorConta(UUID usuarioId) {
        Map<UUID, BigDecimal> resultado = new HashMap<>();
        for (Object[] linha : movimentacaoRepository.somarPorContaETipo(usuarioId)) {
            UUID contaId = (UUID) linha[0];
            TipoMovimentacao tipo = (TipoMovimentacao) linha[1];
            BigDecimal soma = (BigDecimal) linha[2];
            BigDecimal comSinal = tipo == TipoMovimentacao.RECEITA ? soma : soma.negate();
            resultado.merge(contaId, comSinal, BigDecimal::add);
        }
        return resultado;
    }

    /** RN-004: saldo total = soma dos saldos de todas as contas do usuário. */
    public BigDecimal calcularSaldo(UUID usuarioId) {
        Map<UUID, BigDecimal> resultados = calcularResultadoPorConta(usuarioId);
        BigDecimal total = BigDecimal.ZERO;
        for (ContaFinanceira conta : contaRepository.findByUsuarioIdOrderByNomeAsc(usuarioId)) {
            total = total.add(conta.calcularSaldo(resultados.get(conta.getId())));
        }
        return total;
    }

    public BigDecimal totalizarReceitas(UUID usuarioId, Periodo periodo) {
        return zeroSeNulo(movimentacaoRepository.somarPorTipoNoPeriodo(
                usuarioId, TipoMovimentacao.RECEITA, periodo.inicio(), periodo.fim()));
    }

    public BigDecimal totalizarDespesas(UUID usuarioId, Periodo periodo) {
        return zeroSeNulo(movimentacaoRepository.somarPorTipoNoPeriodo(
                usuarioId, TipoMovimentacao.DESPESA, periodo.inicio(), periodo.fim()));
    }

    public List<GastoCategoria> agruparGastosPorCategoria(UUID usuarioId, Periodo periodo) {
        List<GastoPorCategoria> grupos = movimentacaoRepository.agruparPorCategoria(
                usuarioId, TipoMovimentacao.DESPESA, periodo.inicio(), periodo.fim());
        BigDecimal totalDespesas = grupos.stream()
                .map(GastoPorCategoria::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<GastoCategoria> resultado = new ArrayList<>();
        for (GastoPorCategoria g : grupos) {
            BigDecimal percentual = totalDespesas.signum() == 0
                    ? BigDecimal.ZERO
                    : g.total().multiply(CEM).divide(totalDespesas, 1, RoundingMode.HALF_UP);
            resultado.add(new GastoCategoria(g.categoriaId(), g.categoria(), g.total(), percentual));
        }
        return resultado;
    }

    /** Histórico filtrado por período, categoria, tipo, conta e texto (RF-006). */
    public List<Movimentacao> filtrarMovimentacoes(UUID usuarioId, FiltroMovimentacao filtros) {
        return movimentacaoRepository.findAll(
                MovimentacaoEspecificacoes.doUsuarioComFiltros(usuarioId, filtros),
                Sort.by(Sort.Order.desc("data"), Sort.Order.asc("descricao")));
    }

    /** Receitas e despesas dos últimos {@code quantidadeMeses} meses, terminando em {@code ultimoMes}. */
    public List<ResumoMensal> calcularEvolucaoMensal(UUID usuarioId, YearMonth ultimoMes, int quantidadeMeses) {
        YearMonth primeiroMes = ultimoMes.minusMonths(quantidadeMeses - 1L);
        Map<YearMonth, BigDecimal[]> porMes = new LinkedHashMap<>();
        for (int i = 0; i < quantidadeMeses; i++) {
            porMes.put(primeiroMes.plusMonths(i), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        List<LancamentoResumido> lancamentos = movimentacaoRepository.listarLancamentosResumidos(
                usuarioId, primeiroMes.atDay(1), ultimoMes.atEndOfMonth());
        for (LancamentoResumido l : lancamentos) {
            BigDecimal[] totais = porMes.get(YearMonth.from(l.data()));
            if (totais == null) {
                continue;
            }
            int indice = l.tipo() == TipoMovimentacao.RECEITA ? 0 : 1;
            totais[indice] = totais[indice].add(l.valor());
        }

        List<ResumoMensal> resultado = new ArrayList<>();
        porMes.forEach((mes, totais) ->
                resultado.add(new ResumoMensal(mes.toString(), totais[0], totais[1], totais[0].subtract(totais[1]))));
        return resultado;
    }

    /**
     * Capacidade mensal de economia: média de (receitas − despesas) dos últimos
     * {@value #MESES_BASE_CAPACIDADE} meses completos. Sem histórico nesses meses,
     * usa o resultado do mês corrente. Nunca é negativa.
     */
    public BigDecimal calcularCapacidadeMensalEconomia(UUID usuarioId, LocalDate hoje) {
        YearMonth mesAnterior = YearMonth.from(hoje).minusMonths(1);
        List<ResumoMensal> meses = calcularEvolucaoMensal(usuarioId, mesAnterior, MESES_BASE_CAPACIDADE);

        List<ResumoMensal> comMovimento = meses.stream()
                .filter(m -> m.receitas().signum() != 0 || m.despesas().signum() != 0)
                .toList();

        BigDecimal capacidade;
        if (comMovimento.isEmpty()) {
            Periodo mesAtual = Periodo.doMes(YearMonth.from(hoje));
            capacidade = totalizarReceitas(usuarioId, mesAtual).subtract(totalizarDespesas(usuarioId, mesAtual));
        } else {
            BigDecimal soma = comMovimento.stream()
                    .map(ResumoMensal::resultado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            capacidade = soma.divide(BigDecimal.valueOf(comMovimento.size()), 2, RoundingMode.HALF_UP);
        }
        return capacidade.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    /** Média mensal gasta em uma categoria nos últimos meses completos (usada na simulação de cenários). */
    public BigDecimal calcularMediaMensalCategoria(UUID usuarioId, UUID categoriaId, LocalDate hoje) {
        YearMonth ultimo = YearMonth.from(hoje).minusMonths(1);
        Periodo periodo = new Periodo(ultimo.minusMonths(MESES_BASE_CAPACIDADE - 1L).atDay(1), ultimo.atEndOfMonth());
        BigDecimal total = agruparGastosPorCategoria(usuarioId, periodo).stream()
                .filter(g -> g.categoriaId().equals(categoriaId))
                .map(GastoCategoria::total)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        return total.divide(BigDecimal.valueOf(MESES_BASE_CAPACIDADE), 2, RoundingMode.HALF_UP);
    }

    /** Consolida os dados do painel financeiro para o período informado (RF-007). */
    public ResumoFinanceiro consolidarDados(UUID usuarioId, Periodo periodo, LocalDate hoje) {
        BigDecimal receitas = totalizarReceitas(usuarioId, periodo);
        BigDecimal despesas = totalizarDespesas(usuarioId, periodo);
        return new ResumoFinanceiro(
                periodo.inicio(),
                periodo.fim(),
                calcularSaldo(usuarioId),
                receitas,
                despesas,
                receitas.subtract(despesas),
                calcularCapacidadeMensalEconomia(usuarioId, hoje),
                agruparGastosPorCategoria(usuarioId, periodo),
                calcularEvolucaoMensal(usuarioId, YearMonth.from(periodo.fim()), 6));
    }

    private static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
