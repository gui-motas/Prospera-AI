package br.ucsal.gestaofinanceira.meta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetaFinanceiraRepository extends JpaRepository<MetaFinanceira, UUID> {

    List<MetaFinanceira> findByUsuarioIdOrderByPrazoAsc(UUID usuarioId);

    Optional<MetaFinanceira> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
