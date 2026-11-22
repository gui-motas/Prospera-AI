package br.ucsal.gestaofinanceira.conta;

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
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Classe {@code ContaFinanceira} do diagrama de classes (RF-003).
 * Atributos: id, nome, tipo, saldoInicial. Operações: editar(), calcularSaldo().
 *
 * <p>O saldo NÃO é armazenado: é sempre calculado pela plataforma a partir do saldo
 * inicial e das movimentações registradas (RN-004, RN-012).</p>
 */
@Entity
@Table(name = "contas_financeiras", uniqueConstraints = @UniqueConstraint(
        name = "uk_contas_financeiras_usuario_nome", columnNames = {"usuario_id", "nome"}))
public class ContaFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Associação "Usuario possui 0..* ContaFinanceira". */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConta tipo;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    protected ContaFinanceira() {
    }

    public ContaFinanceira(Usuario usuario, String nome, TipoConta tipo, BigDecimal saldoInicial) {
        this.usuario = usuario;
        editar(nome, tipo, saldoInicial);
    }

    public void editar(String nome, TipoConta tipo, BigDecimal saldoInicial) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Informe o nome da conta.");
        }
        if (tipo == null) {
            throw new RegraNegocioException("Informe o tipo da conta.");
        }
        this.nome = nome.trim();
        this.tipo = tipo;
        this.saldoInicial = saldoInicial == null ? BigDecimal.ZERO : saldoInicial;
    }

    /**
     * Saldo = saldo inicial + receitas − despesas da conta.
     *
     * @param resultadoMovimentacoes soma das receitas menos as despesas desta conta,
     *                               totalizada por consulta no banco (ServicoFinanceiro)
     */
    public BigDecimal calcularSaldo(BigDecimal resultadoMovimentacoes) {
        return saldoInicial.add(resultadoMovimentacoes == null ? BigDecimal.ZERO : resultadoMovimentacoes);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }
}
