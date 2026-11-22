package br.ucsal.gestaofinanceira.cadastro;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CadastroDtos {

    private CadastroDtos() {
    }

    /** RF-001: informações obrigatórias para o cadastro (apenas o necessário – RNF-005). */
    public record CadastroRequest(
            @NotBlank(message = "Informe o nome.")
            @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
            String nome,
            @NotBlank(message = "Informe o e-mail.")
            @Email(message = "Informe um e-mail válido.")
            @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres.")
            String email,
            @NotBlank(message = "Informe a senha.")
            @Size(max = 72, message = "A senha deve ter no máximo 72 caracteres.")
            String senha) {
    }

    public record CadastroResponse(String mensagem, boolean confirmacaoPendente) {
    }

    public record MensagemResponse(String mensagem) {
    }
}
