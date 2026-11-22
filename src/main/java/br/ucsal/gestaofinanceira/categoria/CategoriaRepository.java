package br.ucsal.gestaofinanceira.categoria;

import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    List<Categoria> findByUsuarioIdOrderByTipoAscNomeAsc(UUID usuarioId);

    Optional<Categoria> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByUsuarioIdAndNomeIgnoreCaseAndTipo(UUID usuarioId, String nome, TipoMovimentacao tipo);

    boolean existsByUsuarioIdAndNomeIgnoreCaseAndTipoAndIdNot(UUID usuarioId, String nome, TipoMovimentacao tipo, UUID id);
}
