package br.ucsal.gestaofinanceira.admin;

import br.ucsal.gestaofinanceira.usuario.PerfilUsuario;
import br.ucsal.gestaofinanceira.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public final class AdminDtos {

    private AdminDtos() {
    }

    /** Dados cadastrais apenas — o administrador não tem acesso a dados financeiros (RN-001). */
    public record UsuarioAdminResponse(UUID id, String nome, String email, PerfilUsuario perfil,
                                       boolean ativo, boolean bloqueado, LocalDateTime criadoEm) {
        public static UsuarioAdminResponse de(Usuario u) {
            return new UsuarioAdminResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(),
                    u.isAtivo(), u.isBloqueado(), u.getCriadoEm());
        }
    }

    /** Campos nulos não são alterados. */
    public record SituacaoUsuarioRequest(Boolean ativo, Boolean bloqueado) {
    }

    /** Acompanhamento do funcionamento da plataforma. */
    public record StatusOperacaoResponse(long totalUsuarios, long usuariosAtivos, long usuariosBloqueados,
                                         long analisesGeradas, boolean iaDisponivel, LocalDateTime horaServidor) {
    }
}
