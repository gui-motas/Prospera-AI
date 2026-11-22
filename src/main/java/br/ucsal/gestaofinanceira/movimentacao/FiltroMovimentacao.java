package br.ucsal.gestaofinanceira.movimentacao;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtros do histórico de movimentações: período, categoria e tipo (escopo 1.2),
 * além de conta e texto da descrição. Campos nulos são ignorados.
 */
public record FiltroMovimentacao(
        LocalDate dataInicio,
        LocalDate dataFim,
        UUID categoriaId,
        UUID contaId,
        TipoMovimentacao tipo,
        String texto) {
}
