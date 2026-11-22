package br.ucsal.gestaofinanceira;

import br.ucsal.gestaofinanceira.categoria.Categoria;
import br.ucsal.gestaofinanceira.comum.RegraNegocioException;
import br.ucsal.gestaofinanceira.conta.ContaFinanceira;
import br.ucsal.gestaofinanceira.conta.TipoConta;
import br.ucsal.gestaofinanceira.meta.MetaFinanceira;
import br.ucsal.gestaofinanceira.meta.StatusMeta;
import br.ucsal.gestaofinanceira.movimentacao.Movimentacao;
import br.ucsal.gestaofinanceira.movimentacao.TipoMovimentacao;
import br.ucsal.gestaofinanceira.orcamento.Orcamento;
import br.ucsal.gestaofinanceira.orcamento.SituacaoOrcamento;
import br.ucsal.gestaofinanceira.planejamento.CenarioFinanceiro;
import br.ucsal.gestaofinanceira.planejamento.ComparacaoCenario;
import br.ucsal.gestaofinanceira.usuario.PerfilUsuario;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testes unitários das regras de negócio implementadas nas classes de domínio. */
class RegrasDeNegocioTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 16);

    private final Usuario usuario = new Usuario("Ana", "ana@email.com", "hash", PerfilUsuario.USUARIO);
    private final ContaFinanceira conta = new ContaFinanceira(usuario, "Carteira", TipoConta.CARTEIRA, BigDecimal.ZERO);
    private final Categoria alimentacao = new Categoria(usuario, "Alimentação", TipoMovimentacao.DESPESA);
    private final Categoria salario = new Categoria(usuario, "Salário", TipoMovimentacao.RECEITA);

    @Test
    void rn002_movimentacaoComValorZeroOuNegativoEhRejeitada() {
        assertThrows(RegraNegocioException.class, () -> new Movimentacao(conta, alimentacao,
                TipoMovimentacao.DESPESA, "Mercado", BigDecimal.ZERO, HOJE));
        assertThrows(RegraNegocioException.class, () -> new Movimentacao(conta, alimentacao,
                TipoMovimentacao.DESPESA, "Mercado", new BigDecimal("-10"), HOJE));
    }

    @Test
    void rn003_categoriaPrecisaSerCompativelComOTipoDaMovimentacao() {
        assertThrows(RegraNegocioException.class, () -> new Movimentacao(conta, salario,
                TipoMovimentacao.DESPESA, "Mercado", BigDecimal.TEN, HOJE));
        assertTrue(alimentacao.validarCompatibilidade(TipoMovimentacao.DESPESA));
        assertFalse(alimentacao.validarCompatibilidade(TipoMovimentacao.RECEITA));
    }

    @Test
    void rn004_saldoDaContaConsideraReceitasEDespesas() {
        ContaFinanceira corrente = new ContaFinanceira(usuario, "Banco", TipoConta.CONTA_CORRENTE, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("350.00"), corrente.calcularSaldo(new BigDecimal("250.00")));
        assertEquals(new BigDecimal("100.00"), corrente.calcularSaldo(null));
    }

    @Test
    void rn005_limiteDeOrcamentoPrecisaSerPositivoComPeriodoValido() {
        LocalDate inicio = HOJE.withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1).minusDays(1);
        assertThrows(RegraNegocioException.class, () -> new Orcamento(alimentacao, BigDecimal.ZERO, inicio, fim));
        assertThrows(RegraNegocioException.class, () -> new Orcamento(alimentacao, BigDecimal.TEN, fim, inicio));
        assertThrows(RegraNegocioException.class, () -> new Orcamento(salario, BigDecimal.TEN, inicio, fim));
    }

    @Test
    void orcamentoIndicaAlertaEExcessoDoLimite() {
        Orcamento orcamento = new Orcamento(alimentacao, new BigDecimal("1000"),
                HOJE.withDayOfMonth(1), HOJE.withDayOfMonth(28));
        assertEquals(SituacaoOrcamento.DENTRO_DO_LIMITE, orcamento.avaliarSituacao(new BigDecimal("500")));
        assertEquals(SituacaoOrcamento.ALERTA, orcamento.avaliarSituacao(new BigDecimal("850")));
        assertEquals(SituacaoOrcamento.EXCEDIDO, orcamento.avaliarSituacao(new BigDecimal("1200")));
        assertEquals(new BigDecimal("-200"), orcamento.calcularSaldoRestante(new BigDecimal("1200")));
    }

    @Test
    void rn006_rn007_metaExigeNomeValorPositivoEPrazo() {
        LocalDate prazo = HOJE.plusMonths(12);
        assertThrows(RegraNegocioException.class, () -> new MetaFinanceira(usuario, " ", BigDecimal.TEN, prazo, HOJE));
        assertThrows(RegraNegocioException.class, () -> new MetaFinanceira(usuario, "Viagem", BigDecimal.ZERO, prazo, HOJE));
        assertThrows(RegraNegocioException.class, () -> new MetaFinanceira(usuario, "Viagem", BigDecimal.TEN, null, HOJE));
    }

    @Test
    void rf09_progressoEStatusDaMeta() {
        MetaFinanceira meta = new MetaFinanceira(usuario, "Reserva", new BigDecimal("1000"), HOJE.plusMonths(10), HOJE);
        meta.atualizarValorAcumulado(new BigDecimal("250"), HOJE);
        assertEquals(new BigDecimal("25.0"), meta.calcularProgresso());
        assertEquals(new BigDecimal("750"), meta.calcularValorRestante());
        assertEquals(StatusMeta.EM_ANDAMENTO, meta.getStatus());

        assertThrows(RegraNegocioException.class, () -> meta.atualizarValorAcumulado(new BigDecimal("-300"), HOJE));

        meta.atualizarValorAcumulado(new BigDecimal("750"), HOJE);
        assertEquals(StatusMeta.CONCLUIDA, meta.getStatus());
        assertEquals(new BigDecimal("100.0"), meta.calcularProgresso());
    }

    @Test
    void cenarioProjetaPrazoEComparaComSituacaoAtual() {
        MetaFinanceira meta = new MetaFinanceira(usuario, "Viagem", new BigDecimal("6000"), HOJE.plusMonths(12), HOJE);
        CenarioFinanceiro cenario = new CenarioFinanceiro(meta, "Guardar R$ 1.000 por mês", new BigDecimal("1000"), HOJE);

        assertEquals(HOJE.plusMonths(6), cenario.getPrazoProjetado());

        ComparacaoCenario comparacao = cenario.compararComSituacaoAtual(new BigDecimal("400"), HOJE);
        assertEquals(Long.valueOf(15), comparacao.mesesSituacaoAtual());
        assertEquals(6L, comparacao.mesesCenario());
        assertEquals(Long.valueOf(9), comparacao.mesesAntecipados());
        assertTrue(comparacao.cenarioCumprePrazo());
        assertFalse(comparacao.situacaoAtualCumprePrazo());
    }
}
