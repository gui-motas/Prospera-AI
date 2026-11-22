package br.ucsal.gestaofinanceira.planejamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AnaliseFinanceiraRepository extends JpaRepository<AnaliseFinanceira, UUID> {

    @EntityGraph(attributePaths = {"meta", "cenario"})
    List<AnaliseFinanceira> findTop10ByUsuarioIdOrderByDataGeracaoDesc(UUID usuarioId);

    long countByUsuarioIdAndDataGeracaoGreaterThanEqual(UUID usuarioId, LocalDateTime desde);

    /**
     * delete em massa: os metodos derivados (deleteBy...) carregam uma entidade por linha.
     */
    @Modifying
    @Query("delete from AnaliseFinanceira a where a.meta.id = :metaId")
    void deleteByMetaId(@Param("metaId") UUID metaId);

    @Modifying
    @Query("delete from AnaliseFinanceira a where a.cenario.id = :cenarioId")
    void deleteByCenarioId(@Param("cenarioId") UUID cenarioId);

    @Modifying
    @Query("delete from AnaliseFinanceira a where a.cenario.meta.id = :metaId")
    void deleteByCenarioMetaId(@Param("metaId") UUID metaId);
}
