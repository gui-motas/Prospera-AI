package br.ucsal.gestaofinanceira.planejamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CenarioFinanceiroRepository extends JpaRepository<CenarioFinanceiro, UUID> {

    @EntityGraph(attributePaths = "meta")
    List<CenarioFinanceiro> findByMetaIdOrderByCriadoEmDesc(UUID metaId);

    @EntityGraph(attributePaths = "meta")
    Optional<CenarioFinanceiro> findByIdAndMetaUsuarioId(UUID id, UUID usuarioId);

    void deleteByMetaId(UUID metaId);
}
