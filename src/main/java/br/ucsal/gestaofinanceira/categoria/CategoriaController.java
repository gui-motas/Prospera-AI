package br.ucsal.gestaofinanceira.categoria;

import br.ucsal.gestaofinanceira.categoria.CategoriaDtos.CategoriaRequest;
import br.ucsal.gestaofinanceira.categoria.CategoriaDtos.CategoriaResponse;
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
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorias", description = "RF-004 – criar, consultar, editar e excluir categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar categorias")
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @PostMapping
    @Operation(summary = "Criar categoria")
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criar(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar categoria")
    public CategoriaResponse editar(@PathVariable UUID id, @Valid @RequestBody CategoriaRequest req) {
        return categoriaService.editar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria sem vínculos")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
