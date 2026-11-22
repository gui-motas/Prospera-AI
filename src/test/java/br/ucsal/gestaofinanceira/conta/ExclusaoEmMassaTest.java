package br.ucsal.gestaofinanceira.conta;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.categoria.CategoriaRepository;
import br.ucsal.gestaofinanceira.meta.MetaFinanceira;
import br.ucsal.gestaofinanceira.meta.MetaFinanceiraRepository;
import br.ucsal.gestaofinanceira.movimentacao.Movimentacao;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoRepository;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import br.ucsal.gestaofinanceira.planejamento.CenarioFinanceiro;
import br.ucsal.gestaofinanceira.planejamento.CenarioFinanceiroRepository;
import br.ucsal.gestaofinanceira.usuario.PerfilUsuario;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import br.ucsal.gestaofinanceira.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Os deletes de filhos eram métodos derivados (uma entidade por linha) e viraram
 * @Modifying @Query. Este teste garante que continuam apagando de fato e sem levar
 * registros de outro usuário junto.
 */
@SpringBootTest
@Transactional
class ExclusaoEmMassaTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContaFinanceiraRepository contaRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private MovimentacaoRepository movimentacaoRepository;
    @Autowired private MetaFinanceiraRepository metaRepository;
    @Autowired private CenarioFinanceiroRepository cenarioRepository;

    private Usuario usuario(String email) {
        Usuario u = new Usuario("Fulano", email, "hash", PerfilUsuario.USUARIO);
        u.ativar();
        return usuarioRepository.save(u);
    }

    @Test
    void deleteByContaIdApagaSomenteAsMovimentacoesDaConta() {
        Usuario dono = usuario("dono.massa@email.com");
        ContaFinanceira contaA = contaRepository.save(
                new ContaFinanceira(dono, "A", TipoConta.CARTEIRA, BigDecimal.ZERO));
        ContaFinanceira contaB = contaRepository.save(
                new ContaFinanceira(dono, "B", TipoConta.CARTEIRA, BigDecimal.ZERO));
        Categoria cat = categoriaRepository.save(new Categoria(dono, "Mercado", TipoMovimentacao.DESPESA));

        for (int i = 0; i < 5; i++) {
            movimentacaoRepository.save(new Movimentacao(contaA, cat, TipoMovimentacao.DESPESA,
                    "A" + i, BigDecimal.TEN, LocalDate.now()));
        }
        movimentacaoRepository.save(new Movimentacao(contaB, cat, TipoMovimentacao.DESPESA,
                "B", BigDecimal.TEN, LocalDate.now()));
        movimentacaoRepository.flush();

        movimentacaoRepository.deleteByContaId(contaA.getId());

        assertEquals(0, movimentacaoRepository.countByContaId(contaA.getId()));
        assertEquals(1, movimentacaoRepository.countByContaId(contaB.getId()));
    }

    @Test
    void deleteByMetaIdApagaCenariosDaMeta() {
        Usuario dono = usuario("dono.massa2@email.com");
        MetaFinanceira meta = metaRepository.save(new MetaFinanceira(dono, "Viagem",
                new BigDecimal("1000"), LocalDate.now().plusMonths(6), LocalDate.now()));
        cenarioRepository.save(new CenarioFinanceiro(meta, "Guardar 300", new BigDecimal("300"), LocalDate.now()));
        cenarioRepository.flush();

        cenarioRepository.deleteByMetaId(meta.getId());

        assertEquals(0, cenarioRepository.findByMetaIdOrderByCriadoEmDesc(meta.getId()).size());
    }
}
