package br.ucsal.gestaofinanceira.conta;

import br.ucsal.gestaofinanceira.conta.ContaDtos.ContaRequest;
import br.ucsal.gestaofinanceira.conta.ContaDtos.ContaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contas")
@Tag(name = "Contas financeiras", description = "RF-003 – cadastrar, consultar, editar e excluir contas")
public class ContaFinanceiraController {

    private final ContaFinanceiraService contaService;

    public ContaFinanceiraController(ContaFinanceiraService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    @Operation(summary = "Listar contas com saldo calculado")
    public List<ContaResponse> listar() {
        return contaService.listar();
    }

    @PostMapping
    @Operation(summary = "Cadastrar conta")
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contaService.criar(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar conta")
    public ContaResponse editar(@PathVariable UUID id, @Valid @RequestBody ContaRequest req) {
        return contaService.editar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir conta (e suas movimentações)")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        contaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
