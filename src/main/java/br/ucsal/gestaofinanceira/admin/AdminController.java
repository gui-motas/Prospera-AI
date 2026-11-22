package br.ucsal.gestaofinanceira.admin;

import br.ucsal.gestaofinanceira.admin.AdminDtos.SituacaoUsuarioRequest;
import br.ucsal.gestaofinanceira.admin.AdminDtos.StatusOperacaoResponse;
import br.ucsal.gestaofinanceira.admin.AdminDtos.UsuarioAdminResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Rotas restritas ao perfil Administrador (ver SegurancaConfig). */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administração", description = "Gerenciamento de usuários e monitoramento da operação")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuários")
    public List<UsuarioAdminResponse> listarUsuarios() {
        return adminService.listarUsuarios();
    }

    @PatchMapping("/usuarios/{id}/situacao")
    @Operation(summary = "Ativar, bloquear ou desbloquear usuário")
    public UsuarioAdminResponse alterarSituacao(@PathVariable UUID id, @RequestBody SituacaoUsuarioRequest req) {
        return adminService.alterarSituacao(id, req);
    }

    @GetMapping("/status")
    @Operation(summary = "Indicadores de funcionamento da plataforma")
    public StatusOperacaoResponse status() {
        return adminService.statusOperacao();
    }
}
