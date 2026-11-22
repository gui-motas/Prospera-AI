package br.ucsal.gestaofinanceira.movimentacao;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.conta.ContaFinanceira;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Classe {@code Movimentacao} do diagrama de classes (RF-005, RF-006).
 * Atributos: id, descricao, valor, data, tipo. Operações: validarValor(), editar().
 */
@Entity
@Table(name = "movimentacoes", indexes = @Index(name = "idx_movimentacoes_data", columnList = "data"))
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 140)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    /** Associação "ContaFinanceira registra 0..* Movimentacao". */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private ContaFinanceira conta;

    /** Associação "Categoria classifica 0..* Movimentacao". */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    protected Movimentacao() {
    }

    public Movimentacao(ContaFinanceira conta, Categoria categoria, TipoMovimentacao tipo,
                        String descricao, BigDecimal valor, LocalDate data) {
        editar(conta, categoria, tipo, descricao, valor, data);
    }

    public void editar(ContaFinanceira conta, Categoria categoria, TipoMovimentacao tipo,
                       String descricao, BigDecimal valor, LocalDate data) {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Informe a descrição da movimentação.");
        }
        if (conta == null || categoria == null || tipo == null || data == null) {
            throw new RegraNegocioException("Informe conta, categoria, tipo e data da movimentação.");
        }
        this.conta = conta;
        this.categoria = categoria;
        this.tipo = tipo;
        this.descricao = descricao.trim();
        this.valor = valor;
        this.data = data;

        if (!validarValor()) {
            // RN-002
            throw new RegraNegocioException("O valor da movimentação deve ser maior que zero.");
        }
        if (!categoria.validarCompatibilidade(tipo)) {
            // RN-003
            throw new RegraNegocioException("A categoria \"" + categoria.getNome()
                    + "\" não é compatível com uma " + (tipo == TipoMovimentacao.RECEITA ? "receita" : "despesa") + ".");
        }
    }

    /** RN-002: toda movimentação deve ter valor monetário superior a zero. */
    public boolean validarValor() {
        return valor != null && valor.signum() > 0;
    }

    /** Valor positivo para receitas e negativo para despesas (usado no cálculo do saldo). */
    public BigDecimal valorComSinal() {
        return tipo == TipoMovimentacao.RECEITA ? valor : valor.negate();
    }

    public UUID getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getData() {
        return data;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public ContaFinanceira getConta() {
        return conta;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}
