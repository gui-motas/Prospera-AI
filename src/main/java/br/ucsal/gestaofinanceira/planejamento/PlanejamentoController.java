package br.ucsal.gestaofinanceira.planejamento;

import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.AnaliseResponse;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.CenarioRequest;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.CenarioResponse;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.DiagnosticoResponse;
import br.ucsal.gestaofinanceira.planejamento.PlanejamentoDtos.StatusIA;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * RF-010 – Planejamento Inteligente de Metas. Todas as rotas exigem autenticação (RN-008).
 */
@RestController
@RequestMapping("/api/v1/planejamento")
@Tag(name = "Planejamento inteligente", description = "RF-010 – análises com IA e simulação de cenários")
public class PlanejamentoController {

    private final PlanejamentoService planejamentoService;

    public PlanejamentoController(PlanejamentoService planejamentoService) {
        this.planejamentoService = planejamentoService;
    }

    @GetMapping("/status")
    @Operation(summary = "Disponibilidade da API de IA e aviso de caráter informativo")
    public StatusIA status() {
        return planejamentoService.status();
    }

    @GetMapping("/diagnostico")
    @Operation(summary = "Capacidade de economia e viabilidade das metas (cálculo da plataforma)")
    public DiagnosticoResponse diagnostico() {
        return planejamentoService.diagnosticar();
    }

    @PostMapping("/analises/situacao")
    @Operation(summary = "Gerar análise da situação financeira com IA")
    public AnaliseResponse analisarSituacao() {
        return planejamentoService.analisarSituacao();
    }

    @PostMapping("/metas/{metaId}/analises")
    @Operation(summary = "Analisar viabilidade de uma meta com IA")
    public AnaliseResponse analisarMeta(@PathVariable UUID metaId) {
        return planejamentoService.analisarMeta(metaId);
    }

    @GetMapping("/analises")
    @Operation(summary = "Últimas análises geradas")
    public List<AnaliseResponse> historico() {
        return planejamentoService.historico();
    }

    @GetMapping("/metas/{metaId}/cenarios")
    @Operation(summary = "Listar cenários simulados de uma meta")
    public List<CenarioResponse> listarCenarios(@PathVariable UUID metaId) {
        return planejamentoService.listarCenarios(metaId);
    }

    @PostMapping("/metas/{metaId}/cenarios")
    @Operation(summary = "Simular cenário hipotético para uma meta")
    public ResponseEntity<CenarioResponse> simular(@PathVariable UUID metaId, @Valid @RequestBody CenarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planejamentoService.simularCenario(metaId, req));
    }

    @PostMapping("/cenarios/{cenarioId}/interpretacoes")
    @Operation(summary = "Interpretar um cenário com IA")
    public AnaliseResponse interpretar(@PathVariable UUID cenarioId) {
        return planejamentoService.interpretarCenario(cenarioId);
    }

    @DeleteMapping("/cenarios/{cenarioId}")
    @Operation(summary = "Excluir cenário")
    public ResponseEntity<Void> excluirCenario(@PathVariable UUID cenarioId) {
        planejamentoService.excluirCenario(cenarioId);
        return ResponseEntity.noContent().build();
    }
}
