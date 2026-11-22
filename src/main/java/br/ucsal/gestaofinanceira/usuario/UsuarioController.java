package br.ucsal.gestaofinanceira.usuario;

import br.ucsal.gestaofinanceira.usuario.UsuarioDtos.AlterarSenhaRequest;
import br.ucsal.gestaofinanceira.usuario.UsuarioDtos.AtualizarPerfilRequest;
import br.ucsal.gestaofinanceira.usuario.UsuarioDtos.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios/me")
@Tag(name = "Perfil do usuário", description = "Dados do usuário autenticado e gerenciamento do perfil")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Dados do usuário autenticado")
    public UsuarioResponse obter() {
        return usuarioService.obterPerfil();
    }

    @PutMapping
    @Operation(summary = "Atualizar perfil (nome)")
    public UsuarioResponse atualizar(@Valid @RequestBody AtualizarPerfilRequest req) {
        return usuarioService.atualizarPerfil(req);
    }

    @PutMapping("/senha")
    @Operation(summary = "Alterar senha")
    public ResponseEntity<Void> alterarSenha(@Valid @RequestBody AlterarSenhaRequest req) {
        usuarioService.alterarSenha(req);
        return ResponseEntity.noContent().build();
    }
}
