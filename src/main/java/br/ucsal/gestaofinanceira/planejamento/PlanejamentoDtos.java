package br.ucsal.gestaofinanceira.planejamento;

import br.ucsal.gestaofinanceira.meta.StatusMeta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlanejamentoDtos {

    private PlanejamentoDtos() {
    }

    public record StatusIA(boolean iaDisponivel, String aviso) {
    }

    /** Diagnóstico determinístico que serve de base ao planejamento (RN-012). */
    public record DiagnosticoResponse(LocalDate dataReferencia, BigDecimal saldoTotal,
                                      BigDecimal capacidadeMensalEconomia, int mesesConsiderados,
                                      List<ViabilidadeMeta> metas) {
    }

    public record ViabilidadeMeta(UUID metaId, String nome, StatusMeta status, LocalDate prazo,
                                  BigDecimal valorRestante, BigDecimal valorMensalNecessario,
                                  long mesesRestantes, boolean viavelComCapacidadeAtual,
                                  Long mesesComCapacidadeAtual, LocalDate prazoProjetadoComCapacidadeAtual) {
    }

    /**
     * Simulação de cenário: quanto o usuário pretende reservar por mês e, opcionalmente,
     * a redução percentual de gastos em uma categoria de despesa.
     */
    public record CenarioRequest(
            @NotBlank(message = "Descreva o cenário.")
            @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
            String descricao,
            @NotNull(message = "Informe quanto pretende reservar por mês.")
            @PositiveOrZero(message = "O valor reservado por mês não pode ser negativo.")
            @Digits(integer = 13, fraction = 2, message = "Valor inválido.")
            BigDecimal economiaMensal,
            UUID categoriaReducaoId,
            @Min(value = 1, message = "A redução deve ser de pelo menos 1%.")
            @Max(value = 100, message = "A redução não pode passar de 100%.")
            Integer percentualReducao) {
    }

    public record CenarioResponse(UUID id, UUID metaId, String metaNome, String descricao,
                                  BigDecimal economiaMensalSimulada, BigDecimal reducaoMensalAplicada,
                                  LocalDate prazoProjetado, LocalDateTime criadoEm,
                                  ComparacaoCenario comparacao) {
    }

    public record AnaliseResponse(UUID id, TipoAnalise tipo, LocalDateTime dataGeracao, String resultado,
                                  UUID metaId, String metaNome, UUID cenarioId, String aviso,
                                  Map<String, Object> dadosEnviados) {

        public static AnaliseResponse de(AnaliseFinanceira a, Map<String, Object> dadosEnviados) {
            return new AnaliseResponse(a.getId(), a.getTipo(), a.getDataGeracao(), a.consultarResultado(),
                    a.getMeta() == null ? null : a.getMeta().getId(),
                    a.getMeta() == null ? null : a.getMeta().getNome(),
                    a.getCenario() == null ? null : a.getCenario().getId(),
                    ServicoIA.AVISO, dadosEnviados);
        }
    }
}
