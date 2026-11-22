package br.ucsal.gestaofinanceira.cadastro;

import br.ucsal.gestaofinanceira.cadastro.CadastroDtos.CadastroRequest;
import br.ucsal.gestaofinanceira.cadastro.CadastroDtos.CadastroResponse;
import br.ucsal.gestaofinanceira.categoria.CategoriaService;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.conta.ContaFinanceira;
import br.ucsal.gestaofinanceira.conta.ContaFinanceiraRepository;
import br.ucsal.gestaofinanceira.conta.TipoConta;
import br.ucsal.gestaofinanceira.seguranca.ValidadorSenha;
import br.ucsal.gestaofinanceira.usuario.PerfilUsuario;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import br.ucsal.gestaofinanceira.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** RF-001 – Cadastro de usuário. */
@Service
public class CadastroService {

    private static final long HORAS_VALIDADE_TOKEN = 24;

    private final UsuarioRepository usuarioRepository;
    private final TokenConfirmacaoRepository tokenRepository;
    private final ContaFinanceiraRepository contaRepository;
    private final CategoriaService categoriaService;
    private final PasswordEncoder passwordEncoder;
    private final ServicoEmail servicoEmail;
    private final boolean exigirConfirmacaoEmail;
    private final String urlFrontend;

    public CadastroService(UsuarioRepository usuarioRepository,
                           TokenConfirmacaoRepository tokenRepository,
                           ContaFinanceiraRepository contaRepository,
                           CategoriaService categoriaService,
                           PasswordEncoder passwordEncoder,
                           ServicoEmail servicoEmail,
                           @Value("${app.cadastro.confirmacao-email:false}") boolean exigirConfirmacaoEmail,
                           @Value("${app.url-frontend}") String urlFrontend) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.contaRepository = contaRepository;
        this.categoriaService = categoriaService;
        this.passwordEncoder = passwordEncoder;
        this.servicoEmail = servicoEmail;
        this.exigirConfirmacaoEmail = exigirConfirmacaoEmail;
        this.urlFrontend = urlFrontend.replaceAll("/+$", "");
    }

    @Transactional
    public CadastroResponse cadastrar(CadastroRequest req) {
        String email = Usuario.normalizarEmail(req.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Este e-mail já está cadastrado.");
        }
        ValidadorSenha.validar(req.senha());

        Usuario usuario = new Usuario(req.nome(), email, passwordEncoder.encode(req.senha()), PerfilUsuario.USUARIO);
        if (!exigirConfirmacaoEmail) {
            usuario.ativar();
        }
        usuario = usuarioRepository.save(usuario);

        // Dados iniciais para o usuário começar a registrar movimentações
        categoriaService.criarCategoriasPadrao(usuario);
        contaRepository.save(new ContaFinanceira(usuario, "Carteira", TipoConta.CARTEIRA, BigDecimal.ZERO));

        if (!exigirConfirmacaoEmail) {
            return new CadastroResponse("Cadastro realizado! Você já pode entrar.", false);
        }

        LocalDateTime agora = LocalDateTime.now();
        String token = UUID.randomUUID().toString();
        tokenRepository.save(new TokenConfirmacao(token, usuario, agora, agora.plusHours(HORAS_VALIDADE_TOKEN)));
        // Só envia depois do commit: antes, o e-mail podia chegar com o token ainda invisível
        // para outra conexão, e um rollback deixava um link morto na caixa do usuário.
        enviarAposCommit(email, usuario.getNome(), urlFrontend + "/confirmar-conta?token=" + token);
        return new CadastroResponse("Cadastro realizado! Enviamos um link de confirmação para o seu e-mail.", true);
    }

    private void enviarAposCommit(String email, String nome, String link) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    servicoEmail.enviarConfirmacao(email, nome, link);
                }
            });
        } else {
            servicoEmail.enviarConfirmacao(email, nome, link);
        }
    }

    @Transactional
    public void confirmar(String token) {
        TokenConfirmacao confirmacao = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RegraNegocioException("Link de confirmação inválido."));
        if (confirmacao.jaConfirmado()) {
            return;
        }
        LocalDateTime agora = LocalDateTime.now();
        if (confirmacao.estaExpirado(agora)) {
            throw new RegraNegocioException("Este link de confirmação expirou. Faça um novo cadastro.");
        }
        confirmacao.confirmar(agora);
    }
}
