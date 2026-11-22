package br.ucsal.gestaofinanceira.movimentacao;

import br.ucsal.gestaofinanceira.financeiro.GastoPorCategoria;
import br.ucsal.gestaofinanceira.financeiro.LancamentoResumido;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, UUID>, JpaSpecificationExecutor<Movimentacao> {

    @Override
    @EntityGraph(attributePaths = {"conta", "categoria"})
    List<Movimentacao> findAll(Specification<Movimentacao> spec, Sort sort);

    @EntityGraph(attributePaths = {"conta", "categoria"})
    Optional<Movimentacao> findByIdAndContaUsuarioId(UUID id, UUID usuarioId);

    boolean existsByCategoriaId(UUID categoriaId);

    boolean existsByCategoriaIdAndTipoNot(UUID categoriaId, TipoMovimentacao tipo);

    void deleteByContaId(UUID contaId);

    long countByContaId(UUID contaId);

    /** Linhas: [contaId (UUID), tipo (TipoMovimentacao), soma (BigDecimal)]. Base do cálculo de saldo (RN-004). */
    @Query("select m.conta.id, m.tipo, sum(m.valor) from Movimentacao m "
            + "where m.conta.usuario.id = :usuarioId group by m.conta.id, m.tipo")
    List<Object[]> somarPorContaETipo(@Param("usuarioId") UUID usuarioId);

    @Query("select sum(m.valor) from Movimentacao m where m.conta.usuario.id = :usuarioId "
            + "and m.tipo = :tipo and m.data between :inicio and :fim")
    BigDecimal somarPorTipoNoPeriodo(@Param("usuarioId") UUID usuarioId,
                                     @Param("tipo") TipoMovimentacao tipo,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);

    @Query("select new br.ucsal.gestaofinanceira.financeiro.GastoPorCategoria(c.id, c.nome, sum(m.valor)) "
            + "from Movimentacao m join m.categoria c "
            + "where m.conta.usuario.id = :usuarioId and m.tipo = :tipo and m.data between :inicio and :fim "
            + "group by c.id, c.nome order by sum(m.valor) desc")
    List<GastoPorCategoria> agruparPorCategoria(@Param("usuarioId") UUID usuarioId,
                                                @Param("tipo") TipoMovimentacao tipo,
                                                @Param("inicio") LocalDate inicio,
                                                @Param("fim") LocalDate fim);

    /** Total gasto em uma categoria no período de um orçamento (Orcamento.calcularTotalGasto). */
    @Query("select sum(m.valor) from Movimentacao m where m.categoria.id = :categoriaId "
            + "and m.tipo = :tipo and m.data between :inicio and :fim")
    BigDecimal somarPorCategoriaNoPeriodo(@Param("categoriaId") UUID categoriaId,
                                          @Param("tipo") TipoMovimentacao tipo,
                                          @Param("inicio") LocalDate inicio,
                                          @Param("fim") LocalDate fim);

    @Query("select new br.ucsal.gestaofinanceira.financeiro.LancamentoResumido(m.data, m.tipo, m.valor) "
            + "from Movimentacao m where m.conta.usuario.id = :usuarioId and m.data between :inicio and :fim")
    List<LancamentoResumido> listarLancamentosResumidos(@Param("usuarioId") UUID usuarioId,
                                                        @Param("inicio") LocalDate inicio,
                                                        @Param("fim") LocalDate fim);
}
