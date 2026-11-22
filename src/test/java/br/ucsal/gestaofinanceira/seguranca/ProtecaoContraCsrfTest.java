package br.ucsal.gestaofinanceira.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O CSRF está desligado porque a SPA usa JSON e cookie de sessão. A proteção efetiva contra
 * requisições forjadas de outro site é o filtro de CORS (origens permitidas vêm de
 * APP_CORS_ALLOWED_ORIGINS). Estes testes travam esse comportamento: se alguém liberar
 * origens curinga com credenciais, um site malicioso passa a poder alterar dados do usuário.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProtecaoContraCsrfTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void formForjadoDeOutraOrigemEhRejeitadoPeloCors() throws Exception {
        // formulário HTML em site malicioso: POST simples, sem preflight
        mockMvc.perform(post("/api/v1/movimentacoes")
                        .header("Origin", "https://site-malicioso.example")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("descricao=x"))
                .andExpect(status().isForbidden());
    }

    @Test
    void jsonForjadoDeOutraOrigemEhRejeitadoPeloCors() throws Exception {
        mockMvc.perform(post("/api/v1/movimentacoes")
                        .header("Origin", "https://site-malicioso.example")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    /** Controle: a mesma requisição, de origem permitida, chega à autorização (401 sem sessão). */
    @Test
    void origemPermitidaNaoEhBloqueadaPeloCors() throws Exception {
        mockMvc.perform(post("/api/v1/movimentacoes")
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
