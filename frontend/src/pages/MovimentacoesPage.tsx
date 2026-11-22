import { useId, useState } from 'react';
import { EstadoVazio, ErroCarregamento, PageHeader } from '../components/Comuns';
import { ConfirmModal } from '../components/ConfirmModal';
import { Icone } from '../components/Icone';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { MovimentacaoModal } from '../components/modais/MovimentacaoModal';
import { useAcao } from '../hooks/useAcao';
import { useCategorias, useContas, useMovimentacoes } from '../hooks/useDados';
import { mensagemDeErro } from '../services/api-client';
import { movimentacaoService } from '../services/servicos';
import type { FiltrosMovimentacao, Movimentacao, TipoMovimentacao } from '../services/tipos';
import { formatarData, formatarMoeda, periodoDoMes } from '../utils/formatacao';

/**
 * RF-006 – Consulta, edição e exclusão de movimentações, com filtros por
 * período, categoria e tipo (escopo 1.2).
 */
export function MovimentacoesPage() {
  const id = useId();
  const mesAtual = periodoDoMes();
  const [filtros, setFiltros] = useState<FiltrosMovimentacao>({ dataInicio: mesAtual.inicio, dataFim: mesAtual.fim });
  const [nova, setNova] = useState<TipoMovimentacao | null>(null);
  const [editando, setEditando] = useState<Movimentacao | null>(null);
  const [excluindo, setExcluindo] = useState<Movimentacao | null>(null);

  const { data: categorias = [] } = useCategorias();
  const { data: contas = [] } = useContas();
  const consulta = useMovimentacoes(filtros);
  const movimentacoes = consulta.data ?? [];

  const excluir = useAcao((m: Movimentacao) => movimentacaoService.excluir(m.id), 'Movimentação excluída.', () => setExcluindo(null));

  const alterar = (campo: keyof FiltrosMovimentacao, valor: string) =>
    setFiltros((f) => ({ ...f, [campo]: valor || undefined }));

  const totalReceitas = movimentacoes.filter((m) => m.tipo === 'RECEITA').reduce((s, m) => s + m.valor, 0);
  const totalDespesas = movimentacoes.filter((m) => m.tipo === 'DESPESA').reduce((s, m) => s + m.valor, 0);
  const categoriasFiltradas = filtros.tipo ? categorias.filter((c) => c.tipo === filtros.tipo) : categorias;

  return (
    <div className="pagina fade-in">
      <PageHeader
        titulo="Movimentações"
        descricao="Histórico de receitas e despesas"
        acoes={
          <>
            <button type="button" className="btn btn-success btn-sm" onClick={() => setNova('RECEITA')}>
              <Icone nome="add" /> Receita
            </button>
            <button type="button" className="btn btn-primary btn-sm" onClick={() => setNova('DESPESA')}>
              <Icone nome="remove" /> Despesa
            </button>
          </>
        }
      />

      <form className="cartao filtros" role="search" aria-label="Filtrar movimentações" onSubmit={(e) => e.preventDefault()}>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-inicio`}>De</label>
          <input id={`${id}-inicio`} type="date" className="form-control" value={filtros.dataInicio ?? ''}
            onChange={(e) => alterar('dataInicio', e.target.value)} />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-fim`}>Até</label>
          <input id={`${id}-fim`} type="date" className="form-control" value={filtros.dataFim ?? ''}
            onChange={(e) => alterar('dataFim', e.target.value)} />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-tipo`}>Tipo</label>
          <select id={`${id}-tipo`} className="form-control" value={filtros.tipo ?? ''}
            onChange={(e) => setFiltros((f) => ({ ...f, tipo: (e.target.value || undefined) as TipoMovimentacao | undefined, categoriaId: undefined }))}>
            <option value="">Todos</option>
            <option value="RECEITA">Receitas</option>
            <option value="DESPESA">Despesas</option>
          </select>
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-categoria`}>Categoria</label>
          <select id={`${id}-categoria`} className="form-control" value={filtros.categoriaId ?? ''}
            onChange={(e) => alterar('categoriaId', e.target.value)}>
            <option value="">Todas</option>
            {categoriasFiltradas.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-conta`}>Conta</label>
          <select id={`${id}-conta`} className="form-control" value={filtros.contaId ?? ''}
            onChange={(e) => alterar('contaId', e.target.value)}>
            <option value="">Todas</option>
            {contas.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
          </select>
        </div>
        <div className="form-group filtros-busca">
          <label className="form-label" htmlFor={`${id}-texto`}>Descrição</label>
          <input id={`${id}-texto`} type="search" className="form-control" placeholder="Buscar…" value={filtros.texto ?? ''}
            onChange={(e) => alterar('texto', e.target.value)} />
        </div>
        <button type="button" className="btn btn-ghost btn-sm filtros-limpar"
          onClick={() => setFiltros({ dataInicio: mesAtual.inicio, dataFim: mesAtual.fim })}>
          Limpar filtros
        </button>
      </form>

      <div className="resumo-filtro" aria-live="polite">
        <span>{movimentacoes.length} movimentação(ões)</span>
        <span className="valor-positivo">Receitas: {formatarMoeda(totalReceitas)}</span>
        <span className="valor-negativo">Despesas: {formatarMoeda(totalDespesas)}</span>
      </div>

      {consulta.isLoading ? (
        <LoadingSpinner />
      ) : consulta.isError ? (
        <ErroCarregamento mensagem={mensagemDeErro(consulta.error)} onTentar={() => void consulta.refetch()} />
      ) : movimentacoes.length === 0 ? (
        <EstadoVazio icone="receipt_long" titulo="Nenhuma movimentação encontrada"
          texto="Ajuste os filtros ou registre uma nova receita ou despesa." />
      ) : (
        <div className="cartao cartao-tabela">
          <table className="tabela tabela-responsiva">
            <caption className="sr-only">Movimentações filtradas</caption>
            <thead>
              <tr>
                <th scope="col">Data</th>
                <th scope="col">Descrição</th>
                <th scope="col">Categoria</th>
                <th scope="col">Conta</th>
                <th scope="col" className="col-valor">Valor</th>
                <th scope="col" className="col-acoes"><span className="sr-only">Ações</span></th>
              </tr>
            </thead>
            <tbody>
              {movimentacoes.map((m) => (
                <tr key={m.id}>
                  <td data-rotulo="Data">{formatarData(m.data)}</td>
                  <td data-rotulo="Descrição" className="celula-destaque">{m.descricao}</td>
                  <td data-rotulo="Categoria"><span className={`chip chip-${m.tipo.toLowerCase()}`}>{m.categoriaNome}</span></td>
                  <td data-rotulo="Conta">{m.contaNome}</td>
                  <td data-rotulo="Valor" className={`col-valor ${m.tipo === 'RECEITA' ? 'valor-positivo' : 'valor-negativo'}`}>
                    {m.tipo === 'RECEITA' ? '+ ' : '− '}{formatarMoeda(m.valor)}
                  </td>
                  <td className="col-acoes">
                    <button type="button" className="btn-icone" onClick={() => setEditando(m)} aria-label={`Editar ${m.descricao}`}>
                      <Icone nome="edit" />
                    </button>
                    <button type="button" className="btn-icone btn-icone-perigo" onClick={() => setExcluindo(m)} aria-label={`Excluir ${m.descricao}`}>
                      <Icone nome="delete" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <MovimentacaoModal aberto={nova !== null} tipoInicial={nova ?? 'DESPESA'} onClose={() => setNova(null)} />
      <MovimentacaoModal aberto={editando !== null} movimentacao={editando} onClose={() => setEditando(null)} />
      <ConfirmModal
        isOpen={excluindo !== null}
        titulo="Excluir movimentação"
        textoConfirmar="Excluir"
        perigo
        carregando={excluir.isPending}
        onClose={() => setExcluindo(null)}
        onConfirmar={() => excluindo && excluir.mutate(excluindo)}
      >
        Excluir <strong>{excluindo?.descricao}</strong> ({formatarMoeda(excluindo?.valor)})? O saldo será recalculado.
      </ConfirmModal>
    </div>
  );
}
