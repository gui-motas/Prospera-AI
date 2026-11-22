import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import { useAcao } from '../../hooks/useAcao';
import { metaService } from '../../services/servicos';
import type { MetaFinanceira, MetaRequest } from '../../services/tipos';
import { hojeIso } from '../../utils/formatacao';
import { BaseModal } from '../BaseModal';
import { CurrencyInput } from '../CurrencyInput';

interface MetaModalProps {
  aberto: boolean;
  onClose: () => void;
  meta?: MetaFinanceira | null;
}

/** RF-08 – nome, valor desejado e prazo são obrigatórios (RN-006, RN-007). */
export function MetaModal({ aberto, onClose, meta }: MetaModalProps) {
  return (
    <BaseModal isOpen={aberto} onClose={onClose} title={meta ? 'Editar meta' : 'Nova meta financeira'} largura="sm">
      {aberto && <Formulario meta={meta ?? null} onClose={onClose} />}
    </BaseModal>
  );
}

function Formulario({ meta, onClose }: { meta: MetaFinanceira | null; onClose: () => void }) {
  const id = useId();
  const [nome, setNome] = useState(meta?.nome ?? '');
  const [valor, setValor] = useState(meta ? String(meta.valorDesejado) : '');
  const [prazo, setPrazo] = useState(meta?.prazo ?? '');
  const [erro, setErro] = useState('');

  const salvar = useAcao(
    (dados: MetaRequest) => (meta ? metaService.editar(meta.id, dados) : metaService.criar(dados)),
    meta ? 'Meta atualizada.' : 'Meta criada.',
    onClose,
  );

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    const numero = Number(valor);
    if (!nome.trim() || !prazo) {
      setErro('Informe o nome e o prazo da meta.');
      return;
    }
    if (!(numero > 0)) {
      setErro('O valor da meta deve ser maior que zero.');
      return;
    }
    if (!meta && prazo <= hojeIso()) {
      setErro('O prazo deve ser uma data futura.');
      return;
    }
    setErro('');
    salvar.mutate({ nome: nome.trim(), valorDesejado: numero, prazo });
  };

  return (
    <form onSubmit={enviar} className="form-grid" noValidate>
      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-nome`}>Nome da meta</label>
        <input id={`${id}-nome`} className="form-control" value={nome} onChange={(e) => setNome(e.target.value)}
          placeholder="Ex.: Reserva de emergência, Viagem" maxLength={120} required autoFocus />
      </div>
      <div className="form-linha">
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-valor`}>Valor desejado (R$)</label>
          <CurrencyInput id={`${id}-valor`} className="form-control" value={valor} onValueChange={setValor} placeholder="0,00" required />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-prazo`}>Prazo para conclusão</label>
          <input id={`${id}-prazo`} type="date" className="form-control" value={prazo} min={meta ? undefined : hojeIso()}
            onChange={(e) => setPrazo(e.target.value)} required />
        </div>
      </div>
      {erro && <p className="form-erro" role="alert">{erro}</p>}
      <button type="submit" className="btn btn-primary w-100" disabled={salvar.isPending}>
        {salvar.isPending ? 'Salvando…' : 'Salvar meta'}
      </button>
    </form>
  );
}
