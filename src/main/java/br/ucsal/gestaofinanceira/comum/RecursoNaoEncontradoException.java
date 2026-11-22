package br.ucsal.gestaofinanceira.comum;

/**
 * Recurso inexistente ou pertencente a outro usuário.
 * Os dois casos respondem igual (HTTP 404) para não revelar dados de terceiros (RN-001).
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
