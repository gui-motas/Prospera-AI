package br.ucsal.gestaofinanceira.conta;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public final class ContaDtos {

    private ContaDtos() {
    }

    public record ContaRequest(
            @NotBlank(message = "Informe o nome da conta.")
            @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres.")
            String nome,
            @NotNull(message = "Informe o tipo da conta.")
            TipoConta tipo,
            @Digits(integer = 13, fraction = 2, message = "Saldo inicial inválido.")
            BigDecimal saldoInicial) {
    }

    /** {@code saldoAtual} é calculado pela plataforma (RN-004). */
    public record ContaResponse(UUID id, String nome, TipoConta tipo, BigDecimal saldoInicial,
                                BigDecimal saldoAtual, long quantidadeMovimentacoes) {
    }
}
