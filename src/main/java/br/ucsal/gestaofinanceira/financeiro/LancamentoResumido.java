package br.ucsal.gestaofinanceira.financeiro;

import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Projeção leve de uma movimentação (data, tipo e valor), sem descrição, conta ou categoria. */
public record LancamentoResumido(LocalDate data, TipoMovimentacao tipo, BigDecimal valor) {
}
