package br.ucsal.gestaofinanceira.financeiro;

import br.ucsal.gestaofinanceira.comum.RegraNegocioException;

import java.time.LocalDate;
import java.time.YearMonth;

/** Intervalo de datas fechado [inicio, fim]. */
public record Periodo(LocalDate inicio, LocalDate fim) {

    public Periodo {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("Informe o início e o fim do período.");
        }
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data final do período não pode ser anterior à inicial.");
        }
    }

    public static Periodo doMes(YearMonth mes) {
        return new Periodo(mes.atDay(1), mes.atEndOfMonth());
    }

    /** Usa o mês corrente quando o início ou o fim não forem informados. */
    public static Periodo ouMesAtual(LocalDate inicio, LocalDate fim, LocalDate hoje) {
        if (inicio == null || fim == null) {
            return doMes(YearMonth.from(hoje));
        }
        return new Periodo(inicio, fim);
    }
}
