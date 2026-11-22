package br.ucsal.gestaofinanceira.meta;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class MetaDtos {

    private MetaDtos() {
    }

    /** RF-08: nome, valor desejado e prazo são obrigatórios (RN-006, RN-007). */
    public record MetaRequest(
            @NotBlank(message = "Informe o nome da meta.")
            @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
            String nome,
            @NotNull(message = "Informe o valor desejado.")
            @Positive(message = "O valor da meta deve ser maior que zero.")
            @Digits(integer = 13, fraction = 2, message = "Valor inválido.")
            BigDecimal valorDesejado,
            @NotNull(message = "Informe o prazo para conclusão.")
            LocalDate prazo) {
    }

    public enum OperacaoAporte { APORTE, RESGATE }

    public record AporteRequest(
            @NotNull(message = "Informe a operação.")
            OperacaoAporte operacao,
            @NotNull(message = "Informe o valor.")
            @Positive(message = "O valor deve ser maior que zero.")
            @Digits(integer = 13, fraction = 2, message = "Valor inválido.")
            BigDecimal valor) {
    }

    /** RF-09: progresso em relação ao valor e ao prazo, calculado pela plataforma (RN-012). */
    public record MetaResponse(UUID id, String nome, BigDecimal valorDesejado, BigDecimal valorAcumulado,
                               BigDecimal valorRestante, BigDecimal progresso, BigDecimal progressoEsperado,
                               boolean noRitmo, LocalDate prazo, long mesesRestantes,
                               BigDecimal valorMensalNecessario, StatusMeta status, LocalDate criadaEm) {

        public static MetaResponse de(MetaFinanceira m, LocalDate hoje) {
            return new MetaResponse(m.getId(), m.getNome(), m.getValorDesejado(), m.getValorAcumulado(),
                    m.calcularValorRestante(), m.calcularProgresso(), m.calcularProgressoEsperado(hoje),
                    m.estaNoRitmo(hoje), m.getPrazo(), m.calcularMesesRestantes(hoje),
                    m.calcularValorMensalNecessario(hoje), m.getStatus(), m.getCriadaEm());
        }
    }
}
