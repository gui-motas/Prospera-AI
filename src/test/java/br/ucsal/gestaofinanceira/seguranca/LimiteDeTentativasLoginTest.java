package br.ucsal.gestaofinanceira.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O endpoint de login não pode ser usado para adivinhar senha indefinidamente. */
@SpringBootTest
@AutoConfigureMockMvc
class LimiteDeTentativasLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContadorTentativasLogin contador;

    @Test
    void aposAsFalhasOLoginResponde429SemConferirASenha() throws Exception {
        String email = "alvo@email.com";

        for (int i = 0; i < ContadorTentativasLogin.MAX_FALHAS; i++) {
            mockMvc.perform(post("/api/v1/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", email)
                            .param("password", "errada" + i))
                    .andExpect(status().isUnauthorized());
        }

        // o filtro barra antes da verificação de senha
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", email)
                        .param("password", "qualquer"))
                .andExpect(status().isTooManyRequests());

        assertThrows(MuitasTentativasException.class, () -> contador.exigirPermissao(email));
    }
}
