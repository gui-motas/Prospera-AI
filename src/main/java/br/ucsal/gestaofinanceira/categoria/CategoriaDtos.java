package br.ucsal.gestaofinanceira.categoria;

import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class CategoriaDtos {

    private CategoriaDtos() {
    }

    public record CategoriaRequest(
            @NotBlank(message = "Informe o nome da categoria.")
            @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres.")
            String nome,
            @NotNull(message = "Informe se a categoria é de receita ou de despesa.")
            TipoMovimentacao tipo) {
    }

    public record CategoriaResponse(UUID id, String nome, TipoMovimentacao tipo) {
        public static CategoriaResponse de(Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNome(), c.getTipo());
        }
    }
}
