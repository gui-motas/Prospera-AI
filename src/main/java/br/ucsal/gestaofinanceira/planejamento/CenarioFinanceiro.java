package br.ucsal.gestaofinanceira.planejamento;

import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.meta.MetaFinanceira;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe {@code CenarioFinanceiro} do diagrama de classes — simulação de cenário hipotético
 * para uma meta (ex.: "e se eu economizar R$ 600 por mês?" ou "e se eu reduzir 30% do lazer?").
 * Atributos: id, descricao, economiaMensalSimulada, prazoProjetado.
 * Operações: calcularProjecao(), compararComSituacaoAtual().
 *
 * <p>A projeção é calculada pela plataforma de forma determinística (RN-012);
 * a IA apenas interpreta o resultado.</p>
 */
@Entity
@Table(name = "cenarios_financeiros")
public class CenarioFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Associação "MetaFinanceira possui 0..* CenarioFinanceiro". */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meta_id", nullable = false)
    private MetaFinanceira meta;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(name = "economia_mensal_simulada", nullable = false, precision = 15, scale = 2)
    private BigDecimal economiaMensalSimulada;

    @Column(name = "prazo_projetado", nullable = false)
    private LocalDate prazoProjetado;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected CenarioFinanceiro() {
    }

    public CenarioFinanceiro(MetaFinanceira meta, String descricao, BigDecimal economiaMensalSimulada, LocalDate hoje) {
        if (meta == null) {
            throw new RegraNegocioException("Selecione a meta do cenário.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Descreva o cenário.");
        }
        if (economiaMensalSimulada == null || economiaMensalSimulada.signum() <= 0) {
            throw new RegraNegocioException("A economia mensal simulada precisa ser maior que zero.");
        }
        this.meta = meta;
        this.descricao = descricao.trim();
        this.economiaMensalSimulada = economiaMensalSimulada.setScale(2, RoundingMode.HALF_UP);
        this.criadoEm = LocalDateTime.now();
        calcularProjecao(hoje);
    }

    /** Projeta a data de conclusão da meta reservando {@code economiaMensalSimulada} por mês. */
    public LocalDate calcularProjecao(LocalDate hoje) {
        long meses = calcularMesesNecessarios(meta.calcularValorRestante(), economiaMensalSimulada);
        this.prazoProjetado = hoje.plusMonths(meses);
        return prazoProjetado;
    }

    /** Compara o cenário com a economia mensal atual do usuário. */
    public ComparacaoCenario compararComSituacaoAtual(BigDecimal economiaMensalAtual, LocalDate hoje) {
        BigDecimal atual = economiaMensalAtual == null ? BigDecimal.ZERO : economiaMensalAtual;
        BigDecimal restante = meta.calcularValorRestante();

        long mesesCenario = calcularMesesNecessarios(restante, economiaMensalSimulada);
        LocalDate prazoCenario = hoje.plusMonths(mesesCenario);

        Long mesesAtual = null;
        LocalDate prazoAtual = null;
        if (atual.signum() > 0) {
            mesesAtual = calcularMesesNecessarios(restante, atual);
            prazoAtual = hoje.plusMonths(mesesAtual);
        }

        return new ComparacaoCenario(
                meta.getPrazo(),
                restante,
                meta.calcularValorMensalNecessario(hoje),
                atual,
                mesesAtual,
                prazoAtual,
                prazoAtual != null && !prazoAtual.isAfter(meta.getPrazo()),
                economiaMensalSimulada,
                mesesCenario,
                prazoCenario,
                !prazoCenario.isAfter(meta.getPrazo()),
                mesesAtual == null ? null : mesesAtual - mesesCenario,
                economiaMensalSimulada.subtract(atual));
    }

    /** Meses necessários para acumular {@code restante} guardando {@code porMes} (arredondado para cima). */
    public static long calcularMesesNecessarios(BigDecimal restante, BigDecimal porMes) {
        if (restante.signum() <= 0) {
            return 0;
        }
        if (porMes == null || porMes.signum() <= 0) {
            throw new RegraNegocioException("A economia mensal precisa ser maior que zero.");
        }
        return restante.divide(porMes, 0, RoundingMode.CEILING).longValue();
    }

    public UUID getId() {
        return id;
    }

    public MetaFinanceira getMeta() {
        return meta;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getEconomiaMensalSimulada() {
        return economiaMensalSimulada;
    }

    public LocalDate getPrazoProjetado() {
        return prazoProjetado;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
