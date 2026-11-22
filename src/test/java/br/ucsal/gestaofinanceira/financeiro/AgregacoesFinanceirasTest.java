package br.ucsal.gestaofinanceira.financeiro;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.categoria.CategoriaRepository;
import br.ucsal.gestaofinanceira.conta.ContaFinanceira;
import br.ucsal.gestaofinanceira.conta.ContaFinanceiraRepository;
import br.ucsal.gestaofinanceira.conta.TipoConta;
import br.ucsal.gestaofinanceira.movimentacao.Movimentacao;
import br.ucsal.gestaofinanceira.movimentacao.MovimentacaoRepository;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import br.ucsal.gestaofinanceira.usuario.PerfilUsuario;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import br.ucsal.gestaofinanceira.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * As agregações em JPQL (year/month + group by) precisam rodar de fato no banco;
 * um erro de sintaxe só aparece em execução, não na compilação.
 */
@SpringBootTest
@Transactional
class AgregacoesFinanceirasTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ContaFinanceiraRepository contaRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private MovimentacaoRepository movimentacaoRepository;
    @Autowired private ServicoFinanceiro servicoFinanceiro;

    private Usuario usuario;
    private ContaFinanceira conta;
    private Categoria despesa;
    private Categoria receita;

    @BeforeEach
    void preparar() {
        usuario = usuarioRepository.save(
                new Usuario("Ana", "ana.agregacoes@email.com", "hash", PerfilUsuario.USUARIO));
        usuario.ativar();
        usuarioRepository.save(usuario);
        conta = contaRepository.save(new ContaFinanceira(usuario, "Carteira", TipoConta.CARTEIRA, BigDecimal.ZERO));
        despesa = categoriaRepository.save(new Categoria(usuario, "Mercado", TipoMovimentacao.DESPESA));
        receita = categoriaRepository.save(new Categoria(usuario, "Salário", TipoMovimentacao.RECEITA));
    }

    private void lancar(TipoMovimentacao tipo, String valor, LocalDate data) {
        Categoria categoria = tipo == TipoMovimentacao.DESPESA ? despesa : receita;
        movimentacaoRepository.save(new Movimentacao(conta, categoria, tipo, "lançamento",
                new BigDecimal(valor), data));
    }

    @Test
    void evolucaoMensalSomaPorMesNoBanco() {
        lancar(TipoMovimentacao.RECEITA, "1000.00", LocalDate.of(2026, 1, 10));
        lancar(TipoMovimentacao.RECEITA, "500.00", LocalDate.of(2026, 1, 20));
        lancar(TipoMovimentacao.DESPESA, "300.00", LocalDate.of(2026, 2, 5));

        List<ResumoFinanceiro.ResumoMensal> meses =
                servicoFinanceiro.calcularEvolucaoMensal(usuario.getId(), YearMonth.of(2026, 2), 2);

        assertEquals(2, meses.size());
        assertEquals(new BigDecimal("1500.00"), meses.get(0).receitas());
        assertEquals(BigDecimal.ZERO, meses.get(0).despesas());
        assertEquals(new BigDecimal("300.00"), meses.get(1).despesas());
    }

    @Test
    void evolucaoMensalTrazMesesSemMovimentoZerados() {
        lancar(TipoMovimentacao.RECEITA, "100.00", LocalDate.of(2026, 1, 10));

        List<ResumoFinanceiro.ResumoMensal> meses =
                servicoFinanceiro.calcularEvolucaoMensal(usuario.getId(), YearMonth.of(2026, 3), 3);

        assertEquals(3, meses.size());
        assertEquals("2026-02", meses.get(1).mes());
        assertEquals(BigDecimal.ZERO, meses.get(1).receitas());
        assertEquals(BigDecimal.ZERO, meses.get(1).despesas());
    }

    @Test
    void contagemPorContaAgrupaNoBanco() {
        lancar(TipoMovimentacao.DESPESA, "10.00", LocalDate.of(2026, 1, 1));
        lancar(TipoMovimentacao.DESPESA, "10.00", LocalDate.of(2026, 1, 2));

        List<Object[]> contagens = movimentacaoRepository.contarPorConta(usuario.getId());

        assertEquals(1, contagens.size());
        assertEquals(conta.getId(), contagens.get(0)[0]);
        assertEquals(2L, contagens.get(0)[1]);
    }

    @Test
    void gastoPorCategoriaEMesAgrupaNoBanco() {
        lancar(TipoMovimentacao.DESPESA, "100.00", LocalDate.of(2026, 1, 5));
        lancar(TipoMovimentacao.DESPESA, "50.00", LocalDate.of(2026, 1, 25));
        lancar(TipoMovimentacao.DESPESA, "70.00", LocalDate.of(2026, 2, 5));

        List<Object[]> totais = movimentacaoRepository.somarPorCategoriaEMes(usuario.getId(),
                TipoMovimentacao.DESPESA, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));

        assertEquals(2, totais.size());
        assertEquals(new BigDecimal("150.00"), totais.stream()
                .filter(l -> l[1].equals(2026) && l[2].equals(1))
                .map(l -> (BigDecimal) l[3]).findFirst().orElseThrow());
    }
}
