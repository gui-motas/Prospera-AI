package br.ucsal.gestaofinanceira.planejamento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado de CenarioFinanceiro.compararComSituacaoAtual(): confronta o cenário hipotético
 * com o ritmo atual de economia do usuário. Valores calculados pela plataforma (RN-012).
 *
 * @param mesesSituacaoAtual   meses para concluir a meta mantendo a economia atual (null se a economia atual for zero)
 * @param mesesAntecipados     quantos meses o cenário antecipa (negativo = atrasa) em relação à situação atual
 */
public record ComparacaoCenario(
        LocalDate prazoMeta,
        BigDecimal valorRestante,
        BigDecimal valorMensalNecessario,
        BigDecimal economiaMensalAtual,
        Long mesesSituacaoAtual,
        LocalDate prazoProjetadoAtual,
        boolean situacaoAtualCumprePrazo,
        BigDecimal economiaMensalSimulada,
        long mesesCenario,
        LocalDate prazoProjetadoCenario,
        boolean cenarioCumprePrazo,
        Long mesesAntecipados,
        BigDecimal economiaAdicional) {
}
