package br.ucsal.gestaofinanceira.financeiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Dados consolidados do painel financeiro (RF-007). Todos os números são calculados
 * pela própria plataforma, de forma determinística (RN-012).
 */
public record ResumoFinanceiro(
        LocalDate inicio,
        LocalDate fim,
        BigDecimal saldoTotal,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal resultadoPeriodo,
        BigDecimal capacidadeMensalEconomia,
        List<GastoCategoria> gastosPorCategoria,
        List<ResumoMensal> evolucaoMensal) {

    /** Distribuição dos gastos por categoria, com percentual sobre o total de despesas. */
    public record GastoCategoria(UUID categoriaId, String categoria, BigDecimal total, BigDecimal percentual) {
    }

    /** Receitas e despesas de um mês (formato do mês: AAAA-MM). */
    public record ResumoMensal(String mes, BigDecimal receitas, BigDecimal despesas, BigDecimal resultado) {
    }
}
