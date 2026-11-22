package br.ucsal.gestaofinanceira.seguranca;

import br.ucsal.gestaofinanceira.usuario.Usuario;
import br.ucsal.gestaofinanceira.usuario.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Obtém o usuário da sessão atual. Toda consulta a dados financeiros é filtrada
 * pelo id retornado aqui, garantindo que cada usuário acesse apenas os próprios dados
 * (RN-001, RNF-004).
 */
@Component
public class UsuarioAutenticado {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticado(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obter() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("Usuário não autenticado.");
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Usuário não encontrado."));
        // Um bloqueio feito pelo Administrador vale imediatamente, mesmo com sessão aberta.
        if (usuario.isBloqueado() || !usuario.isAtivo()) {
            throw new AccessDeniedException("Conta bloqueada ou inativa.");
        }
        return usuario;
    }

    public UUID obterId() {
        return obter().getId();
    }
}
