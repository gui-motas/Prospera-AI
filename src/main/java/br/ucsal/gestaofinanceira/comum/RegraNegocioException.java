package br.ucsal.gestaofinanceira.comum;

/**
 * Violação de uma regra de negócio da ERS (RN-001 a RN-012).
 * Respondida com HTTP 422 e uma mensagem compreensível para o usuário.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
