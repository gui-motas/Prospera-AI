package br.ucsal.gestaofinanceira.cadastro;

import br.ucsal.gestaofinanceira.cadastro.CadastroDtos.CadastroRequest;
import br.ucsal.gestaofinanceira.cadastro.CadastroDtos.CadastroResponse;
import br.ucsal.gestaofinanceira.cadastro.CadastroDtos.MensagemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cadastro")
@Tag(name = "Cadastro", description = "RF-001 – cadastro de usuário e confirmação de e-mail")
public class CadastroController {

    private final CadastroService cadastroService;

    public CadastroController(CadastroService cadastroService) {
        this.cadastroService = cadastroService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo usuário")
    public ResponseEntity<CadastroResponse> cadastrar(@Valid @RequestBody CadastroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroService.cadastrar(req));
    }

    @GetMapping("/confirmar")
    @Operation(summary = "Confirmar e-mail pelo token recebido")
    public MensagemResponse confirmar(@RequestParam String token) {
        cadastroService.confirmar(token);
        return new MensagemResponse("E-mail confirmado. Você já pode entrar.");
    }
}
