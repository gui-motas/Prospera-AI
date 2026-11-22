package br.ucsal.gestaofinanceira.planejamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CenarioFinanceiroRepository extends JpaRepository<CenarioFinanceiro, UUID> {

    @EntityGraph(attributePaths = "meta")
    List<CenarioFinanceiro> findByMetaIdOrderByCriadoEmDesc(UUID metaId);

    @EntityGraph(attributePaths = "meta")
    Optional<CenarioFinanceiro> findByIdAndMetaUsuarioId(UUID id, UUID usuarioId);

    /** delete em massa: o derivado deleteByMetaId carrega uma entidade por linha. */
    @Modifying
    @Query("delete from CenarioFinanceiro c where c.meta.id = :metaId")
    void deleteByMetaId(@Param("metaId") UUID metaId);
}
