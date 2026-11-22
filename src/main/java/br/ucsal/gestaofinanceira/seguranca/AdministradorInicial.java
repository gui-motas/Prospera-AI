package br.ucsal.gestaofinanceira.seguranca;

import br.ucsal.gestaofinanceira.usuario.PerfilUsuario;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import br.ucsal.gestaofinanceira.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro Administrador (stakeholder 1.3.2) a partir de ADMIN_EMAIL e ADMIN_PASSWORD,
 * caso ainda não exista. Sem essas variáveis, nenhum administrador é criado.
 */
@Component
public class AdministradorInicial implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdministradorInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public AdministradorInicial(UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.admin.email:}") String email,
                                @Value("${app.admin.senha:}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return;
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email.trim())) {
            return;
        }
        try {
            ValidadorSenha.validar(senha);
        } catch (RuntimeException e) {
            // erro de configuração não deve derrubar a aplicação: sem admin, o resto funciona
            LOG.error("ADMIN_PASSWORD não atende à política de senha ({}). Administrador inicial não criado.",
                    e.getMessage());
            return;
        }
        Usuario admin = new Usuario("Administrador", email, passwordEncoder.encode(senha), PerfilUsuario.ADMINISTRADOR);
        admin.ativar();
        usuarioRepository.save(admin);
        LOG.info("Administrador inicial criado.");
    }
}
