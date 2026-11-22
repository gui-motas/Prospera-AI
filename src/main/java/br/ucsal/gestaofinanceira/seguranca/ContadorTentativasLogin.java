package br.ucsal.gestaofinanceira.seguranca;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contador de tentativas de login malsucedidas por e-mail, para barrar adivinhação de senha.
 *
 * <p>Em memória e por instância: reiniciar o backend zera os contadores. Para várias instâncias,
 * trocar por um contador compartilhado (Redis ou tabela).</p>
 */
@Component
public class ContadorTentativasLogin {

    static final int MAX_FALHAS = 10;
    static final Duration JANELA = Duration.ofMinutes(15);
    /** Limite de e-mails rastreados, para a memória não crescer com tentativas aleatórias. */
    private static final int MAX_REGISTROS = 10_000;

    private record Registro(int falhas, Instant expiraEm) {
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    /** Lança exceção quando o e-mail já estourou o limite na janela atual. */
    public void exigirPermissao(String email) {
        Registro registro = consultar(email);
        if (registro != null && registro.falhas() >= MAX_FALHAS) {
            throw new MuitasTentativasException();
        }
    }

    public void registrarFalha(String email) {
        podar();
        registros.compute(normalizar(email), (chave, atual) -> {
            Instant agora = Instant.now();
            if (atual == null || atual.expiraEm().isBefore(agora)) {
                return new Registro(1, agora.plus(JANELA));
            }
            return new Registro(atual.falhas() + 1, atual.expiraEm());
        });
    }

    public void registrarSucesso(String email) {
        registros.remove(normalizar(email));
    }

    private Registro consultar(String email) {
        Registro registro = registros.get(normalizar(email));
        if (registro == null) {
            return null;
        }
        if (registro.expiraEm().isBefore(Instant.now())) {
            registros.remove(normalizar(email));
            return null;
        }
        return registro;
    }

    /** Remove janelas vencidas; se ainda assim encher, descarta tudo (falha para o lado permissivo). */
    private void podar() {
        if (registros.size() < MAX_REGISTROS) {
            registros.entrySet().removeIf(e -> e.getValue().expiraEm().isBefore(Instant.now()));
        }
        if (registros.size() >= MAX_REGISTROS) {
            registros.clear();
        }
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
