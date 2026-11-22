package br.ucsal.gestaofinanceira;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.categoria.CategoriaRepository;
import br.ucsal.gestaofinanceira.conta.ContaFinanceira;
import br.ucsal.gestaofinanceira.conta.ContaFinanceiraRepository;
import br.ucsal.gestaofinanceira.conta.TipoConta;
import br.ucsal.gestaofinanceira.meta.MetaFinanceira;
import br.ucsal.gestaofinanceira.meta.MetaFinanceiraRepository;
import br.ucsal.gestaofinanceira.movimentacao.Movimentacao;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoRepository;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import br.ucsal.gestaofinanceira.usuario.PerfilUsuario;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import br.ucsal.gestaofinanceira.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RN-001: um usuário nunca acessa nem apaga dados de outro. O README promete isso; se
 * alguém remover o filtro por usuário de um repositório, isto falha.
 *
 * <p>Testa pela camada HTTP, que é onde a sessão e o filtro por usuário se encontram.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class PropriedadeDosDadosTest {

    private static final String SENHA = "senha12345";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContaFinanceiraRepository contaRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private MovimentacaoRepository movimentacaoRepository;
    @Autowired private MetaFinanceiraRepository metaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private ContaFinanceira contaDoDono;
    private MetaFinanceira metaDoDono;

    @BeforeEach
    void preparar() {
        Usuario dono = usuario("dono.propriedade@email.com");
        usuario("intruso.propriedade@email.com");

        // um teste por execução: limpa o que um run anterior deixou
        movimentacaoRepository.deleteAll();
        metaRepository.deleteAll();
        categoriaRepository.deleteAll();
        contaRepository.deleteAll();

        contaDoDono = contaRepository.save(new ContaFinanceira(dono, "Carteira", TipoConta.CARTEIRA, BigDecimal.ZERO));
        Categoria categoria = categoriaRepository.save(new Categoria(dono, "Mercado", TipoMovimentacao.DESPESA));
        movimentacaoRepository.save(new Movimentacao(contaDoDono, categoria, TipoMovimentacao.DESPESA,
                "Compras", new BigDecimal("50.00"), LocalDate.now()));
        metaDoDono = metaRepository.save(new MetaFinanceira(dono, "Viagem",
                new BigDecimal("1000"), LocalDate.now().plusMonths(6), LocalDate.now()));
    }

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            Usuario u = new Usuario("Fulano", email, passwordEncoder.encode(SENHA), PerfilUsuario.USUARIO);
            u.ativar();
            return usuarioRepository.save(u);
        });
    }

    /** Faz login de verdade e devolve a sessão autenticada. */
    private MockHttpSession autenticar(String email) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/login")
                        .param("username", email)
                        .param("password", SENHA))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    @Test
    void intrusoNaoVeNemApagaDadosDeOutroUsuario() throws Exception {
        MockHttpSession sessaoIntruso = autenticar("intruso.propriedade@email.com");

        // listagens do intruso vêm vazias
        mockMvc.perform(get("/api/v1/contas").session(sessaoIntruso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/v1/metas").session(sessaoIntruso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // apagar recurso alheio responde 404, sem revelar que ele existe
        mockMvc.perform(delete("/api/v1/contas/" + contaDoDono.getId()).session(sessaoIntruso))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/metas/" + metaDoDono.getId()).session(sessaoIntruso))
                .andExpect(status().isNotFound());

        // o recurso continua intacto para o dono
        mockMvc.perform(get("/api/v1/contas").session(autenticar("dono.propriedade@email.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void semSessaoARotaProtegidaResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/contas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioComumNaoAcessaAreaAdministrativa() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios").session(autenticar("intruso.propriedade@email.com")))
                .andExpect(status().isForbidden());
    }
}
