package br.ucsal.gestaofinanceira.planejamento;

/** Tipos de análise gerados pelo ServicoIA. */
public enum TipoAnalise {
    /** ServicoIA.gerarAnalise(usuario): padrões de consumo e capacidade de economia. */
    SITUACAO_FINANCEIRA,
    /** ServicoIA.planejarMeta(meta): viabilidade e sugestões para uma meta. */
    PLANEJAMENTO_META,
    /** ServicoIA.interpretarCenario(cenario): explicação de um cenário simulado. */
    INTERPRETACAO_CENARIO
}
