import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useMutation, useQuery } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ErroCarregamento, EstadoVazio, PageHeader } from '../components/Comuns';
import { ConfirmModal } from '../components/ConfirmModal';
import { CurrencyInput } from '../components/CurrencyInput';
import { Icone } from '../components/Icone';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { TextoMarkdown } from '../components/TextoMarkdown';
import { useAcao } from '../hooks/useAcao';
import { useAtualizarDados, useCategorias, useMetas, useStatusIA } from '../hooks/useDados';
import { iaIndisponivel, limiteDeAnaliseAtingido, mensagemDeErro } from '../services/api-client';
import { metaService, planejamentoService } from '../services/servicos';
import type { AnaliseFinanceira, CenarioFinanceiro, CenarioRequest, MetaFinanceira } from '../services/tipos';
import { ROTULO_STATUS_META, ROTULO_TIPO_ANALISE, formatarData, formatarDataHora, formatarMoeda } from '../utils/formatacao';

/**
 * RF-010 – Planejamento Inteligente de Metas.
 * Fluxo do BPMN: a plataforma calcula saldo e progresso (RN-004, RN-012), padroniza os dados (RN-009),
 * envia à API de IA, exibe a análise em linguagem natural (RN-010) e o usuário decide se ajusta a meta (RN-011).
 */
export function PlanejamentoPage() {
  const [params, setParams] = useSearchParams();
  const statusIA = useStatusIA();
  const metas = useMetas();
  const diagnostico = useQuery({ queryKey: ['planejamento', 'diagnostico'], queryFn: planejamentoService.diagnostico });
  const historico = useQuery({ queryKey: ['planejamento', 'historico'], queryFn: planejamentoService.historico });

  const [analise, setAnalise] = useState<AnaliseFinanceira | null>(null);
  const [iaFalhou, setIaFalhou] = useState(false);

  const metasAbertas = (metas.data ?? []).filter((m) => m.status !== 'CONCLUIDA');
  const metaParam = params.get('meta');
  const metaId = metasAbertas.some((m) => m.id === metaParam) ? (metaParam ?? '') : (metasAbertas[0]?.id ?? '');
  const metaSelecionada = (metas.data ?? []).find((m) => m.id === metaId) ?? null;

  const iaDisponivel = (statusIA.data?.iaDisponivel ?? false) && !iaFalhou;

  const tratarErroIA = (erro: unknown) => {
    if (iaIndisponivel(erro)) {
      setIaFalhou(true); // RNF-09: informa e mantém o restante da página funcionando
    } else if (limiteDeAnaliseAtingido(erro)) {
      toast.error(mensagemDeErro(erro), { id: 'limite-ia' });
    } else {
      toast.error(mensagemDeErro(erro));
    }
  };

  const aoGerarAnalise = (a: AnaliseFinanceira) => {
    setAnalise(a);
    void historico.refetch();
  };

  const analisarSituacao = useMutation({ mutationFn: planejamentoService.analisarSituacao, onSuccess: aoGerarAnalise, onError: tratarErroIA });
  const analisarMeta = useMutation({ mutationFn: planejamentoService.analisarMeta, onSuccess: aoGerarAnalise, onError: tratarErroIA });
  const interpretarCenario = useMutation({ mutationFn: planejamentoService.interpretarCenario, onSuccess: aoGerarAnalise, onError: tratarErroIA });
  const gerandoIA = analisarSituacao.isPending || analisarMeta.isPending || interpretarCenario.isPending;

  return (
    <div className="pagina fade-in">
      <PageHeader
        titulo="ProsperAI"
        descricao="Viabilidade das metas, simulação de cenários e análises em linguagem natural"
        acoes={
          <span className={`badge ${iaDisponivel ? 'badge-sucesso' : 'badge-neutro'}`}>
            <Icone nome={iaDisponivel ? 'auto_awesome' : 'cloud_off'} />
            {iaDisponivel ? 'ProsperAI disponível' : 'ProsperAI indisponível'}
          </span>
        }
      />

      {!statusIA.isLoading && !iaDisponivel && (
        <div className="aviso aviso-alerta" role="status">
          <Icone nome="info" />
          <span>
            O recurso de planejamento inteligente está temporariamente indisponível. O diagnóstico e a simulação
            de cenários abaixo continuam funcionando, assim como as demais funcionalidades da plataforma.
          </span>
        </div>
      )}

      {/* 1. Diagnóstico determinístico (RN-012) */}
      <section className="cartao" aria-labelledby="titulo-diagnostico">
        <div className="section-title">
          <h2 id="titulo-diagnostico" className="secao-titulo">Diagnóstico da plataforma</h2>
          <button type="button" className="btn btn-primary btn-sm" disabled={!iaDisponivel || gerandoIA}
            onClick={() => analisarSituacao.mutate()}>
            <Icone nome="auto_awesome" /> {analisarSituacao.isPending ? 'Analisando…' : 'Analisar minha situação com ProsperAI'}
          </button>
        </div>
        {diagnostico.isLoading ? (
          <LoadingSpinner />
        ) : diagnostico.isError || !diagnostico.data ? (
          <ErroCarregamento mensagem={mensagemDeErro(diagnostico.error)} onTentar={() => void diagnostico.refetch()} />
        ) : (
          <>
            <dl className="indicadores">
              <div className="indicador">
                <dt>Capacidade mensal de economia</dt>
                <dd>{formatarMoeda(diagnostico.data.capacidadeMensalEconomia)}</dd>
              </div>
              <div className="indicador">
                <dt>Saldo total</dt>
                <dd>{formatarMoeda(diagnostico.data.saldoTotal)}</dd>
              </div>
            </dl>
            <p className="form-ajuda">
              Capacidade = média de (receitas − despesas) dos últimos {diagnostico.data.mesesConsiderados} meses completos,
              calculada pela plataforma.
            </p>
            {diagnostico.data.metas.length === 0 ? (
              <p className="text-muted text-sm">Cadastre uma meta para avaliar sua viabilidade. <Link to="/metas">Ir para Metas</Link></p>
            ) : (
              <ul className="lista-viabilidade">
                {diagnostico.data.metas.map((v) => (
                  <li key={v.metaId} className={`viabilidade ${v.viavelComCapacidadeAtual ? 'viavel' : 'inviavel'}`}>
                    <Icone nome={v.viavelComCapacidadeAtual ? 'check_circle' : 'report'} />
                    <div className="viabilidade-texto">
                      <strong>{v.nome}</strong>
                      <span className="text-sm">
                        {v.status === 'CONCLUIDA'
                          ? 'Meta concluída.'
                          : v.status === 'ATRASADA'
                            ? `Prazo encerrado em ${formatarData(v.prazo)}. Simule um novo prazo.`
                            : `Precisa de ${formatarMoeda(v.valorMensalNecessario)}/mês até ${formatarData(v.prazo)}.`}
                        {v.prazoProjetadoComCapacidadeAtual && v.status !== 'CONCLUIDA' &&
                          ` No ritmo atual, conclui em ${formatarData(v.prazoProjetadoComCapacidadeAtual)}.`}
                      </span>
                    </div>
                    {v.status !== 'CONCLUIDA' && (
                      <button type="button" className="btn btn-ghost btn-sm" onClick={() => setParams({ meta: v.metaId })}
                        aria-pressed={v.metaId === metaId}>
                        {v.metaId === metaId ? 'Selecionada' : 'Planejar'}
                      </button>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </>
        )}
      </section>

      {analise && <PainelAnalise analise={analise} onFechar={() => setAnalise(null)} />}

      {/* 2. Planejamento de uma meta */}
      {metas.isLoading ? null : metasAbertas.length === 0 ? (
        <EstadoVazio icone="flag" titulo="Nenhuma meta em andamento"
          acao={<Link to="/metas" className="btn btn-primary btn-sm">Criar meta</Link>} />
      ) : (
        <section className="cartao" aria-labelledby="titulo-meta-planejada">
          <div className="section-title">
            <h2 id="titulo-meta-planejada" className="secao-titulo">Planejar meta</h2>
            <label className="sr-only" htmlFor="seletor-meta">Meta</label>
            <select id="seletor-meta" className="form-control seletor-meta" value={metaId}
              onChange={(e) => setParams({ meta: e.target.value })}>
              {metasAbertas.map((m) => <option key={m.id} value={m.id}>{m.nome}</option>)}
            </select>
          </div>

          {metaSelecionada && (
            <>
              <ResumoMetaPlanejada meta={metaSelecionada} />
              <button type="button" className="btn btn-primary btn-sm" disabled={!iaDisponivel || gerandoIA}
                onClick={() => analisarMeta.mutate(metaSelecionada.id)}>
                <Icone nome="auto_awesome" /> {analisarMeta.isPending ? 'Analisando…' : 'Avaliar viabilidade com ProsperAI'}
              </button>
              <SimuladorCenarios
                key={`${metaSelecionada.id}-${diagnostico.data?.capacidadeMensalEconomia ?? 0}`}
                meta={metaSelecionada}
                capacidade={diagnostico.data?.capacidadeMensalEconomia ?? 0}
                iaDisponivel={iaDisponivel}
                gerandoIA={gerandoIA}
                onInterpretar={(id) => interpretarCenario.mutate(id)}
              />
            </>
          )}
        </section>
      )}

      {/* 3. Histórico */}
      {(historico.data ?? []).length > 0 && (
        <section className="cartao" aria-labelledby="titulo-historico">
          <h2 id="titulo-historico" className="secao-titulo">Análises anteriores</h2>
          <ul className="lista-historico">
            {(historico.data ?? []).map((a) => (
              <li key={a.id}>
                <details>
                  <summary>
                    <span className="badge badge-neutro">{ROTULO_TIPO_ANALISE[a.tipo]}</span>
                    {a.metaNome && <span className="text-sm"> {a.metaNome}</span>}
                    <span className="text-xs text-muted"> · {formatarDataHora(a.dataGeracao)}</span>
                  </summary>
                  <TextoMarkdown texto={a.resultado} />
                  <p className="aviso-ia">{a.aviso}</p>
                </details>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}

function ResumoMetaPlanejada({ meta }: { meta: MetaFinanceira }) {
  return (
    <dl className="meta-detalhes meta-detalhes-amplo">
      <div><dt>Situação</dt><dd>{ROTULO_STATUS_META[meta.status]}</dd></div>
      <div><dt>Acumulado</dt><dd>{formatarMoeda(meta.valorAcumulado)} de {formatarMoeda(meta.valorDesejado)}</dd></div>
      <div><dt>Prazo</dt><dd>{formatarData(meta.prazo)}</dd></div>
      <div><dt>Necessário por mês</dt><dd>{formatarMoeda(meta.valorMensalNecessario)}</dd></div>
    </dl>
  );
}

/** Texto gerado pela IA, com aviso de caráter informativo (RN-010) e transparência dos dados enviados (RN-009). */
function PainelAnalise({ analise, onFechar }: { analise: AnaliseFinanceira; onFechar: () => void }) {
  return (
    <section className="cartao painel-analise" aria-labelledby="titulo-analise" aria-live="polite">
      <div className="section-title">
        <h2 id="titulo-analise" className="secao-titulo">
          <Icone nome="auto_awesome" /> {ROTULO_TIPO_ANALISE[analise.tipo]}{analise.metaNome ? ` — ${analise.metaNome}` : ''}
        </h2>
        <button type="button" className="btn-icone" onClick={onFechar} aria-label="Fechar análise">
          <Icone nome="close" />
        </button>
      </div>
      <TextoMarkdown texto={analise.resultado} />
      <p className="aviso-ia"><Icone nome="info" /> {analise.aviso}</p>
      {analise.dadosEnviados && (
        <details className="dados-enviados">
          <summary>Ver os dados enviados à IA</summary>
          <p className="text-xs text-muted">
            Somente valores consolidados. Nome, e-mail, senha, descrições de movimentações, nomes de contas,
            nomes de metas e descrições de cenários não são enviados.
          </p>
          <pre>{JSON.stringify(analise.dadosEnviados, null, 2)}</pre>
        </details>
      )}
    </section>
  );
}

/** Simulação de cenários hipotéticos (CenarioFinanceiro) – cálculo feito pela plataforma. */
function SimuladorCenarios({ meta, capacidade, iaDisponivel, gerandoIA, onInterpretar }: {
  meta: MetaFinanceira;
  capacidade: number;
  iaDisponivel: boolean;
  gerandoIA: boolean;
  onInterpretar: (cenarioId: string) => void;
}) {
  const id = useId();
  const atualizar = useAtualizarDados();
  const { data: categorias = [] } = useCategorias();
  const despesas = categorias.filter((c) => c.tipo === 'DESPESA');
  const cenarios = useQuery({ queryKey: ['planejamento', 'cenarios', meta.id], queryFn: () => planejamentoService.listarCenarios(meta.id) });

  const [economia, setEconomia] = useState(capacidade > 0 ? String(capacidade) : '');
  const [categoriaId, setCategoriaId] = useState('');
  const [percentual, setPercentual] = useState('');
  const [descricao, setDescricao] = useState('');
  const [adotando, setAdotando] = useState<CenarioFinanceiro | null>(null);

  const simular = useAcao(
    (dados: CenarioRequest) => planejamentoService.simularCenario(meta.id, dados),
    'Cenário simulado.',
  );
  const excluir = useAcao((c: CenarioFinanceiro) => planejamentoService.excluirCenario(c.id), 'Cenário excluído.');

  // RN-011: a meta só muda quando o usuário confirma
  const adotarPrazo = useMutation({
    mutationFn: (c: CenarioFinanceiro) =>
      metaService.editar(meta.id, { nome: meta.nome, valorDesejado: meta.valorDesejado, prazo: c.comparacao.prazoProjetadoCenario }),
    onSuccess: () => {
      toast.success('Prazo da meta atualizado.');
      setAdotando(null);
      atualizar();
    },
    onError: (e) => toast.error(mensagemDeErro(e)),
  });

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    const valorEconomia = Number(economia || 0);
    const pct = percentual ? Number(percentual) : undefined;
    const categoria = despesas.find((c) => c.id === categoriaId);
    const texto = descricao.trim() || [
      `Reservar ${formatarMoeda(valorEconomia)}/mês`,
      categoria && pct ? `reduzir ${pct}% em ${categoria.nome}` : '',
    ].filter(Boolean).join(' e ');
    simular.mutate({
      descricao: texto,
      economiaMensal: valorEconomia,
      categoriaReducaoId: categoria && pct ? categoria.id : undefined,
      percentualReducao: categoria && pct ? pct : undefined,
    });
  };

  return (
    <div className="simulador">
      <h3 className="subtitulo">Simular cenário</h3>
      <p className="text-sm text-muted">
        E se você guardasse outro valor por mês, ou reduzisse gastos em uma categoria? Veja como o prazo da meta mudaria.
      </p>

      <form onSubmit={enviar} className="simulador-form">
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-economia`}>Quanto guardar por mês (R$)</label>
          <CurrencyInput id={`${id}-economia`} className="form-control" value={economia} onValueChange={setEconomia} placeholder="0,00" />
          <p className="form-ajuda">Sua capacidade atual: {formatarMoeda(capacidade)}/mês</p>
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-categoria`}>Reduzir gastos em (opcional)</label>
          <select id={`${id}-categoria`} className="form-control" value={categoriaId} onChange={(e) => setCategoriaId(e.target.value)}>
            <option value="">Nenhuma categoria</option>
            {despesas.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-percentual`}>Redução (%)</label>
          <input id={`${id}-percentual`} type="number" min={1} max={100} className="form-control" value={percentual}
            onChange={(e) => setPercentual(e.target.value)} disabled={!categoriaId} placeholder="Ex.: 30" />
        </div>
        <div className="form-group simulador-descricao">
          <label className="form-label" htmlFor={`${id}-descricao`}>Descrição (opcional)</label>
          <input id={`${id}-descricao`} className="form-control" value={descricao} maxLength={200}
            onChange={(e) => setDescricao(e.target.value)} placeholder="Ex.: Cortar delivery pela metade" />
        </div>
        <button type="submit" className="btn btn-primary" disabled={simular.isPending}>
          <Icone nome="calculate" /> {simular.isPending ? 'Calculando…' : 'Simular'}
        </button>
      </form>

      {cenarios.isLoading ? (
        <LoadingSpinner />
      ) : (
        <ul className="lista-cenarios">
          {(cenarios.data ?? []).map((c) => {
            const comp = c.comparacao;
            return (
              <li key={c.id} className="cenario">
                <div className="cenario-topo">
                  <strong>{c.descricao}</strong>
                  <span className={`badge ${comp.cenarioCumprePrazo ? 'badge-sucesso' : 'badge-alerta'}`}>
                    {comp.cenarioCumprePrazo ? 'Cumpre o prazo' : 'Não cumpre o prazo'}
                  </span>
                </div>
                <div className="comparacao">
                  <div className="comparacao-coluna">
                    <span className="comparacao-rotulo">Situação atual</span>
                    <span>{formatarMoeda(comp.economiaMensalAtual)}/mês</span>
                    <span className="comparacao-destaque">
                      {comp.prazoProjetadoAtual ? `Conclui em ${formatarData(comp.prazoProjetadoAtual)}` : 'Sem economia no período'}
                    </span>
                  </div>
                  <Icone nome="arrow_forward" className="comparacao-seta" />
                  <div className="comparacao-coluna">
                    <span className="comparacao-rotulo">Cenário</span>
                    <span>
                      {formatarMoeda(comp.economiaMensalSimulada)}/mês
                      {c.reducaoMensalAplicada ? ` (inclui ${formatarMoeda(c.reducaoMensalAplicada)} de redução)` : ''}
                    </span>
                    <span className="comparacao-destaque">Conclui em {formatarData(comp.prazoProjetadoCenario)} ({comp.mesesCenario} meses)</span>
                  </div>
                </div>
                <p className="text-sm text-muted m-0">
                  Prazo da meta: {formatarData(comp.prazoMeta)}
                  {comp.mesesAntecipados !== null && comp.mesesAntecipados !== 0 &&
                    ` · ${comp.mesesAntecipados > 0 ? 'antecipa' : 'atrasa'} ${Math.abs(comp.mesesAntecipados)} mês(es) em relação à situação atual`}
                </p>
                <div className="cenario-acoes">
                  <button type="button" className="btn btn-ghost btn-sm" disabled={!iaDisponivel || gerandoIA} onClick={() => onInterpretar(c.id)}>
                    <Icone nome="auto_awesome" /> Interpretar com IA
                  </button>
                  {comp.prazoProjetadoCenario !== meta.prazo && (
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => setAdotando(c)}>
                      Adotar novo prazo
                    </button>
                  )}
                  <button type="button" className="btn-icone btn-icone-perigo" onClick={() => excluir.mutate(c)} aria-label={`Excluir cenário ${c.descricao}`}>
                    <Icone nome="delete" />
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      )}

      <ConfirmModal
        isOpen={adotando !== null}
        titulo="Atualizar prazo da meta"
        textoConfirmar="Atualizar prazo"
        carregando={adotarPrazo.isPending}
        onClose={() => setAdotando(null)}
        onConfirmar={() => adotando && adotarPrazo.mutate(adotando)}
      >
        Alterar o prazo de <strong>{meta.nome}</strong> de {formatarData(meta.prazo)} para{' '}
        <strong>{formatarData(adotando?.comparacao.prazoProjetadoCenario)}</strong>? Lembre-se de guardar{' '}
        {formatarMoeda(adotando?.economiaMensalSimulada)} por mês para manter o plano.
      </ConfirmModal>
    </div>
  );
}
