package br.ucsal.gestaofinanceira.admin;

import br.ucsal.gestaofinanceira.admin.AdminDtos.SituacaoUsuarioRequest;
import br.ucsal.gestaofinanceira.admin.AdminDtos.StatusOperacaoResponse;
import br.ucsal.gestaofinanceira.admin.AdminDtos.UsuarioAdminResponse;
import br.ucsal.gestaofinanceira.comum.RecursoNaoEncontradoException;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.planejamento.AnaliseFinanceiraRepository;
import br.ucsal.gestaofinanceira.planejamento.ServicoIA;
import br.ucsal.gestaofinanceira.seguranca.UsuarioAutenticado;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import br.ucsal.gestaofinanceira.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Caso de uso "Gerenciar usuários e monitorar operação" (perfil Administrador). */
@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final AnaliseFinanceiraRepository analiseRepository;
    private final ServicoIA servicoIA;
    private final UsuarioAutenticado usuarioAutenticado;

    public AdminService(UsuarioRepository usuarioRepository,
                        AnaliseFinanceiraRepository analiseRepository,
                        ServicoIA servicoIA,
                        UsuarioAutenticado usuarioAutenticado) {
        this.usuarioRepository = usuarioRepository;
        this.analiseRepository = analiseRepository;
        this.servicoIA = servicoIA;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuarios() {
        return usuarioRepository.findAllByOrderByCriadoEmDesc().stream()
                .map(UsuarioAdminResponse::de)
                .toList();
    }

    @Transactional
    public UsuarioAdminResponse alterarSituacao(UUID id, SituacaoUsuarioRequest req) {
        Usuario admin = usuarioAutenticado.obter();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        if (usuario.getId().equals(admin.getId())) {
            throw new RegraNegocioException("Você não pode alterar a situação da sua própria conta.");
        }
        if (Boolean.TRUE.equals(req.ativo())) {
            usuario.ativar();
        }
        if (req.bloqueado() != null) {
            usuario.definirBloqueio(req.bloqueado());
        }
        return UsuarioAdminResponse.de(usuario);
    }

    @Transactional(readOnly = true)
    public StatusOperacaoResponse statusOperacao() {
        return new StatusOperacaoResponse(
                usuarioRepository.count(),
                usuarioRepository.countByAtivoTrue(),
                usuarioRepository.countByBloqueadoTrue(),
                analiseRepository.count(),
                servicoIA.estaDisponivel(),
                LocalDateTime.now());
    }
}
