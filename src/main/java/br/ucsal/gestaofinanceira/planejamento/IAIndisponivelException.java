package br.ucsal.gestaofinanceira.planejamento;

/**
 * RNF-09 – Falha da API de Inteligência Artificial.
 * O usuário recebe uma mensagem clara (HTTP 503) e as demais funcionalidades continuam disponíveis.
 */
public class IAIndisponivelException extends RuntimeException {

    public static final String MENSAGEM_USUARIO =
            "O recurso de planejamento inteligente está temporariamente indisponível. "
                    + "As demais funcionalidades da plataforma continuam funcionando normalmente.";

    private final String motivoTecnico;

    public IAIndisponivelException(String motivoTecnico) {
        super(MENSAGEM_USUARIO);
        this.motivoTecnico = motivoTecnico;
    }

    public IAIndisponivelException(String motivoTecnico, Throwable causa) {
        super(MENSAGEM_USUARIO, causa);
        this.motivoTecnico = motivoTecnico;
    }

    public String getMotivoTecnico() {
        return motivoTecnico;
    }
}
