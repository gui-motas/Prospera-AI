package br.ucsal.gestaofinanceira.orcamento;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
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
import java.util.UUID;

/**
 * Classe {@code Orcamento} do diagrama de classes — limite de gastos de uma categoria em um período.
 * Atributos: id, limite, inicioPeriodo, fimPeriodo.
 * Operações: calcularTotalGasto(), calcularSaldoRestante(), verificarLimite().
 *
 * <p>O total gasto é obtido por consulta agregada no banco (OrcamentoService) e repassado
 * aos métodos de cálculo desta classe.</p>
 */
@Entity
@Table(name = "orcamentos")
public class Orcamento {

    /** Percentual a partir do qual o usuário é alertado antes de ultrapassar o limite. */
    public static final BigDecimal PERCENTUAL_ALERTA = BigDecimal.valueOf(80);

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Associação "Categoria delimita 0..* Orcamento". */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal limite;

    @Column(name = "inicio_periodo", nullable = false)
    private LocalDate inicioPeriodo;

    @Column(name = "fim_periodo", nullable = false)
    private LocalDate fimPeriodo;

    protected Orcamento() {
    }

    public Orcamento(Categoria categoria, BigDecimal limite, LocalDate inicioPeriodo, LocalDate fimPeriodo) {
        editar(categoria, limite, inicioPeriodo, fimPeriodo);
    }

    /** RN-005: limite maior que zero, associado a uma categoria e a um período válido. */
    public void editar(Categoria categoria, BigDecimal limite, LocalDate inicioPeriodo, LocalDate fimPeriodo) {
        if (categoria == null) {
            throw new RegraNegocioException("Selecione a categoria do limite de gastos.");
        }
        if (categoria.getTipo() != TipoMovimentacao.DESPESA) {
            throw new RegraNegocioException("Limites de gastos só podem ser definidos para categorias de despesa.");
        }
        if (limite == null || limite.signum() <= 0) {
            throw new RegraNegocioException("O limite de gastos deve ser maior que zero.");
        }
        if (inicioPeriodo == null || fimPeriodo == null) {
            throw new RegraNegocioException("Informe o período do limite de gastos.");
        }
        if (fimPeriodo.isBefore(inicioPeriodo)) {
            throw new RegraNegocioException("O fim do período não pode ser anterior ao início.");
        }
        this.categoria = categoria;
        this.limite = limite;
        this.inicioPeriodo = inicioPeriodo;
        this.fimPeriodo = fimPeriodo;
    }

    public boolean sobrepoe(LocalDate inicio, LocalDate fim) {
        return !inicioPeriodo.isAfter(fim) && !fimPeriodo.isBefore(inicio);
    }

    public BigDecimal calcularSaldoRestante(BigDecimal totalGasto) {
        return limite.subtract(totalGasto);
    }

    /** @return {@code true} enquanto o total gasto não ultrapassa o limite. */
    public boolean verificarLimite(BigDecimal totalGasto) {
        return totalGasto.compareTo(limite) <= 0;
    }

    public BigDecimal calcularPercentualUtilizado(BigDecimal totalGasto) {
        return totalGasto.multiply(CEM).divide(limite, 1, RoundingMode.HALF_UP);
    }

    public SituacaoOrcamento avaliarSituacao(BigDecimal totalGasto) {
        if (!verificarLimite(totalGasto)) {
            return SituacaoOrcamento.EXCEDIDO;
        }
        if (calcularPercentualUtilizado(totalGasto).compareTo(PERCENTUAL_ALERTA) >= 0) {
            return SituacaoOrcamento.ALERTA;
        }
        return SituacaoOrcamento.DENTRO_DO_LIMITE;
    }

    public UUID getId() {
        return id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public BigDecimal getLimite() {
        return limite;
    }

    public LocalDate getInicioPeriodo() {
        return inicioPeriodo;
    }

    public LocalDate getFimPeriodo() {
        return fimPeriodo;
    }
}
