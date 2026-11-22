package br.ucsal.gestaofinanceira.financeiro;

import br.ucsal.gestaofinanceira.seguranca.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** RF-007 – Dashboard financeiro: saldo, receitas, despesas, distribuição dos gastos e evolução. */
@RestController
@RequestMapping("/api/v1/painel")
@Tag(name = "Painel financeiro", description = "RF-007 – informações consolidadas da situação financeira")
public class PainelController {

    private final ServicoFinanceiro servicoFinanceiro;
    private final UsuarioAutenticado usuarioAutenticado;

    public PainelController(ServicoFinanceiro servicoFinanceiro, UsuarioAutenticado usuarioAutenticado) {
        this.servicoFinanceiro = servicoFinanceiro;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @GetMapping
    @Operation(summary = "Resumo financeiro do período (padrão: mês atual)")
    public ResumoFinanceiro resumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        LocalDate hoje = LocalDate.now();
        return servicoFinanceiro.consolidarDados(
                usuarioAutenticado.obterId(), Periodo.ouMesAtual(inicio, fim, hoje), hoje);
    }
}
