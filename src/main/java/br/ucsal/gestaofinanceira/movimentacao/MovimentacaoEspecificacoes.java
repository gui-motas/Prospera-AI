package br.ucsal.gestaofinanceira.movimentacao;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Consulta dinâmica usada por ServicoFinanceiro.filtrarMovimentacoes(usuario, filtros). */
public final class MovimentacaoEspecificacoes {

    private MovimentacaoEspecificacoes() {
    }

    public static Specification<Movimentacao> doUsuarioComFiltros(UUID usuarioId, FiltroMovimentacao filtro) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            // RN-001: sempre restringe às contas do próprio usuário
            condicoes.add(cb.equal(root.get("conta").get("usuario").get("id"), usuarioId));

            if (filtro.dataInicio() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("data"), filtro.dataInicio()));
            }
            if (filtro.dataFim() != null) {
                condicoes.add(cb.lessThanOrEqualTo(root.<LocalDate>get("data"), filtro.dataFim()));
            }
            if (filtro.categoriaId() != null) {
                condicoes.add(cb.equal(root.get("categoria").get("id"), filtro.categoriaId()));
            }
            if (filtro.contaId() != null) {
                condicoes.add(cb.equal(root.get("conta").get("id"), filtro.contaId()));
            }
            if (filtro.tipo() != null) {
                condicoes.add(cb.equal(root.get("tipo"), filtro.tipo()));
            }
            if (filtro.texto() != null && !filtro.texto().isBlank()) {
                String termo = "%" + filtro.texto().trim().toLowerCase(Locale.ROOT) + "%";
                condicoes.add(cb.like(cb.lower(root.<String>get("descricao")), termo));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };
    }
}
