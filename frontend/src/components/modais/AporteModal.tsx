import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import { useAcao } from '../../hooks/useAcao';
import { metaService } from '../../services/servicos';
import type { MetaFinanceira, OperacaoAporte } from '../../services/tipos';
import { formatarMoeda } from '../../utils/formatacao';
import { BaseModal } from '../BaseModal';
import { CurrencyInput } from '../CurrencyInput';

interface AporteModalProps {
  meta: MetaFinanceira | null;
  operacao: OperacaoAporte;
  onClose: () => void;
}

/** Registro do valor guardado (ou retirado) da meta — sempre por ação explícita do usuário (RN-011). */
export function AporteModal({ meta, operacao, onClose }: AporteModalProps) {
  const titulo = operacao === 'APORTE' ? 'Guardar dinheiro na meta' : 'Retirar dinheiro da meta';
  return (
    <BaseModal isOpen={meta !== null} onClose={onClose} title={titulo} largura="sm">
      {meta && <Formulario meta={meta} operacao={operacao} onClose={onClose} />}
    </BaseModal>
  );
}

function Formulario({ meta, operacao, onClose }: { meta: MetaFinanceira; operacao: OperacaoAporte; onClose: () => void }) {
  const id = useId();
  const [valor, setValor] = useState('');
  const [erro, setErro] = useState('');

  const salvar = useAcao(
    (numero: number) => metaService.registrarAporte(meta.id, { operacao, valor: numero }),
    operacao === 'APORTE' ? 'Valor guardado na meta.' : 'Valor retirado da meta.',
    onClose,
  );

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    const numero = Number(valor);
    if (!(numero > 0)) {
      setErro('Informe um valor maior que zero.');
      return;
    }
    if (operacao === 'RESGATE' && numero > meta.valorAcumulado) {
      setErro('O valor não pode ser maior que o acumulado na meta.');
      return;
    }
    setErro('');
    salvar.mutate(numero);
  };

  return (
    <form onSubmit={enviar} className="form-grid" noValidate>
      <div className="resumo-meta-modal">
        <strong>{meta.nome}</strong>
        <span className="text-sm text-muted">
          Acumulado: {formatarMoeda(meta.valorAcumulado)} de {formatarMoeda(meta.valorDesejado)}
        </span>
      </div>
      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-valor`}>Valor (R$)</label>
        <CurrencyInput id={`${id}-valor`} className="form-control input-valor" value={valor} onValueChange={setValor} placeholder="0,00" autoFocus required />
      </div>
      {erro && <p className="form-erro" role="alert">{erro}</p>}
      <button type="submit" className={`btn w-100 ${operacao === 'APORTE' ? 'btn-success' : 'btn-secondary'}`} disabled={salvar.isPending}>
        {salvar.isPending ? 'Salvando…' : operacao === 'APORTE' ? 'Guardar' : 'Retirar'}
      </button>
    </form>
  );
}
