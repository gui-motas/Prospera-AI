import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useAcao } from '../../hooks/useAcao';
import { useCategorias, useContas } from '../../hooks/useDados';
import { movimentacaoService } from '../../services/servicos';
import type { Movimentacao, MovimentacaoRequest, TipoMovimentacao } from '../../services/tipos';
import { hojeIso } from '../../utils/formatacao';
import { BaseModal } from '../BaseModal';
import { CurrencyInput } from '../CurrencyInput';
import { SeletorTipo } from './SeletorTipo';

interface MovimentacaoModalProps {
  aberto: boolean;
  onClose: () => void;
  /** Quando informado, o formulário edita a movimentação (RF-006). */
  movimentacao?: Movimentacao | null;
  tipoInicial?: TipoMovimentacao;
}

/** RF-005 – Registro de despesas (e receitas) / RF-006 – edição de movimentações. */
export function MovimentacaoModal({ aberto, onClose, movimentacao, tipoInicial = 'DESPESA' }: MovimentacaoModalProps) {
  const tipo = movimentacao?.tipo ?? tipoInicial;
  const titulo = movimentacao ? 'Editar movimentação' : tipo === 'RECEITA' ? 'Nova receita' : 'Nova despesa';
  return (
    <BaseModal isOpen={aberto} onClose={onClose} title={titulo}>
      {aberto && <Formulario movimentacao={movimentacao ?? null} tipoInicial={tipo} onClose={onClose} />}
    </BaseModal>
  );
}

function Formulario({ movimentacao, tipoInicial, onClose }: {
  movimentacao: Movimentacao | null;
  tipoInicial: TipoMovimentacao;
  onClose: () => void;
}) {
  const id = useId();
  const { data: contas = [] } = useContas();
  const { data: categorias = [] } = useCategorias();

  const [tipo, setTipo] = useState<TipoMovimentacao>(tipoInicial);
  const [descricao, setDescricao] = useState(movimentacao?.descricao ?? '');
  const [valor, setValor] = useState(movimentacao ? String(movimentacao.valor) : '');
  const [data, setData] = useState(movimentacao?.data ?? hojeIso());
  const [contaId, setContaId] = useState(movimentacao?.contaId ?? '');
  const [categoriaId, setCategoriaId] = useState(movimentacao?.categoriaId ?? '');
  const [erro, setErro] = useState('');

  // RN-003: só são oferecidas categorias compatíveis com o tipo escolhido
  const categoriasDoTipo = categorias.filter((c) => c.tipo === tipo);
  const contaEscolhida = contas.some((c) => c.id === contaId) ? contaId : (contas[0]?.id ?? '');
  const categoriaEscolhida = categoriasDoTipo.some((c) => c.id === categoriaId)
    ? categoriaId
    : (categoriasDoTipo[0]?.id ?? '');

  const salvar = useAcao(
    (dados: MovimentacaoRequest) =>
      movimentacao ? movimentacaoService.editar(movimentacao.id, dados) : movimentacaoService.registrar(dados),
    movimentacao ? 'Movimentação atualizada.' : 'Movimentação registrada.',
    onClose,
  );

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    const numero = Number(valor);
    if (!(numero > 0)) {
      setErro('O valor deve ser maior que zero.'); // RN-002
      return;
    }
    if (!contaEscolhida || !categoriaEscolhida) {
      setErro('Selecione a conta e a categoria.');
      return;
    }
    setErro('');
    salvar.mutate({ tipo, descricao: descricao.trim(), valor: numero, data, contaId: contaEscolhida, categoriaId: categoriaEscolhida });
  };

  if (contas.length === 0) {
    return (
      <p className="text-muted">
        Cadastre uma conta antes de registrar movimentações. <Link to="/contas" onClick={onClose}>Ir para Contas</Link>
      </p>
    );
  }

  return (
    <form onSubmit={enviar} className="form-grid" noValidate>
      <SeletorTipo valor={tipo} onChange={setTipo} legenda="Tipo da movimentação" />

      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-valor`}>Valor (R$)</label>
        <CurrencyInput
          id={`${id}-valor`}
          className={`form-control input-valor ${tipo === 'RECEITA' ? 'valor-receita' : 'valor-despesa'}`}
          value={valor}
          onValueChange={setValor}
          placeholder="0,00"
          required
          autoFocus
        />
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-descricao`}>Descrição</label>
        <input
          id={`${id}-descricao`}
          className="form-control"
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
          placeholder={tipo === 'RECEITA' ? 'Ex.: Salário' : 'Ex.: Mercado'}
          maxLength={140}
          required
        />
      </div>

      <div className="form-linha">
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-data`}>Data</label>
          <input id={`${id}-data`} className="form-control" type="date" value={data} onChange={(e) => setData(e.target.value)} required />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-conta`}>Conta</label>
          <select id={`${id}-conta`} className="form-control" value={contaEscolhida} onChange={(e) => setContaId(e.target.value)}>
            {contas.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
          </select>
        </div>
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-categoria`}>Categoria</label>
        {categoriasDoTipo.length === 0 ? (
          <p className="text-sm text-muted m-0">
            Nenhuma categoria de {tipo === 'RECEITA' ? 'receita' : 'despesa'}.{' '}
            <Link to="/categorias" onClick={onClose}>Criar categoria</Link>
          </p>
        ) : (
          <select id={`${id}-categoria`} className="form-control" value={categoriaEscolhida} onChange={(e) => setCategoriaId(e.target.value)}>
            {categoriasDoTipo.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
          </select>
        )}
      </div>

      {erro && <p className="form-erro" role="alert">{erro}</p>}

      <button type="submit" className={`btn w-100 ${tipo === 'RECEITA' ? 'btn-success' : 'btn-primary'}`} disabled={salvar.isPending}>
        {salvar.isPending ? 'Salvando…' : 'Salvar'}
      </button>
    </form>
  );
}
