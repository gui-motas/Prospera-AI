package br.ucsal.gestaofinanceira.planejamento.ia;

import br.ucsal.gestaofinanceira.planejamento.IAIndisponivelException;

/**
 * Porta de comunicação com a API externa de Inteligência Artificial (stakeholder 1.3.3).
 * A implementação é escolhida pela propriedade {@code app.ia.provedor}.
 */
public interface ClienteIA {

    /** @return {@code true} se há chave e modelo configurados. */
    boolean estaConfigurado();

    /**
     * Envia as instruções e a mensagem à API e devolve o texto gerado.
     *
     * @throws IAIndisponivelException se a API não estiver configurada, falhar ou demorar demais (RNF-09)
     */
    String gerarTexto(String instrucoes, String mensagem);
}
