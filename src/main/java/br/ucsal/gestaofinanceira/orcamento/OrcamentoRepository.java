package br.ucsal.gestaofinanceira.orcamento;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {

    @EntityGraph(attributePaths = "categoria")
    List<Orcamento> findByCategoriaUsuarioIdOrderByInicioPeriodoDesc(UUID usuarioId);

    @EntityGraph(attributePaths = "categoria")
    Optional<Orcamento> findByIdAndCategoriaUsuarioId(UUID id, UUID usuarioId);

    List<Orcamento> findByCategoriaId(UUID categoriaId);

    boolean existsByCategoriaId(UUID categoriaId);
}
