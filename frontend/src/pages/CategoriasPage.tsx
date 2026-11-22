import { useState } from 'react';
import { EstadoVazio, ErroCarregamento, PageHeader } from '../components/Comuns';
import { ConfirmModal } from '../components/ConfirmModal';
import { Icone } from '../components/Icone';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { CategoriaModal } from '../components/modais/CategoriaModal';
import { useAcao } from '../hooks/useAcao';
import { useCategorias } from '../hooks/useDados';
import { mensagemDeErro } from '../services/api-client';
import { categoriaService } from '../services/servicos';
import type { Categoria, TipoMovimentacao } from '../services/tipos';
import { iconeCategoria } from '../utils/formatacao';

/** RF-004 – Gerenciamento de categorias (RN-003: classificação receita/despesa). */
export function CategoriasPage() {
  const consulta = useCategorias();
  const categorias = consulta.data ?? [];
  const [modal, setModal] = useState<{ aberto: boolean; categoria: Categoria | null; tipo: TipoMovimentacao }>({
    aberto: false, categoria: null, tipo: 'DESPESA',
  });
  const [excluindo, setExcluindo] = useState<Categoria | null>(null);

  const excluir = useAcao((c: Categoria) => categoriaService.excluir(c.id), 'Categoria excluída.', () => setExcluindo(null));

  const grupo = (tipo: TipoMovimentacao, titulo: string) => {
    const lista = categorias.filter((c) => c.tipo === tipo);
    return (
      <section className="cartao" aria-labelledby={`titulo-${tipo}`}>
        <div className="section-title">
          <h2 id={`titulo-${tipo}`} className="secao-titulo">{titulo}</h2>
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => setModal({ aberto: true, categoria: null, tipo })}>
            <Icone nome="add" /> Adicionar
          </button>
        </div>
        {lista.length === 0 ? (
          <p className="text-muted text-sm">Nenhuma categoria.</p>
        ) : (
          <ul className="lista-simples">
            {lista.map((c) => (
              <li key={c.id} className="lista-simples-item">
                <span className={`icon-circle ${tipo === 'RECEITA' ? 'icon-receita' : 'bg-slate-soft'}`} aria-hidden="true">
                  <Icone nome={iconeCategoria(c.nome, c.tipo)} />
                </span>
                <span className="lista-simples-nome">{c.nome}</span>
                <button type="button" className="btn-icone" onClick={() => setModal({ aberto: true, categoria: c, tipo })}
                  aria-label={`Editar categoria ${c.nome}`}>
                  <Icone nome="edit" />
                </button>
                <button type="button" className="btn-icone btn-icone-perigo" onClick={() => setExcluindo(c)}
                  aria-label={`Excluir categoria ${c.nome}`}>
                  <Icone nome="delete" />
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>
    );
  };

  return (
    <div className="pagina fade-in">
      <PageHeader titulo="Categorias" descricao="Classifique suas receitas e despesas" />

      {consulta.isLoading ? (
        <LoadingSpinner />
      ) : consulta.isError ? (
        <ErroCarregamento mensagem={mensagemDeErro(consulta.error)} onTentar={() => void consulta.refetch()} />
      ) : categorias.length === 0 ? (
        <EstadoVazio icone="sell" titulo="Nenhuma categoria"
          acao={<button type="button" className="btn btn-primary btn-sm" onClick={() => setModal({ aberto: true, categoria: null, tipo: 'DESPESA' })}>Criar categoria</button>} />
      ) : (
        <div className="grade-duas-colunas">
          {grupo('DESPESA', 'Despesas')}
          {grupo('RECEITA', 'Receitas')}
        </div>
      )}

      <CategoriaModal aberto={modal.aberto} categoria={modal.categoria} tipoInicial={modal.tipo}
        onClose={() => setModal((m) => ({ ...m, aberto: false, categoria: null }))} />
      <ConfirmModal
        isOpen={excluindo !== null}
        titulo="Excluir categoria"
        textoConfirmar="Excluir"
        perigo
        carregando={excluir.isPending}
        onClose={() => setExcluindo(null)}
        onConfirmar={() => excluindo && excluir.mutate(excluindo)}
      >
        Excluir a categoria <strong>{excluindo?.nome}</strong>? Categorias com movimentações ou limites de gastos
        não podem ser excluídas.
      </ConfirmModal>
    </div>
  );
}
