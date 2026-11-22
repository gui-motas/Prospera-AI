import { Link } from 'react-router-dom';
import type { MetaFinanceira } from '../../services/tipos';
import { ROTULO_STATUS_META, formatarData, formatarMoeda } from '../../utils/formatacao';
import { BarraProgresso } from '../Comuns';
import { Icone } from '../Icone';

/** RF-09 – progresso das metas em relação ao valor e ao prazo. */
export function MetasResumo({ metas, limite = 3 }: { metas: MetaFinanceira[]; limite?: number }) {
  const exibidas = metas.filter((m) => m.status !== 'CONCLUIDA').slice(0, limite);

  return (
    <section className="secao" aria-labelledby="titulo-metas">
      <div className="section-title">
        <h2 id="titulo-metas" className="secao-titulo">Minhas metas</h2>
        <Link to="/metas" className="link-acao">Ver todas</Link>
      </div>

      <div className="lista-cards">
        {exibidas.length === 0 ? (
          <p className="text-muted text-sm">Nenhuma meta em andamento.</p>
        ) : (
          exibidas.map((meta) => (
            <article key={meta.id} className="goal-card">
              <div className="goal-card-topo">
                <div className="d-flex align-center gap-3">
                  <span className="goal-icone" aria-hidden="true"><Icone nome="flag" /></span>
                  <div>
                    <h3 className="goal-nome">{meta.nome}</h3>
                    <p className="goal-detalhe">
                      {formatarMoeda(meta.valorAcumulado)} de {formatarMoeda(meta.valorDesejado)} · até {formatarData(meta.prazo)}
                    </p>
                  </div>
                </div>
                <span className="goal-percentual">{Math.round(meta.progresso)}%</span>
              </div>
              <BarraProgresso
                valor={meta.progresso}
                marcador={meta.progressoEsperado}
                rotulo={`Progresso da meta ${meta.nome}`}
                variante={meta.status === 'ATRASADA' ? 'perigo' : meta.noRitmo ? 'primaria' : 'alerta'}
              />
              <p className={`goal-ritmo ${meta.noRitmo ? 'ok' : 'atencao'}`}>
                {meta.status === 'ATRASADA'
                  ? ROTULO_STATUS_META.ATRASADA
                  : meta.noRitmo
                    ? 'No ritmo para o prazo'
                    : `Abaixo do ritmo · guarde ${formatarMoeda(meta.valorMensalNecessario)}/mês`}
              </p>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
