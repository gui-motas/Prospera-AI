package br.ucsal.gestaofinanceira.meta;

import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Classe {@code MetaFinanceira} do diagrama de classes (RF-08, RF-09).
 * Atributos: id, nome, valorDesejado, valorAcumulado, prazo, status.
 * Operações: atualizarValorAcumulado(valor), calcularProgresso(), calcularValorRestante().
 */
@Entity
@Table(name = "metas_financeiras")
public class MetaFinanceira {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    /** Tolerância, em pontos percentuais, para considerar a meta "no ritmo". */
    private static final BigDecimal TOLERANCIA_RITMO = BigDecimal.valueOf(5);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Associação "Usuario planeja 0..* MetaFinanceira". */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "valor_desejado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorDesejado;

    @Column(name = "valor_acumulado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAcumulado;

    @Column(nullable = false)
    private LocalDate prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMeta status;

    @Column(name = "criada_em", nullable = false)
    private LocalDate criadaEm;

    protected MetaFinanceira() {
    }

    public MetaFinanceira(Usuario usuario, String nome, BigDecimal valorDesejado, LocalDate prazo, LocalDate hoje) {
        this.usuario = usuario;
        this.valorAcumulado = BigDecimal.ZERO;
        this.criadaEm = hoje;
        editar(nome, valorDesejado, prazo, hoje);
    }

    /** RN-006 (nome, valor e prazo obrigatórios) e RN-007 (valor maior que zero). */
    public void editar(String nome, BigDecimal valorDesejado, LocalDate prazo, LocalDate hoje) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Informe o nome da meta.");
        }
        if (valorDesejado == null || valorDesejado.signum() <= 0) {
            throw new RegraNegocioException("O valor da meta deve ser maior que zero.");
        }
        if (prazo == null) {
            throw new RegraNegocioException("Informe o prazo para conclusão da meta.");
        }
        this.nome = nome.trim();
        this.valorDesejado = valorDesejado;
        this.prazo = prazo;
        atualizarStatus(hoje);
    }

    /**
     * Registra um aporte (valor positivo) ou resgate (valor negativo) feito pelo usuário.
     * Só é chamado a partir de uma ação explícita do usuário (RN-011).
     */
    public void atualizarValorAcumulado(BigDecimal valor, LocalDate hoje) {
        if (valor == null || valor.signum() == 0) {
            throw new RegraNegocioException("Informe um valor diferente de zero.");
        }
        BigDecimal novoValor = valorAcumulado.add(valor);
        if (novoValor.signum() < 0) {
            throw new RegraNegocioException("O resgate não pode ser maior que o valor acumulado na meta.");
        }
        this.valorAcumulado = novoValor;
        atualizarStatus(hoje);
    }

    /** Percentual do valor desejado já acumulado (0 a 100). */
    public BigDecimal calcularProgresso() {
        BigDecimal progresso = valorAcumulado.multiply(CEM).divide(valorDesejado, 1, RoundingMode.HALF_UP);
        return progresso.min(CEM.setScale(1, RoundingMode.HALF_UP));
    }

    public BigDecimal calcularValorRestante() {
        return valorDesejado.subtract(valorAcumulado).max(BigDecimal.ZERO);
    }

    /** Meses entre hoje e o prazo, arredondado para cima (0 se o prazo já passou). */
    public long calcularMesesRestantes(LocalDate hoje) {
        if (!prazo.isAfter(hoje)) {
            return 0;
        }
        long dias = ChronoUnit.DAYS.between(hoje, prazo);
        return (long) Math.ceil(dias / 30.0);
    }

    /** Quanto o usuário precisa reservar por mês para cumprir o prazo. */
    public BigDecimal calcularValorMensalNecessario(LocalDate hoje) {
        long meses = Math.max(1, calcularMesesRestantes(hoje));
        return calcularValorRestante().divide(BigDecimal.valueOf(meses), 2, RoundingMode.UP);
    }

    /** Progresso esperado proporcional ao tempo decorrido entre a criação e o prazo (RF-09). */
    public BigDecimal calcularProgressoEsperado(LocalDate hoje) {
        long total = ChronoUnit.DAYS.between(criadaEm, prazo);
        if (total <= 0 || !hoje.isBefore(prazo)) {
            return CEM.setScale(1, RoundingMode.HALF_UP);
        }
        long decorridos = Math.max(0, ChronoUnit.DAYS.between(criadaEm, hoje));
        return BigDecimal.valueOf(decorridos).multiply(CEM).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    /** Indica se a evolução está compatível com o prazo (RF-09). */
    public boolean estaNoRitmo(LocalDate hoje) {
        if (status == StatusMeta.CONCLUIDA) {
            return true;
        }
        return calcularProgresso().add(TOLERANCIA_RITMO).compareTo(calcularProgressoEsperado(hoje)) >= 0;
    }

    public void atualizarStatus(LocalDate hoje) {
        if (valorAcumulado.compareTo(valorDesejado) >= 0) {
            status = StatusMeta.CONCLUIDA;
        } else if (hoje.isAfter(prazo)) {
            status = StatusMeta.ATRASADA;
        } else {
            status = StatusMeta.EM_ANDAMENTO;
        }
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorDesejado() {
        return valorDesejado;
    }

    public BigDecimal getValorAcumulado() {
        return valorAcumulado;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public StatusMeta getStatus() {
        return status;
    }

    public LocalDate getCriadaEm() {
        return criadaEm;
    }
}
