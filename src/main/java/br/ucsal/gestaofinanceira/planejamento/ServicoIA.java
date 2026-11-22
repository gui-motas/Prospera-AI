package br.ucsal.gestaofinanceira.planejamento;

import br.ucsal.gestaofinanceira.financeiro.Periodo;
import br.ucsal.gestaofinanceira.financeiro.ResumoFinanceiro.GastoCategoria;
import br.ucsal.gestaofinanceira.financeiro.ResumoFinanceiro.ResumoMensal;
import br.ucsal.gestaofinanceira.financeiro.ServicoFinanceiro;
import br.ucsal.gestaofinanceira.meta.MetaFinanceira;
import br.ucsal.gestaofinanceira.meta.MetaFinanceiraRepository;
import br.ucsal.gestaofinanceira.planejamento.ia.ClienteIA;
import br.ucsal.gestaofinanceira.usuario.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço {@code ServicoIA} do diagrama de classes (RF-010 – Planejamento Inteligente de Metas).
 *
 * <ul>
 *   <li><b>RN-009 / RNF-005</b> — {@code prepararDadosParaEnvio}: só dados financeiros consolidados
 *       são enviados. Nunca são enviados nome, e-mail, senha, identificadores, descrições de
 *       movimentações nem nomes de contas.</li>
 *   <li><b>RN-012</b> — os números vêm do {@link ServicoFinanceiro}; a IA apenas interpreta.</li>
 *   <li><b>RN-010</b> — instruções exigem caráter informativo e toda resposta é exibida com aviso.</li>
 *   <li><b>RN-011</b> — a IA só devolve texto; nenhum dado é alterado a partir da resposta.</li>
 *   <li><b>RNF-09</b> — falhas viram {@link IAIndisponivelException}, tratada sem afetar o restante.</li>
 * </ul>
 */
@Service
public class ServicoIA {

    public static final String AVISO =
            "Conteúdo gerado por Inteligência Artificial, de caráter informativo e de apoio ao planejamento pessoal. "
                    + "Não constitui garantia de resultado financeiro nem recomendação profissional de investimento. "
                    + "Nenhuma alteração é feita nos seus dados sem a sua confirmação.";

    static final String INSTRUCOES = """
            Você é o assistente de planejamento de uma plataforma de gestão financeira pessoal.
            Regras obrigatórias:
            1. Use somente os dados consolidados recebidos em JSON. Os valores já foram calculados pela
               plataforma e são a fonte oficial: não recalcule nem contradiga esses números.
            2. Responda em português do Brasil, em linguagem simples, com no máximo 250 palavras,
               organizada em tópicos curtos. Não use tabelas.
            3. Seu papel é informativo e de apoio ao planejamento pessoal: não prometa resultados e não
               recomende produtos de investimento, instituições financeiras ou ativos específicos.
            4. Você não executa ações. Apresente sugestões que o próprio usuário pode decidir aplicar.
            5. Não peça nem mencione dados pessoais. As metas chegam identificadas apenas como
               "Meta 1", "Meta 2" etc. (o nome real nunca é enviado): referencie-as por esse rótulo.
            """;

    static final String PEDIDO_SITUACAO = """
            Analise a situação financeira abaixo. Identifique padrões de consumo (por exemplo, categorias
            com maior peso ou com crescimento ao longo dos meses), comente a capacidade mensal de economia
            e como ela afeta as metas, e sugira de 2 a 4 ajustes práticos.""";

    static final String PEDIDO_META = """
            Avalie a viabilidade da meta abaixo considerando a capacidade mensal de economia e os gastos
            recentes. Diga se o plano atual é realista e sugira ajustes de prazo, de valor reservado por mês
            ou de hábitos de consumo (citando categorias quando fizer sentido).""";

    static final String PEDIDO_CENARIO = """
            Interprete o cenário hipotético abaixo, comparando-o com a situação atual. Explique o impacto
            no prazo da meta, se o cenário torna a meta viável dentro do prazo e o que o usuário precisaria
            manter para que a projeção se confirme.""";

    private static final int MAX_CATEGORIAS = 8;

    /** Limite de análises com IA por usuário a cada 24h: cada chamada custa dinheiro. */
    static final int MAX_ANALISES_POR_DIA = 20;

    private final ClienteIA clienteIA;
    private final ServicoFinanceiro servicoFinanceiro;
    private final MetaFinanceiraRepository metaRepository;
    private final AnaliseFinanceiraRepository analiseRepository;
    private final ObjectMapper objectMapper;

    public ServicoIA(ClienteIA clienteIA,
                     ServicoFinanceiro servicoFinanceiro,
                     MetaFinanceiraRepository metaRepository,
                     AnaliseFinanceiraRepository analiseRepository,
                     ObjectMapper objectMapper) {
        this.clienteIA = clienteIA;
        this.servicoFinanceiro = servicoFinanceiro;
        this.metaRepository = metaRepository;
        this.analiseRepository = analiseRepository;
        this.objectMapper = objectMapper;
    }

    public boolean estaDisponivel() {
        return clienteIA.estaConfigurado();
    }

    /** gerarAnalise(usuario): padrões de consumo e capacidade de economia. */
    public ResultadoAnalise gerarAnalise(Usuario usuario) {
        LocalDate hoje = LocalDate.now();
        Map<String, Object> dados = prepararDadosSituacao(usuario.getId(), hoje);
        exigirLimiteDiario(usuario);
        String texto = consultarAPIExterna(PEDIDO_SITUACAO, dados);
        AnaliseFinanceira analise = analiseRepository.save(
                new AnaliseFinanceira(usuario, TipoAnalise.SITUACAO_FINANCEIRA, null, null, texto));
        return new ResultadoAnalise(analise, dados);
    }

    /** planejarMeta(meta): viabilidade da meta e sugestões. */
    public ResultadoAnalise planejarMeta(Usuario usuario, MetaFinanceira meta) {
        LocalDate hoje = LocalDate.now();
        Map<String, Object> dados = prepararDadosMeta(usuario.getId(), meta, hoje);
        exigirLimiteDiario(usuario);
        String texto = consultarAPIExterna(PEDIDO_META, dados);
        AnaliseFinanceira analise = analiseRepository.save(
                new AnaliseFinanceira(usuario, TipoAnalise.PLANEJAMENTO_META, meta, null, texto));
        return new ResultadoAnalise(analise, dados);
    }

    /** interpretarCenario(cenario): explicação, em linguagem natural, de um cenário simulado. */
    public ResultadoAnalise interpretarCenario(Usuario usuario, CenarioFinanceiro cenario) {
        LocalDate hoje = LocalDate.now();
        Map<String, Object> dados = prepararDadosCenario(usuario.getId(), cenario, hoje);
        exigirLimiteDiario(usuario);
        String texto = consultarAPIExterna(PEDIDO_CENARIO, dados);
        AnaliseFinanceira analise = analiseRepository.save(
                new AnaliseFinanceira(usuario, TipoAnalise.INTERPRETACAO_CENARIO, cenario.getMeta(), cenario, texto));
        return new ResultadoAnalise(analise, dados);
    }

    /**
     * Cada análise é uma chamada paga à API externa. Sem este limite, um usuário autenticado
     * (ou uma página recarregada em loop) gera chamadas e linhas sem fim.
     */
    private void exigirLimiteDiario(Usuario usuario) {
        long feitas = analiseRepository.countByUsuarioIdAndDataGeracaoGreaterThanEqual(
                usuario.getId(), LocalDateTime.now().minusDays(1));
        if (feitas >= MAX_ANALISES_POR_DIA) {
            throw new LimiteDeAnaliseException(MAX_ANALISES_POR_DIA);
        }
    }

    // ------------------------------------------------------------------
    // prepararDadosParaEnvio() — RN-009: apenas dados consolidados
    // ------------------------------------------------------------------

    Map<String, Object> prepararDadosSituacao(UUID usuarioId, LocalDate hoje) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("dataReferencia", hoje.toString());
        dados.put("saldoTotalAtual", servicoFinanceiro.calcularSaldo(usuarioId));
        dados.put("capacidadeMensalEconomia", servicoFinanceiro.calcularCapacidadeMensalEconomia(usuarioId, hoje));
        dados.put("evolucaoMensal", evolucaoMensal(usuarioId, YearMonth.from(hoje), 6));
        dados.put("gastosPorCategoriaUltimos3Meses", gastosUltimosMeses(usuarioId, hoje));

        List<Map<String, Object>> metas = new ArrayList<>();
        int indice = 0;
        for (MetaFinanceira meta : metaRepository.findByUsuarioIdOrderByPrazoAsc(usuarioId)) {
            metas.add(resumoMeta(meta, indice++, hoje));
        }
        dados.put("metas", metas);
        return dados;
    }

    Map<String, Object> prepararDadosMeta(UUID usuarioId, MetaFinanceira meta, LocalDate hoje) {
        BigDecimal capacidade = servicoFinanceiro.calcularCapacidadeMensalEconomia(usuarioId, hoje);
        BigDecimal necessario = meta.calcularValorMensalNecessario(hoje);

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("dataReferencia", hoje.toString());
        dados.put("meta", resumoMeta(meta, 0, hoje));
        dados.put("capacidadeMensalEconomia", capacidade);
        dados.put("capacidadeAtualCobreValorMensalNecessario", capacidade.compareTo(necessario) >= 0);
        if (capacidade.signum() > 0) {
            long meses = CenarioFinanceiro.calcularMesesNecessarios(meta.calcularValorRestante(), capacidade);
            dados.put("mesesParaConcluirComCapacidadeAtual", meses);
            dados.put("dataProjetadaComCapacidadeAtual", hoje.plusMonths(meses).toString());
        }
        dados.put("evolucaoMensal", evolucaoMensal(usuarioId, YearMonth.from(hoje).minusMonths(1), 3));
        dados.put("gastosPorCategoriaUltimos3Meses", gastosUltimosMeses(usuarioId, hoje));
        return dados;
    }

    Map<String, Object> prepararDadosCenario(UUID usuarioId, CenarioFinanceiro cenario, LocalDate hoje) {
        BigDecimal capacidade = servicoFinanceiro.calcularCapacidadeMensalEconomia(usuarioId, hoje);
        ComparacaoCenario comparacao = cenario.compararComSituacaoAtual(capacidade, hoje);

        Map<String, Object> dadosCenario = new LinkedHashMap<>();
        dadosCenario.put("economiaMensalSimulada", cenario.getEconomiaMensalSimulada());

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("dataReferencia", hoje.toString());
        dados.put("meta", resumoMeta(cenario.getMeta(), 0, hoje));
        dados.put("cenario", dadosCenario);
        dados.put("comparacaoComSituacaoAtual", objectMapper.convertValue(comparacao, Map.class));
        return dados;
    }

    /**
     * RN-009: só números consolidados. O nome da meta é texto livre digitado pelo usuário
     * (pode conter dado pessoal), então a IA recebe apenas a posição da meta na lista.
     */
    private Map<String, Object> resumoMeta(MetaFinanceira meta, int indice, LocalDate hoje) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("meta", "Meta " + (indice + 1));
        m.put("valorDesejado", meta.getValorDesejado());
        m.put("valorAcumulado", meta.getValorAcumulado());
        m.put("valorRestante", meta.calcularValorRestante());
        m.put("prazo", meta.getPrazo().toString());
        m.put("mesesRestantes", meta.calcularMesesRestantes(hoje));
        m.put("valorMensalNecessario", meta.calcularValorMensalNecessario(hoje));
        m.put("progressoPercentual", meta.calcularProgresso());
        m.put("progressoEsperadoPercentual", meta.calcularProgressoEsperado(hoje));
        m.put("noRitmo", meta.estaNoRitmo(hoje));
        m.put("status", meta.statusEm(hoje).name());
        return m;
    }

    private List<Map<String, Object>> evolucaoMensal(UUID usuarioId, YearMonth ultimoMes, int meses) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (ResumoMensal r : servicoFinanceiro.calcularEvolucaoMensal(usuarioId, ultimoMes, meses)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mes", r.mes());
            item.put("receitas", r.receitas());
            item.put("despesas", r.despesas());
            item.put("resultado", r.resultado());
            lista.add(item);
        }
        return lista;
    }

    private List<Map<String, Object>> gastosUltimosMeses(UUID usuarioId, LocalDate hoje) {
        YearMonth ultimo = YearMonth.from(hoje).minusMonths(1);
        Periodo periodo = new Periodo(
                ultimo.minusMonths(ServicoFinanceiro.MESES_BASE_CAPACIDADE - 1L).atDay(1), ultimo.atEndOfMonth());
        List<Map<String, Object>> lista = new ArrayList<>();
        for (GastoCategoria g : servicoFinanceiro.agruparGastosPorCategoria(usuarioId, periodo)) {
            if (lista.size() == MAX_CATEGORIAS) {
                break;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("categoria", g.categoria());
            item.put("total", g.total());
            item.put("percentual", g.percentual());
            lista.add(item);
        }
        return lista;
    }

    // ------------------------------------------------------------------
    // consultarAPIExterna()
    // ------------------------------------------------------------------

    private String consultarAPIExterna(String pedido, Map<String, Object> dados) {
        if (!clienteIA.estaConfigurado()) {
            throw new IAIndisponivelException("API de IA não configurada.");
        }
        String json;        try {
            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dados);
        } catch (JsonProcessingException e) {
            throw new IAIndisponivelException("Falha ao preparar os dados para a IA.", e);
        }
        String resposta = clienteIA.gerarTexto(INSTRUCOES, pedido + "\n\nDados consolidados (JSON):\n" + json);
        if (resposta == null || resposta.isBlank()) {
            throw new IAIndisponivelException("A API de IA retornou uma resposta vazia.");
        }
        return resposta.trim();
    }
}
