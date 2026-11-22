package br.ucsal.gestaofinanceira.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Com o CSRF desligado, o CORS é a barreira contra requisições de outros sites.
 * Origem curinga com cookie de sessão anularia essa barreira.
 */
class ConfiguracaoDeCorsTest {

    private static SegurancaConfig comOrigens(String origens) {
        SegurancaConfig config = new SegurancaConfig();
        ReflectionTestUtils.setField(config, "origensPermitidas", origens);
        return config;
    }

    @Test
    void recusaOrigemCuringa() {
        assertThrows(IllegalStateException.class, () -> comOrigens("*").corsConfigurationSource());
        assertThrows(IllegalStateException.class, () -> comOrigens("https://*").corsConfigurationSource());
        assertThrows(IllegalStateException.class,
                () -> comOrigens("https://app.example.com,*").corsConfigurationSource());
    }

    @Test
    void aceitaOrigensExatasEDesenvolvimentoLocal() {
        assertDoesNotThrow(() -> comOrigens("https://app.example.com").corsConfigurationSource());
        assertDoesNotThrow(() -> comOrigens("http://localhost:*,http://127.0.0.1:*").corsConfigurationSource());
    }
}
