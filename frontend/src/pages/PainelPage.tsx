import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ErroCarregamento } from '../components/Comuns';
import { Icone } from '../components/Icone';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { MetaModal } from '../components/modais/MetaModal';
import { MovimentacaoModal } from '../components/modais/MovimentacaoModal';
import { ContasSlider } from '../components/painel/ContasSlider';
import { GraficoCategorias, GraficoEvolucao } from '../components/painel/Graficos';
import { HeroSaldo } from '../components/painel/HeroSaldo';
import { MetasResumo } from '../components/painel/MetasResumo';
import { UltimasMovimentacoes } from '../components/painel/UltimasMovimentacoes';
import { useContas, useMetas, useMovimentacoes, useOrcamentos, usePainel } from '../hooks/useDados';
import { mensagemDeErro } from '../services/api-client';
import type { TipoMovimentacao } from '../services/tipos';
import { ROTULO_SITUACAO_ORCAMENTO, formatarMoeda, formatarPercentual, paraIso, periodoDoMes } from '../utils/formatacao';

function deslocarMes(iso: string, meses: number): Date {
  const [ano, mes] = iso.split('-').map(Number);
  return new Date(ano, mes - 1 + meses, 1);
}

/** RF-007 – Dashboard financeiro: saldo, receitas, despesas, distribuição dos gastos e evolução. */
export function PainelPage() {
  const [periodo, setPeriodo] = useState(periodoDoMes());
  const [novaMovimentacao, setNovaMovimentacao] = useState<TipoMovimentacao | null>(null);
  const [novaMeta, setNovaMeta] = useState(false);

  const painel = usePainel(periodo.inicio, periodo.fim);
  const contas = useContas();
  const metas = useMetas();
  const orcamentos = useOrcamentos();
  const movimentacoes = useMovimentacoes({ dataInicio: periodo.inicio, dataFim: periodo.fim });

  const mudarMes = (delta: number) => setPeriodo(periodoDoMes(deslocarMes(periodo.inicio, delta)));
  const nomeMes = deslocarMes(periodo.inicio, 0).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  const ehMesAtual = periodo.inicio === periodoDoMes().inicio;

  if (painel.isLoading) return <LoadingSpinner />;
  if (painel.isError || !painel.data) {
    return <ErroCarregamento mensagem={mensagemDeErro(painel.error)} onTentar={() => void painel.refetch()} />;
  }

  const resumo = painel.data;
  const alertas = (orcamentos.data ?? []).filter((o) => o.situacao !== 'DENTRO_DO_LIMITE' && o.fimPeriodo >= paraIso(new Date()));

  return (
    <div className="pagina pagina-painel fade-in">
      <div className="painel-grid">
        <div className="painel-coluna-principal">
          <div className="cartao cartao-hero">
            <HeroSaldo
              saldo={resumo.saldoTotal}
              onReceita={() => setNovaMovimentacao('RECEITA')}
              onDespesa={() => setNovaMovimentacao('DESPESA')}
              onMeta={() => setNovaMeta(true)}
            />
          </div>

          <section className="cartao" aria-labelledby="titulo-periodo">
            <div className="seletor-mes">
              <button type="button" className="header-btn" onClick={() => mudarMes(-1)} aria-label="Mês anterior">
                <Icone nome="chevron_left" />
              </button>
              <h2 id="titulo-periodo" className="seletor-mes-titulo">{nomeMes}</h2>
              <button type="button" className="header-btn" onClick={() => mudarMes(1)} aria-label="Próximo mês" disabled={ehMesAtual}>
                <Icone nome="chevron_right" />
              </button>
            </div>
            <dl className="indicadores">
              <div className="indicador">
                <dt>Receitas</dt>
                <dd className="valor-positivo">{formatarMoeda(resumo.totalReceitas)}</dd>
              </div>
              <div className="indicador">
                <dt>Despesas</dt>
                <dd className="valor-negativo">{formatarMoeda(resumo.totalDespesas)}</dd>
              </div>
              <div className="indicador">
                <dt>Resultado do mês</dt>
                <dd className={resumo.resultadoPeriodo >= 0 ? 'valor-positivo' : 'valor-negativo'}>
                  {formatarMoeda(resumo.resultadoPeriodo)}
                </dd>
              </div>
              <div className="indicador">
                <dt>Capacidade de economia</dt>
                <dd>{formatarMoeda(resumo.capacidadeMensalEconomia)}<span className="text-xs text-muted"> /mês</span></dd>
              </div>
            </dl>
          </section>

          {alertas.length > 0 && (
            <section className="cartao" aria-labelledby="titulo-alertas">
              <h2 id="titulo-alertas" className="secao-titulo">Limites de gastos</h2>
              <ul className="lista-alertas">
                {alertas.map((o) => (
                  <li key={o.id} className={`aviso ${o.situacao === 'EXCEDIDO' ? 'aviso-perigo' : 'aviso-alerta'}`}>
                    <Icone nome={o.situacao === 'EXCEDIDO' ? 'error' : 'warning'} />
                    <span>
                      <strong>{o.categoriaNome}</strong>: {ROTULO_SITUACAO_ORCAMENTO[o.situacao].toLowerCase()} —{' '}
                      {formatarMoeda(o.totalGasto)} de {formatarMoeda(o.limite)} ({formatarPercentual(o.percentualUtilizado)})
                    </span>
                  </li>
                ))}
              </ul>
              <Link to="/orcamentos" className="link-acao">Ver orçamentos</Link>
            </section>
          )}

          <div className="cartao">
            <ContasSlider contas={contas.data ?? []} />
          </div>

          <section className="cartao" aria-labelledby="titulo-distribuicao">
            <h2 id="titulo-distribuicao" className="secao-titulo">Distribuição dos gastos</h2>
            <GraficoCategorias gastos={resumo.gastosPorCategoria} />
          </section>

          <section className="cartao" aria-labelledby="titulo-evolucao">
            <h2 id="titulo-evolucao" className="secao-titulo">Evolução financeira (6 meses)</h2>
            <GraficoEvolucao meses={resumo.evolucaoMensal} />
          </section>
        </div>

        <div className="painel-coluna-lateral">
          <div className="cartao">
            <MetasResumo metas={metas.data ?? []} />
          </div>
          <div className="cartao cartao-ia">
            <span className="cartao-ia-icone" aria-hidden="true"><Icone nome="auto_awesome" /></span>
            <div>
              <h2 className="secao-titulo">Planejamento inteligente</h2>
              <p className="text-sm text-muted">
                Veja se suas metas cabem no seu orçamento e simule cenários com apoio de IA.
              </p>
              <Link to="/planejamento" className="btn btn-primary btn-sm">Planejar metas</Link>
            </div>
          </div>
          <div className="cartao">
            <UltimasMovimentacoes movimentacoes={movimentacoes.data ?? []} />
          </div>
        </div>
      </div>

      <MovimentacaoModal
        aberto={novaMovimentacao !== null}
        tipoInicial={novaMovimentacao ?? 'DESPESA'}
        onClose={() => setNovaMovimentacao(null)}
      />
      <MetaModal aberto={novaMeta} onClose={() => setNovaMeta(false)} />
    </div>
  );
}
