import { useState } from 'react';
import { BarraProgresso, EstadoVazio, ErroCarregamento, PageHeader } from '../components/Comuns';
import { ConfirmModal } from '../components/ConfirmModal';
import { Icone } from '../components/Icone';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { OrcamentoModal } from '../components/modais/OrcamentoModal';
import { useAcao } from '../hooks/useAcao';
import { useOrcamentos } from '../hooks/useDados';
import { mensagemDeErro } from '../services/api-client';
import { orcamentoService } from '../services/servicos';
import type { Orcamento, SituacaoOrcamento } from '../services/tipos';
import { ROTULO_SITUACAO_ORCAMENTO, formatarData, formatarMoeda, formatarPercentual } from '../utils/formatacao';

const VARIANTE: Record<SituacaoOrcamento, 'primaria' | 'alerta' | 'perigo'> = {
  DENTRO_DO_LIMITE: 'primaria',
  ALERTA: 'alerta',
  EXCEDIDO: 'perigo',
};

/** Definição de limites de gastos e acompanhamento do orçamento pessoal (RN-005). */
export function OrcamentosPage() {
  const consulta = useOrcamentos();
  const orcamentos = consulta.data ?? [];
  const [modal, setModal] = useState<{ aberto: boolean; orcamento: Orcamento | null }>({ aberto: false, orcamento: null });
  const [excluindo, setExcluindo] = useState<Orcamento | null>(null);

  const excluir = useAcao((o: Orcamento) => orcamentoService.excluir(o.id), 'Limite excluído.', () => setExcluindo(null));

  return (
    <div className="pagina fade-in">
      <PageHeader
        titulo="Orçamentos"
        descricao="Limites de gastos por categoria e período. Você é avisado ao atingir 80% do limite."
        acoes={
          <button type="button" className="btn btn-primary btn-sm" onClick={() => setModal({ aberto: true, orcamento: null })}>
            <Icone nome="add" /> Novo limite
          </button>
        }
      />

      {consulta.isLoading ? (
        <LoadingSpinner />
      ) : consulta.isError ? (
        <ErroCarregamento mensagem={mensagemDeErro(consulta.error)} onTentar={() => void consulta.refetch()} />
      ) : orcamentos.length === 0 ? (
        <EstadoVazio icone="donut_large" titulo="Nenhum limite de gastos definido"
          texto="Defina quanto pretende gastar em cada categoria para acompanhar seu orçamento." />
      ) : (
        <div className="grade-cards">
          {orcamentos.map((o) => (
            <article key={o.id} className={`cartao orcamento-card situacao-${o.situacao.toLowerCase()}`}>
              <div className="orcamento-topo">
                <div>
                  <h2 className="orcamento-categoria">{o.categoriaNome}</h2>
                  <p className="text-xs text-muted m-0">{formatarData(o.inicioPeriodo)} a {formatarData(o.fimPeriodo)}</p>
                </div>
                <span className={`badge badge-${VARIANTE[o.situacao]}`}>{ROTULO_SITUACAO_ORCAMENTO[o.situacao]}</span>
              </div>
              <p className="orcamento-valores">
                <strong>{formatarMoeda(o.totalGasto)}</strong>
                <span className="text-muted"> de {formatarMoeda(o.limite)}</span>
              </p>
              <BarraProgresso valor={o.percentualUtilizado} variante={VARIANTE[o.situacao]} rotulo={`Uso do limite de ${o.categoriaNome}`} />
              <div className="orcamento-rodape">
                <span className={o.saldoRestante < 0 ? 'valor-negativo' : 'text-muted'}>
                  {o.saldoRestante < 0
                    ? `Ultrapassou ${formatarMoeda(Math.abs(o.saldoRestante))}`
                    : `Restam ${formatarMoeda(o.saldoRestante)}`}{' '}
                  ({formatarPercentual(o.percentualUtilizado)} usado)
                </span>
                <span className="d-flex gap-2">
                  <button type="button" className="btn-icone" onClick={() => setModal({ aberto: true, orcamento: o })}
                    aria-label={`Editar limite de ${o.categoriaNome}`}>
                    <Icone nome="edit" />
                  </button>
                  <button type="button" className="btn-icone btn-icone-perigo" onClick={() => setExcluindo(o)}
                    aria-label={`Excluir limite de ${o.categoriaNome}`}>
                    <Icone nome="delete" />
                  </button>
                </span>
              </div>
            </article>
          ))}
        </div>
      )}

      <OrcamentoModal aberto={modal.aberto} orcamento={modal.orcamento} onClose={() => setModal({ aberto: false, orcamento: null })} />
      <ConfirmModal
        isOpen={excluindo !== null}
        titulo="Excluir limite de gastos"
        textoConfirmar="Excluir"
        perigo
        carregando={excluir.isPending}
        onClose={() => setExcluindo(null)}
        onConfirmar={() => excluindo && excluir.mutate(excluindo)}
      >
        Excluir o limite de <strong>{excluindo?.categoriaNome}</strong>? As movimentações não são afetadas.
      </ConfirmModal>
    </div>
  );
}
