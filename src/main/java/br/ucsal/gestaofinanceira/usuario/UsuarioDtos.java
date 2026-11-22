package br.ucsal.gestaofinanceira.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/** Objetos de entrada e saída do gerenciamento de perfil. */
public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    public record UsuarioResponse(UUID id, String nome, String email, PerfilUsuario perfil, LocalDateTime criadoEm) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.getCriadoEm());
        }
    }

    public record AtualizarPerfilRequest(
            @NotBlank(message = "Informe o nome.")
            @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
            String nome) {
    }

    public record AlterarSenhaRequest(
            @NotBlank(message = "Informe a senha atual.") String senhaAtual,
            @NotBlank(message = "Informe a nova senha.") String novaSenha) {
    }
}
