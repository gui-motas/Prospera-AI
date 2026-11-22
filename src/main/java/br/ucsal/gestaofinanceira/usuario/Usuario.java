package br.ucsal.gestaofinanceira.usuario;

import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Classe {@code Usuario} do diagrama de classes.
 * Atributos: id, nome, email, senhaHash, perfil. Operações: atualizarPerfil(), alterarSenha().
 */
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    /** Falso enquanto o e-mail não for confirmado (quando a confirmação estiver habilitada). */
    @Column(nullable = false)
    private boolean ativo;

    /** Bloqueio aplicado pelo Administrador. */
    @Column(nullable = false)
    private boolean bloqueado;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Usuario() {
        // exigido pela JPA
    }

    public Usuario(String nome, String email, String senhaHash, PerfilUsuario perfil) {
        atualizarPerfil(nome);
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("Informe o e-mail.");
        }
        this.email = normalizarEmail(email);
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.ativo = false;
        this.bloqueado = false;
        this.criadoEm = LocalDateTime.now();
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public void atualizarPerfil(String novoNome) {
        if (novoNome == null || novoNome.isBlank()) {
            throw new RegraNegocioException("Informe o nome.");
        }
        this.nome = novoNome.trim();
    }

    public void alterarSenha(String novaSenhaHash) {
        this.senhaHash = novaSenhaHash;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void definirBloqueio(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    // --- UserDetails (Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !bloqueado;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
