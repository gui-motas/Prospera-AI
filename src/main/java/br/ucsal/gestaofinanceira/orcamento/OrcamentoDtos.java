package br.ucsal.gestaofinanceira.orcamento;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class OrcamentoDtos {

    private OrcamentoDtos() {
    }

    public record OrcamentoRequest(
            @NotNull(message = "Selecione a categoria.")
            UUID categoriaId,
            @NotNull(message = "Informe o limite.")
            @Positive(message = "O limite deve ser maior que zero.")
            @Digits(integer = 13, fraction = 2, message = "Limite inválido.")
            BigDecimal limite,
            @NotNull(message = "Informe o início do período.")
            LocalDate inicioPeriodo,
            @NotNull(message = "Informe o fim do período.")
            LocalDate fimPeriodo) {
    }

    /** Acompanhamento do orçamento: valores calculados pela plataforma. */
    public record OrcamentoResponse(UUID id, UUID categoriaId, String categoriaNome, BigDecimal limite,
                                    LocalDate inicioPeriodo, LocalDate fimPeriodo, BigDecimal totalGasto,
                                    BigDecimal saldoRestante, BigDecimal percentualUtilizado,
                                    SituacaoOrcamento situacao) {
    }
}
