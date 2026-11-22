package br.ucsal.gestaofinanceira.seguranca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContadorTentativasLoginTest {

    @Test
    void bloqueiaDepoisDoLimiteDeFalhas() {
        ContadorTentativasLogin contador = new ContadorTentativasLogin();

        for (int i = 0; i < ContadorTentativasLogin.MAX_FALHAS; i++) {
            assertDoesNotThrow(() -> contador.exigirPermissao("alvo@email.com"));
            contador.registrarFalha("alvo@email.com");
        }

        assertThrows(MuitasTentativasException.class, () -> contador.exigirPermissao("alvo@email.com"));
    }

    @Test
    void loginComSucessoZeraAsFalhas() {
        ContadorTentativasLogin contador = new ContadorTentativasLogin();
        for (int i = 0; i < ContadorTentativasLogin.MAX_FALHAS; i++) {
            contador.registrarFalha("ana@email.com");
        }
        contador.registrarSucesso("ana@email.com");

        assertDoesNotThrow(() -> contador.exigirPermissao("ana@email.com"));
    }

    @Test
    void eMailDiferenteNaoEhAfetado() {
        ContadorTentativasLogin contador = new ContadorTentativasLogin();
        for (int i = 0; i < ContadorTentativasLogin.MAX_FALHAS; i++) {
            contador.registrarFalha("alvo@email.com");
        }
        // normalização: maiúsculas e espaços são o mesmo e-mail
        assertThrows(MuitasTentativasException.class, () -> contador.exigirPermissao("  ALVO@email.com "));
        assertDoesNotThrow(() -> contador.exigirPermissao("outra@email.com"));
    }
}
