package br.ucsal.gestaofinanceira.financeiro;

import java.math.BigDecimal;
import java.util.UUID;

/** Total movimentado em uma categoria no período (resultado de consulta agregada). */
public record GastoPorCategoria(UUID categoriaId, String categoria, BigDecimal total) {
}
