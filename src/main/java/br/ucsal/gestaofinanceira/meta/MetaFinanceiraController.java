package br.ucsal.gestaofinanceira.meta;

import br.ucsal.gestaofinanceira.meta.MetaDtos.AporteRequest;
import br.ucsal.gestaofinanceira.meta.MetaDtos.MetaRequest;
import br.ucsal.gestaofinanceira.meta.MetaDtos.MetaResponse;
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
@RequestMapping("/api/v1/metas")
@Tag(name = "Metas financeiras", description = "RF-08 e RF-09 – cadastro e acompanhamento de metas")
public class MetaFinanceiraController {

    private final MetaFinanceiraService metaService;

    public MetaFinanceiraController(MetaFinanceiraService metaService) {
        this.metaService = metaService;
    }

    @GetMapping
    @Operation(summary = "Listar metas com progresso, ritmo e valor mensal necessário")
    public List<MetaResponse> listar() {
        return metaService.listar();
    }

    @PostMapping
    @Operation(summary = "Cadastrar meta (nome, valor desejado e prazo)")
    public ResponseEntity<MetaResponse> criar(@Valid @RequestBody MetaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.criar(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar meta")
    public MetaResponse editar(@PathVariable UUID id, @Valid @RequestBody MetaRequest req) {
        return metaService.editar(id, req);
    }

    @PostMapping("/{id}/aportes")
    @Operation(summary = "Registrar valor guardado (aporte) ou retirado (resgate) da meta")
    public MetaResponse registrarAporte(@PathVariable UUID id, @Valid @RequestBody AporteRequest req) {
        return metaService.registrarAporte(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir meta")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        metaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
