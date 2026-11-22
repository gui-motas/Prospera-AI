package br.ucsal.gestaofinanceira.orcamento;

/** Situação do consumo de um limite de gastos. */
public enum SituacaoOrcamento {
    /** Abaixo de 80% do limite. */
    DENTRO_DO_LIMITE,
    /** Entre 80% e 100% do limite: aviso antecipado ao usuário. */
    ALERTA,
    /** Acima do limite. */
    EXCEDIDO
}
