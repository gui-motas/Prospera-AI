import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useAcao } from '../../hooks/useAcao';
import { useCategorias } from '../../hooks/useDados';
import { orcamentoService } from '../../services/servicos';
import type { Orcamento, OrcamentoRequest } from '../../services/tipos';
import { periodoDoMes } from '../../utils/formatacao';
import { BaseModal } from '../BaseModal';
import { CurrencyInput } from '../CurrencyInput';

interface OrcamentoModalProps {
  aberto: boolean;
  onClose: () => void;
  orcamento?: Orcamento | null;
}

/** Definição de limite de gastos por categoria e período (RN-005). */
export function OrcamentoModal({ aberto, onClose, orcamento }: OrcamentoModalProps) {
  return (
    <BaseModal isOpen={aberto} onClose={onClose} title={orcamento ? 'Editar limite de gastos' : 'Novo limite de gastos'} largura="sm">
      {aberto && <Formulario orcamento={orcamento ?? null} onClose={onClose} />}
    </BaseModal>
  );
}

function Formulario({ orcamento, onClose }: { orcamento: Orcamento | null; onClose: () => void }) {
  const id = useId();
  const { data: categorias = [] } = useCategorias();
  const despesas = categorias.filter((c) => c.tipo === 'DESPESA');
  const mesAtual = periodoDoMes();

  const [categoriaId, setCategoriaId] = useState(orcamento?.categoriaId ?? '');
  const [limite, setLimite] = useState(orcamento ? String(orcamento.limite) : '');
  const [inicio, setInicio] = useState(orcamento?.inicioPeriodo ?? mesAtual.inicio);
  const [fim, setFim] = useState(orcamento?.fimPeriodo ?? mesAtual.fim);
  const [erro, setErro] = useState('');

  const categoriaEscolhida = despesas.some((c) => c.id === categoriaId) ? categoriaId : (despesas[0]?.id ?? '');

  const salvar = useAcao(
    (dados: OrcamentoRequest) => (orcamento ? orcamentoService.editar(orcamento.id, dados) : orcamentoService.criar(dados)),
    orcamento ? 'Limite atualizado.' : 'Limite definido.',
    onClose,
  );

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    const valor = Number(limite);
    if (!(valor > 0)) {
      setErro('O limite deve ser maior que zero.');
      return;
    }
    if (fim < inicio) {
      setErro('O fim do período não pode ser anterior ao início.');
      return;
    }
    setErro('');
    salvar.mutate({ categoriaId: categoriaEscolhida, limite: valor, inicioPeriodo: inicio, fimPeriodo: fim });
  };

  if (despesas.length === 0) {
    return (
      <p className="text-muted">
        Crie uma categoria de despesa para definir limites. <Link to="/categorias" onClick={onClose}>Ir para Categorias</Link>
      </p>
    );
  }

  return (
    <form onSubmit={enviar} className="form-grid" noValidate>
      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-categoria`}>Categoria de despesa</label>
        <select id={`${id}-categoria`} className="form-control" value={categoriaEscolhida} onChange={(e) => setCategoriaId(e.target.value)}>
          {despesas.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
        </select>
      </div>
      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-limite`}>Limite (R$)</label>
        <CurrencyInput id={`${id}-limite`} className="form-control" value={limite} onValueChange={setLimite} placeholder="0,00" required autoFocus />
      </div>
      <div className="form-linha">
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-inicio`}>Início</label>
          <input id={`${id}-inicio`} type="date" className="form-control" value={inicio} onChange={(e) => setInicio(e.target.value)} required />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-fim`}>Fim</label>
          <input id={`${id}-fim`} type="date" className="form-control" value={fim} onChange={(e) => setFim(e.target.value)} required />
        </div>
      </div>
      {erro && <p className="form-erro" role="alert">{erro}</p>}
      <button type="submit" className="btn btn-primary w-100" disabled={salvar.isPending}>
        {salvar.isPending ? 'Salvando…' : 'Salvar limite'}
      </button>
    </form>
  );
}
