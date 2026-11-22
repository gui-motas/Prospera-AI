package br.ucsal.gestaofinanceira.planejamento.ia;

import br.ucsal.gestaofinanceira.planejamento.IAIndisponivelException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Cliente da API Messages da Anthropic (modelos Claude). */
public class ClienteIAAnthropic implements ClienteIA {

    private static final String VERSAO_API = "2023-06-01";
    private static final int MAX_TOKENS = 1024;

    private final RestClient restClient;
    private final String chaveApi;
    private final String modelo;

    public ClienteIAAnthropic(RestClient restClient, String chaveApi, String modelo) {
        this.restClient = restClient;
        this.chaveApi = chaveApi;
        this.modelo = modelo;
    }

    @Override
    public boolean estaConfigurado() {
        return true;
    }

    @Override
    public String gerarTexto(String instrucoes, String mensagem) {
        Map<String, Object> corpo = Map.of(
                "model", modelo,
                "max_tokens", MAX_TOKENS,
                "system", instrucoes,
                "messages", List.of(Map.of("role", "user", "content", mensagem)));
        try {
            JsonNode resposta = restClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", chaveApi)
                    .header("anthropic-version", VERSAO_API)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(JsonNode.class);

            StringBuilder texto = new StringBuilder();
            if (resposta != null) {
                for (JsonNode bloco : resposta.path("content")) {
                    if ("text".equals(bloco.path("type").asText())) {
                        texto.append(bloco.path("text").asText());
                    }
                }
            }
            return texto.toString();
        } catch (RuntimeException e) {
            throw new IAIndisponivelException("Falha ao consultar a API da Anthropic: " + e.getMessage(), e);
        }
    }
}
