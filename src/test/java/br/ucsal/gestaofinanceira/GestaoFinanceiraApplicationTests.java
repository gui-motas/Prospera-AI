package br.ucsal.gestaofinanceira;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Garante que o contexto Spring sobe (banco H2 em memória e IA não configurada). */
@SpringBootTest
class GestaoFinanceiraApplicationTests {

    @Test
    void contextoCarrega() {
        // falha se alguma configuração ou dependência estiver incorreta
    }
}
