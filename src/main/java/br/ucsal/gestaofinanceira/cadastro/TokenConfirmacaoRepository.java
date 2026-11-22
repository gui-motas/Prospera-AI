package br.ucsal.gestaofinanceira.cadastro;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenConfirmacaoRepository extends JpaRepository<TokenConfirmacao, UUID> {

    @EntityGraph(attributePaths = "usuario")
    Optional<TokenConfirmacao> findByToken(String token);
}
