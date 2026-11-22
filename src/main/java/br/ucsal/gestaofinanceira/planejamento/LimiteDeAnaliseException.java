package br.ucsal.gestaofinanceira.planejamento;

/**
 * Limite diário de análises com IA atingido. Cada análise é uma chamada paga à API externa,
 * então o limite protege o custo e o crescimento da tabela de análises.
 */
public class LimiteDeAnaliseException extends RuntimeException {

    public LimiteDeAnaliseException(int limitePorDia) {
        super("Você atingiu o limite de " + limitePorDia
                + " análises com IA nas últimas 24 horas. Tente novamente mais tarde.");
    }
}
