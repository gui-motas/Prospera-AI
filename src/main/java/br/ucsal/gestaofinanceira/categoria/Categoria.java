package br.ucsal.gestaofinanceira.categoria;

import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
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

import java.util.UUID;

/**
 * Classe {@code Categoria} do diagrama de classes (RF-004).
 * Atributos: id, nome, tipo. Operações: editar(), validarCompatibilidade(tipo).
 */
@Entity
@Table(name = "categorias", uniqueConstraints = @UniqueConstraint(
        name = "uk_categorias_usuario_nome_tipo", columnNames = {"usuario_id", "nome", "tipo"}))
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Associação "Usuario define 0..* Categoria". */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 60)
    private String nome;

    /** Classificação da categoria: só aceita movimentações do mesmo tipo (RN-003). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    protected Categoria() {
    }

    public Categoria(Usuario usuario, String nome, TipoMovimentacao tipo) {
        this.usuario = usuario;
        editar(nome, tipo);
    }

    public void editar(String nome, TipoMovimentacao tipo) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Informe o nome da categoria.");
        }
        if (tipo == null) {
            throw new RegraNegocioException("Informe se a categoria é de receita ou de despesa.");
        }
        this.nome = nome.trim();
        this.tipo = tipo;
    }

    /** RN-003: a categoria deve ser compatível com o tipo da movimentação. */
    public boolean validarCompatibilidade(TipoMovimentacao tipoMovimentacao) {
        return this.tipo == tipoMovimentacao;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }
}
