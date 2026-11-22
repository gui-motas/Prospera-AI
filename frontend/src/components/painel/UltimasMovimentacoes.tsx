import { Link } from 'react-router-dom';
import type { Movimentacao } from '../../services/tipos';
import { formatarData, formatarMoeda, iconeCategoria } from '../../utils/formatacao';
import { Icone } from '../Icone';

/** Lista de movimentações no estilo do painel (ícone, descrição, data e valor). */
export function ListaMovimentacoes({ movimentacoes, aoSelecionar }: {
  movimentacoes: Movimentacao[];
  aoSelecionar?: (m: Movimentacao) => void;
}) {
  return (
    <ul className="lista-movimentacoes">
      {movimentacoes.map((m) => {
        const conteudo = (
          <>
            <span className={`icon-circle ${m.tipo === 'RECEITA' ? 'icon-receita' : 'bg-slate-soft'}`} aria-hidden="true">
              <Icone nome={iconeCategoria(m.categoriaNome, m.tipo)} />
            </span>
            <span className="list-item-texto">
              <span className="list-item-titulo">{m.descricao}</span>
              <span className="list-item-sub">{m.categoriaNome} · {m.contaNome} · {formatarData(m.data)}</span>
            </span>
            <span className={`list-item-valor ${m.tipo === 'RECEITA' ? 'valor-positivo' : 'valor-negativo'}`}>
              <span className="sr-only">{m.tipo === 'RECEITA' ? 'Receita de' : 'Despesa de'} </span>
              {m.tipo === 'RECEITA' ? '+ ' : '− '}{formatarMoeda(m.valor)}
            </span>
          </>
        );
        return (
          <li key={m.id}>
            {aoSelecionar ? (
              <button type="button" className="list-item list-item-botao" onClick={() => aoSelecionar(m)}
                aria-label={`Editar ${m.descricao}, ${formatarMoeda(m.valor)}, ${formatarData(m.data)}`}>
                {conteudo}
              </button>
            ) : (
              <div className="list-item">{conteudo}</div>
            )}
          </li>
        );
      })}
    </ul>
  );
}

export function UltimasMovimentacoes({ movimentacoes }: { movimentacoes: Movimentacao[] }) {
  return (
    <section className="secao" aria-labelledby="titulo-ultimas">
      <div className="section-title">
        <h2 id="titulo-ultimas" className="secao-titulo">Últimas movimentações</h2>
        <Link to="/movimentacoes" className="link-acao">Ver histórico</Link>
      </div>
      {movimentacoes.length === 0 ? (
        <p className="text-muted text-sm">Nenhuma movimentação no período.</p>
      ) : (
        <ListaMovimentacoes movimentacoes={movimentacoes.slice(0, 6)} />
      )}
    </section>
  );
}
