import { useState } from 'react';
import { Link } from 'react-router-dom';
import { BarraProgresso, EstadoVazio, ErroCarregamento, PageHeader } from '../components/Comuns';
import { ConfirmModal } from '../components/ConfirmModal';
import { Icone } from '../components/Icone';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { AporteModal } from '../components/modais/AporteModal';
import { MetaModal } from '../components/modais/MetaModal';
import { useAcao } from '../hooks/useAcao';
import { useMetas } from '../hooks/useDados';
import { mensagemDeErro } from '../services/api-client';
import { metaService } from '../services/servicos';
import type { MetaFinanceira, OperacaoAporte } from '../services/tipos';
import { ROTULO_STATUS_META, formatarData, formatarMoeda, formatarPercentual } from '../utils/formatacao';

/** RF-08 – Cadastro de metas e RF-09 – Acompanhamento do progresso em relação ao valor e ao prazo. */
export function MetasPage() {
  const consulta = useMetas();
  const metas = consulta.data ?? [];
  const [modal, setModal] = useState<{ aberto: boolean; meta: MetaFinanceira | null }>({ aberto: false, meta: null });
  const [aporte, setAporte] = useState<{ meta: MetaFinanceira | null; operacao: OperacaoAporte }>({ meta: null, operacao: 'APORTE' });
  const [excluindo, setExcluindo] = useState<MetaFinanceira | null>(null);

  const excluir = useAcao((m: MetaFinanceira) => metaService.excluir(m.id), 'Meta excluída.', () => setExcluindo(null));

  return (
    <div className="pagina fade-in">
      <PageHeader
        titulo="Metas financeiras"
        descricao="Defina valor e prazo e acompanhe se você está no ritmo certo"
        acoes={
          <button type="button" className="btn btn-primary btn-sm" onClick={() => setModal({ aberto: true, meta: null })}>
            <Icone nome="add" /> Nova meta
          </button>
        }
      />

      {consulta.isLoading ? (
        <LoadingSpinner />
      ) : consulta.isError ? (
        <ErroCarregamento mensagem={mensagemDeErro(consulta.error)} onTentar={() => void consulta.refetch()} />
      ) : metas.length === 0 ? (
        <EstadoVazio icone="flag" titulo="Nenhuma meta cadastrada"
          texto="Reserva de emergência, viagem, um curso… transforme seus objetivos em um plano." />
      ) : (
        <div className="grade-cards">
          {metas.map((meta) => {
            const variante = meta.status === 'CONCLUIDA' ? 'sucesso' : meta.status === 'ATRASADA' ? 'perigo' : meta.noRitmo ? 'primaria' : 'alerta';
            return (
              <article key={meta.id} className="cartao meta-card">
                <div className="meta-topo">
                  <span className="goal-icone" aria-hidden="true"><Icone nome={meta.status === 'CONCLUIDA' ? 'emoji_events' : 'flag'} /></span>
                  <div className="meta-titulo">
                    <h2 className="goal-nome">{meta.nome}</h2>
                    <p className="text-xs text-muted m-0">Prazo: {formatarData(meta.prazo)}</p>
                  </div>
                  <span className={`badge badge-${variante}`}>{ROTULO_STATUS_META[meta.status]}</span>
                </div>

                <p className="meta-valores">
                  <strong>{formatarMoeda(meta.valorAcumulado)}</strong>
                  <span className="text-muted"> de {formatarMoeda(meta.valorDesejado)}</span>
                  <span className="goal-percentual">{formatarPercentual(meta.progresso)}</span>
                </p>
                <BarraProgresso valor={meta.progresso} marcador={meta.status === 'EM_ANDAMENTO' ? meta.progressoEsperado : undefined}
                  variante={variante} rotulo={`Progresso da meta ${meta.nome}`} />

                {meta.status === 'EM_ANDAMENTO' && (
                  <dl className="meta-detalhes">
                    <div><dt>Falta</dt><dd>{formatarMoeda(meta.valorRestante)}</dd></div>
                    <div><dt>Meses restantes</dt><dd>{meta.mesesRestantes}</dd></div>
                    <div><dt>Guardar por mês</dt><dd>{formatarMoeda(meta.valorMensalNecessario)}</dd></div>
                  </dl>
                )}
                {meta.status === 'EM_ANDAMENTO' && (
                  <p className={`goal-ritmo ${meta.noRitmo ? 'ok' : 'atencao'}`}>
                    <Icone nome={meta.noRitmo ? 'check_circle' : 'schedule'} />
                    {meta.noRitmo
                      ? 'No ritmo para cumprir o prazo'
                      : `Abaixo do ritmo: o esperado hoje seria ${formatarPercentual(meta.progressoEsperado)}`}
                  </p>
                )}
                {meta.status === 'ATRASADA' && (
                  <p className="goal-ritmo atencao">
                    <Icone nome="event_busy" /> O prazo terminou. Simule um novo prazo no planejamento.
                  </p>
                )}

                <div className="meta-acoes">
                  <button type="button" className="btn btn-success btn-sm" onClick={() => setAporte({ meta, operacao: 'APORTE' })}
                    disabled={meta.status === 'CONCLUIDA'}>
                    <Icone nome="savings" /> Guardar
                  </button>
                  <button type="button" className="btn btn-secondary btn-sm" onClick={() => setAporte({ meta, operacao: 'RESGATE' })}
                    disabled={meta.valorAcumulado <= 0}>
                    Retirar
                  </button>
                  <Link to={`/planejamento?meta=${meta.id}`} className="btn btn-ghost btn-sm">
                    <Icone nome="auto_awesome" /> Planejar
                  </Link>
                  <span className="meta-acoes-direita">
                    <button type="button" className="btn-icone" onClick={() => setModal({ aberto: true, meta })} aria-label={`Editar meta ${meta.nome}`}>
                      <Icone nome="edit" />
                    </button>
                    <button type="button" className="btn-icone btn-icone-perigo" onClick={() => setExcluindo(meta)} aria-label={`Excluir meta ${meta.nome}`}>
                      <Icone nome="delete" />
                    </button>
                  </span>
                </div>
              </article>
            );
          })}
        </div>
      )}

      <MetaModal aberto={modal.aberto} meta={modal.meta} onClose={() => setModal({ aberto: false, meta: null })} />
      <AporteModal meta={aporte.meta} operacao={aporte.operacao} onClose={() => setAporte((a) => ({ ...a, meta: null }))} />
      <ConfirmModal
        isOpen={excluindo !== null}
        titulo="Excluir meta"
        textoConfirmar="Excluir meta"
        perigo
        carregando={excluir.isPending}
        onClose={() => setExcluindo(null)}
        onConfirmar={() => excluindo && excluir.mutate(excluindo)}
      >
        Excluir a meta <strong>{excluindo?.nome}</strong>? Os cenários e análises dessa meta também serão excluídos.
      </ConfirmModal>
    </div>
  );
}
