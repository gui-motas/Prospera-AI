package br.ucsal.gestaofinanceira.orcamento;

import br.ucsal.gestaofinanceira.orcamento.OrcamentoDtos.OrcamentoRequest;
import br.ucsal.gestaofinanceira.orcamento.OrcamentoDtos.OrcamentoResponse;
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
@RequestMapping("/api/v1/orcamentos")
@Tag(name = "Orçamentos", description = "Limites de gastos por categoria e período (RN-005)")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @GetMapping
    @Operation(summary = "Listar limites com total gasto, saldo restante e situação")
    public List<OrcamentoResponse> listar() {
        return orcamentoService.listar();
    }

    @PostMapping
    @Operation(summary = "Definir limite de gastos")
    public ResponseEntity<OrcamentoResponse> criar(@Valid @RequestBody OrcamentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orcamentoService.criar(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar limite de gastos")
    public OrcamentoResponse editar(@PathVariable UUID id, @Valid @RequestBody OrcamentoRequest req) {
        return orcamentoService.editar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir limite de gastos")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        orcamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
