package br.ucsal.gestaofinanceira.planejamento.ia;

import br.ucsal.gestaofinanceira.planejamento.IAIndisponivelException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Cliente para APIs no formato "chat completions" da OpenAI. Também atende provedores
 * compatíveis com esse formato (ex.: Google Gemini, Groq, OpenRouter, Ollama local),
 * bastando ajustar {@code IA_URL_BASE} e {@code IA_MODELO}.
 */
public class ClienteIAOpenAICompativel implements ClienteIA {

    private final RestClient restClient;
    private final String chaveApi;
    private final String modelo;

    public ClienteIAOpenAICompativel(RestClient restClient, String chaveApi, String modelo) {
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
                "messages", List.of(
                        Map.of("role", "system", "content", instrucoes),
                        Map.of("role", "user", "content", mensagem)));
        try {
            JsonNode resposta = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + chaveApi)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(JsonNode.class);
            return resposta == null ? "" : resposta.path("choices").path(0).path("message").path("content").asText("");
        } catch (RuntimeException e) {
            throw new IAIndisponivelException("Falha ao consultar a API compatível com OpenAI: " + e.getMessage(), e);
        }
    }
}
