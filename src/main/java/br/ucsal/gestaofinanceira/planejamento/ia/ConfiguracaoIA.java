package br.ucsal.gestaofinanceira.planejamento.ia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Locale;

/**
 * Escolhe o cliente da API de IA a partir das variáveis de ambiente:
 * IA_PROVEDOR (anthropic | openai), IA_CHAVE_API, IA_MODELO, IA_URL_BASE e IA_TIMEOUT_SEGUNDOS.
 * Sem chave configurada, a plataforma funciona normalmente e o recurso inteligente
 * aparece como indisponível (RNF-09).
 */
@Configuration
public class ConfiguracaoIA {

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguracaoIA.class);

    private static final String URL_ANTHROPIC = "https://api.anthropic.com";
    private static final String MODELO_ANTHROPIC = "claude-haiku-5-5";
    private static final String URL_OPENAI = "https://api.openai.com/v1";

    @Bean
    public ClienteIA clienteIA(@Value("${app.ia.provedor:anthropic}") String provedor,
                               @Value("${app.ia.chave-api:}") String chaveApi,
                               @Value("${app.ia.modelo:}") String modelo,
                               @Value("${app.ia.url-base:}") String urlBase,
                               @Value("${app.ia.timeout-segundos:30}") int timeoutSegundos) {
        if (chaveApi == null || chaveApi.isBlank()) {
            LOG.warn("IA_CHAVE_API não configurada: o planejamento inteligente ficará indisponível (RNF-09).");
            return new ClienteIAIndisponivel("Chave da API de IA não configurada.");
        }

        String provedorNormalizado = provedor == null ? "" : provedor.trim().toLowerCase(Locale.ROOT);
        switch (provedorNormalizado) {
            case "anthropic" -> {
                String url = vazio(urlBase) ? URL_ANTHROPIC : urlBase.trim();
                String nomeModelo = vazio(modelo) ? MODELO_ANTHROPIC : modelo.trim();
                LOG.info("API de IA: Anthropic, modelo {}", nomeModelo);
                return new ClienteIAAnthropic(criarRestClient(url, timeoutSegundos), chaveApi.trim(), nomeModelo);
            }
            case "openai" -> {
                if (vazio(modelo)) {
                    LOG.warn("IA_MODELO não informado para o provedor openai: planejamento inteligente indisponível.");
                    return new ClienteIAIndisponivel("Modelo da API de IA não configurado.");
                }
                String url = vazio(urlBase) ? URL_OPENAI : urlBase.trim();
                LOG.info("API de IA: compatível com OpenAI em {}, modelo {}", url, modelo.trim());
                return new ClienteIAOpenAICompativel(criarRestClient(url, timeoutSegundos), chaveApi.trim(), modelo.trim());
            }
            default -> {
                LOG.warn("IA_PROVEDOR desconhecido: '{}'. Use 'anthropic' ou 'openai'.", provedor);
                return new ClienteIAIndisponivel("Provedor de IA desconhecido.");
            }
        }
    }

    private static RestClient criarRestClient(String urlBase, int timeoutSegundos) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(10_000);
        fabrica.setReadTimeout(Math.max(5, timeoutSegundos) * 1000);
        return RestClient.builder()
                .baseUrl(urlBase.replaceAll("/+$", ""))
                .requestFactory(fabrica)
                .build();
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
