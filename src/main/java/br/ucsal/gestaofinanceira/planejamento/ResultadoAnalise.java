package br.ucsal.gestaofinanceira.planejamento;

import java.util.Map;

/**
 * Análise gerada e os dados exatamente como foram enviados à API de IA,
 * exibidos ao usuário por transparência (RN-009, LGPD).
 */
public record ResultadoAnalise(AnaliseFinanceira analise, Map<String, Object> dadosEnviados) {
}
