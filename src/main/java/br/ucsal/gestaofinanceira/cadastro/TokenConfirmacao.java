package br.ucsal.gestaofinanceira.cadastro;

import br.ucsal.gestaofinanceira.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/** Token enviado por e-mail para confirmar o cadastro (usado quando APP_CONFIRMACAO_EMAIL=true). */
@Entity
@Table(name = "tokens_confirmacao")
public class TokenConfirmacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    protected TokenConfirmacao() {
    }

    public TokenConfirmacao(String token, Usuario usuario, LocalDateTime agora, LocalDateTime expiraEm) {
        this.token = token;
        this.usuario = usuario;
        this.criadoEm = agora;
        this.expiraEm = expiraEm;
    }

    public boolean estaExpirado(LocalDateTime agora) {
        return expiraEm.isBefore(agora);
    }

    public boolean jaConfirmado() {
        return confirmadoEm != null;
    }

    public void confirmar(LocalDateTime agora) {
        this.confirmadoEm = agora;
        usuario.ativar();
    }
}
