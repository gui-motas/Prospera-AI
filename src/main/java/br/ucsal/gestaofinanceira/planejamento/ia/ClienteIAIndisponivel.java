package br.ucsal.gestaofinanceira.planejamento.ia;

import br.ucsal.gestaofinanceira.planejamento.IAIndisponivelException;

/** Usado quando a API de IA não está configurada: o recurso inteligente fica indisponível (RNF-09). */
public class ClienteIAIndisponivel implements ClienteIA {

    private final String motivo;

    public ClienteIAIndisponivel(String motivo) {
        this.motivo = motivo;
    }

    @Override
    public boolean estaConfigurado() {
        return false;
    }

    @Override
    public String gerarTexto(String instrucoes, String mensagem) {
        throw new IAIndisponivelException(motivo);
    }
}
