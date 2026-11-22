package br.ucsal.gestaofinanceira.planejamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnaliseFinanceiraRepository extends JpaRepository<AnaliseFinanceira, UUID> {

    @EntityGraph(attributePaths = {"meta", "cenario"})
    List<AnaliseFinanceira> findTop10ByUsuarioIdOrderByDataGeracaoDesc(UUID usuarioId);

    void deleteByMetaId(UUID metaId);

    void deleteByCenarioId(UUID cenarioId);

    void deleteByCenarioMetaId(UUID metaId);
}
