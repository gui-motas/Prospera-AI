package br.ucsal.gestaofinanceira.planejamento;

import br.ucsal.gestaofinanceira.financeiro.ResumoFinanceiro.GastoCategoria;
import br.ucsal.gestaofinanceira.financeiro.ResumoFinanceiro.ResumoMensal;
import br.ucsal.gestaofinanceira.financeiro.ServicoFinanceiro;
import br.ucsal.gestaofinanceira.meta.MetaFinanceiraRepository;
import br.ucsal.gestaofinanceira.planejamento.ia.ClienteIA;
import br.ucsal.gestaofinanceira.planejamento.ia.ClienteIAIndisponivel;
import br.ucsal.gestaofinanceira.usuario.PerfilUsuario;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** RN-009 (minimização dos dados enviados), RN-012 e RNF-09 (falha da API de IA). */
class ServicoIATest {

    private final Usuario usuario = new Usuario("Maria Souza", "maria@email.com", "hash-secreto", PerfilUsuario.USUARIO);

    private final ServicoFinanceiro servicoFinanceiro = mock(ServicoFinanceiro.class);
    private final MetaFinanceiraRepository metaRepository = mock(MetaFinanceiraRepository.class);
    private final AnaliseFinanceiraRepository analiseRepository = mock(AnaliseFinanceiraRepository.class);

    @BeforeEach
    void configurar() {
        when(servicoFinanceiro.calcularSaldo(any())).thenReturn(new BigDecimal("1500.00"));
        when(servicoFinanceiro.calcularCapacidadeMensalEconomia(any(), any())).thenReturn(new BigDecimal("300.00"));
        when(servicoFinanceiro.calcularEvolucaoMensal(any(), any(), anyInt())).thenReturn(List.of(
                new ResumoMensal("2026-10", new BigDecimal("3000"), new BigDecimal("2700"), new BigDecimal("300"))));
        when(servicoFinanceiro.agruparGastosPorCategoria(any(), any())).thenReturn(List.of(
                new GastoCategoria(UUID.randomUUID(), "Alimentação", new BigDecimal("900"), new BigDecimal("33.3"))));
        when(metaRepository.findByUsuarioIdOrderByPrazoAsc(any())).thenReturn(List.of());
        when(analiseRepository.save(any(AnaliseFinanceira.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void rn009_dadosPessoaisNaoSaoEnviadosAIA() {
        ClienteFalso cliente = new ClienteFalso("Análise de teste");
        ServicoIA servico = new ServicoIA(cliente, servicoFinanceiro, metaRepository, analiseRepository, new ObjectMapper());

        ResultadoAnalise resultado = servico.gerarAnalise(usuario);

        assertEquals("Análise de teste", resultado.analise().consultarResultado());
        assertFalse(cliente.ultimaMensagem.contains("maria@email.com"));
        assertFalse(cliente.ultimaMensagem.contains("Maria Souza"));
        assertFalse(cliente.ultimaMensagem.contains("hash-secreto"));
        // RN-012: a IA recebe os valores já calculados pela plataforma
        assertTrue(cliente.ultimaMensagem.contains("capacidadeMensalEconomia"));
        assertTrue(cliente.ultimaMensagem.contains("Alimentação"));
    }

    @Test
    void rnf09_indisponibilidadeDaIAEhInformadaSemSalvarAnalise() {
        ServicoIA servico = new ServicoIA(new ClienteIAIndisponivel("sem chave"),
                servicoFinanceiro, metaRepository, analiseRepository, new ObjectMapper());

        assertFalse(servico.estaDisponivel());
        assertThrows(IAIndisponivelException.class, () -> servico.gerarAnalise(usuario));
        verify(analiseRepository, never()).save(any());
    }

    /** Substitui a API externa e guarda a última mensagem enviada. */
    private static final class ClienteFalso implements ClienteIA {
        private final String resposta;
        private String ultimaMensagem = "";

        private ClienteFalso(String resposta) {
            this.resposta = resposta;
        }

        @Override
        public boolean estaConfigurado() {
            return true;
        }

        @Override
        public String gerarTexto(String instrucoes, String mensagem) {
            this.ultimaMensagem = instrucoes + "\n" + mensagem;
            return resposta;
        }
    }
}
