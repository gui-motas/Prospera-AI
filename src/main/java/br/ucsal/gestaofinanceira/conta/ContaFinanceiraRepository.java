package br.ucsal.gestaofinanceira.conta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaFinanceiraRepository extends JpaRepository<ContaFinanceira, UUID> {

    List<ContaFinanceira> findByUsuarioIdOrderByNomeAsc(UUID usuarioId);

    Optional<ContaFinanceira> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByUsuarioIdAndNomeIgnoreCase(UUID usuarioId, String nome);
}
