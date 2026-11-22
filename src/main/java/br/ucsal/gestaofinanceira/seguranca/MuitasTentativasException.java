package br.ucsal.gestaofinanceira.seguranca;

/** Limite de tentativas de login malsucedidas atingido (HTTP 429). */
public class MuitasTentativasException extends RuntimeException {

    public MuitasTentativasException() {
        super("Muitas tentativas de login. Tente novamente em alguns minutos.");
    }
}
