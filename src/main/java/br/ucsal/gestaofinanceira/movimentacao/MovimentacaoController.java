package br.ucsal.gestaofinanceira.movimentacao;

import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoDtos.MovimentacaoRequest;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoDtos.MovimentacaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movimentacoes")
@Tag(name = "Movimentações", description = "RF-005 e RF-006 – registrar, consultar, filtrar, editar e excluir")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @GetMapping
    @Operation(summary = "Histórico de movimentações com filtros por período, categoria, tipo, conta e texto")
    public List<MovimentacaoResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) UUID contaId,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) String texto) {
        return movimentacaoService.listar(new FiltroMovimentacao(dataInicio, dataFim, categoriaId, contaId, tipo, texto));
    }

    @PostMapping
    @Operation(summary = "Registrar receita ou despesa")
    public ResponseEntity<MovimentacaoResponse> registrar(@Valid @RequestBody MovimentacaoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimentacaoService.registrar(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar movimentação")
    public MovimentacaoResponse editar(@PathVariable UUID id, @Valid @RequestBody MovimentacaoRequest req) {
        return movimentacaoService.editar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir movimentação")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        movimentacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
