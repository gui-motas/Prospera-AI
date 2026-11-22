package br.ucsal.gestaofinanceira.movimentacao;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class MovimentacaoDtos {

    private MovimentacaoDtos() {
    }

    public record MovimentacaoRequest(
            @NotNull(message = "Informe se é receita ou despesa.")
            TipoMovimentacao tipo,
            @NotBlank(message = "Informe a descrição.")
            @Size(max = 140, message = "A descrição deve ter no máximo 140 caracteres.")
            String descricao,
            @NotNull(message = "Informe o valor.")
            @Positive(message = "O valor deve ser maior que zero.")
            @Digits(integer = 13, fraction = 2, message = "Valor inválido.")
            BigDecimal valor,
            @NotNull(message = "Informe a data.")
            LocalDate data,
            @NotNull(message = "Selecione a conta.")
            UUID contaId,
            @NotNull(message = "Selecione a categoria.")
            UUID categoriaId) {
    }

    public record MovimentacaoResponse(UUID id, TipoMovimentacao tipo, String descricao, BigDecimal valor,
                                       LocalDate data, UUID contaId, String contaNome,
                                       UUID categoriaId, String categoriaNome) {
        public static MovimentacaoResponse de(Movimentacao m) {
            return new MovimentacaoResponse(m.getId(), m.getTipo(), m.getDescricao(), m.getValor(), m.getData(),
                    m.getConta().getId(), m.getConta().getNome(),
                    m.getCategoria().getId(), m.getCategoria().getNome());
        }
    }
}
