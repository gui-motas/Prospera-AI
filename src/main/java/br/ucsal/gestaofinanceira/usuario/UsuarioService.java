package br.ucsal.gestaofinanceira.usuario;

import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.seguranca.UsuarioAutenticado;
import br.ucsal.gestaofinanceira.seguranca.ValidadorSenha;
import br.ucsal.gestaofinanceira.usuario.UsuarioDtos.AlterarSenhaRequest;
import br.ucsal.gestaofinanceira.usuario.UsuarioDtos.AtualizarPerfilRequest;
import br.ucsal.gestaofinanceira.usuario.UsuarioDtos.UsuarioResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autenticação (RF-002) e gerenciamento do perfil do usuário (escopo 1.2).
 */
@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticado usuarioAutenticado;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioAutenticado usuarioAutenticado,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticado = usuarioAutenticado;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCase(Usuario.normalizarEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obterPerfil() {
        return UsuarioResponse.de(usuarioAutenticado.obter());
    }

    @Transactional
    public UsuarioResponse atualizarPerfil(AtualizarPerfilRequest req) {
        Usuario usuario = usuarioAutenticado.obter();
        usuario.atualizarPerfil(req.nome());
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public void alterarSenha(AlterarSenhaRequest req) {
        Usuario usuario = usuarioAutenticado.obter();
        if (!passwordEncoder.matches(req.senhaAtual(), usuario.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual está incorreta.");
        }
        ValidadorSenha.validar(req.novaSenha());
        usuario.alterarSenha(passwordEncoder.encode(req.novaSenha()));
        usuarioRepository.save(usuario);
    }
}
