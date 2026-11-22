import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import { useAcao } from '../../hooks/useAcao';
import { contaService } from '../../services/servicos';
import type { ContaFinanceira, ContaRequest, TipoConta } from '../../services/tipos';
import { ROTULO_TIPO_CONTA } from '../../utils/formatacao';
import { BaseModal } from '../BaseModal';
import { CurrencyInput } from '../CurrencyInput';

interface ContaModalProps {
  aberto: boolean;
  onClose: () => void;
  conta?: ContaFinanceira | null;
}

/** RF-003 – cadastrar e editar contas financeiras. */
export function ContaModal({ aberto, onClose, conta }: ContaModalProps) {
  return (
    <BaseModal isOpen={aberto} onClose={onClose} title={conta ? 'Editar conta' : 'Nova conta'} largura="sm">
      {aberto && <Formulario conta={conta ?? null} onClose={onClose} />}
    </BaseModal>
  );
}

function Formulario({ conta, onClose }: { conta: ContaFinanceira | null; onClose: () => void }) {
  const id = useId();
  const [nome, setNome] = useState(conta?.nome ?? '');
  const [tipo, setTipo] = useState<TipoConta>(conta?.tipo ?? 'CONTA_CORRENTE');
  const [saldoInicial, setSaldoInicial] = useState(conta ? String(conta.saldoInicial) : '');

  const salvar = useAcao(
    (dados: ContaRequest) => (conta ? contaService.editar(conta.id, dados) : contaService.criar(dados)),
    conta ? 'Conta atualizada.' : 'Conta cadastrada.',
    onClose,
  );

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    salvar.mutate({ nome: nome.trim(), tipo, saldoInicial: Number(saldoInicial || 0) });
  };

  return (
    <form onSubmit={enviar} className="form-grid">
      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-nome`}>Nome da conta</label>
        <input id={`${id}-nome`} className="form-control" value={nome} onChange={(e) => setNome(e.target.value)}
          placeholder="Ex.: Banco, Carteira, Poupança" maxLength={80} required autoFocus />
      </div>
      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-tipo`}>Tipo</label>
        <select id={`${id}-tipo`} className="form-control" value={tipo} onChange={(e) => setTipo(e.target.value as TipoConta)}>
          {(Object.keys(ROTULO_TIPO_CONTA) as TipoConta[]).map((t) => (
            <option key={t} value={t}>{ROTULO_TIPO_CONTA[t]}</option>
          ))}
        </select>
      </div>
      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-saldo`}>Saldo inicial (R$)</label>
        <CurrencyInput id={`${id}-saldo`} className="form-control" value={saldoInicial} onValueChange={setSaldoInicial} placeholder="0,00" />
        <p className="form-ajuda">O saldo atual é calculado pela plataforma: saldo inicial + receitas − despesas.</p>
      </div>
      <button type="submit" className="btn btn-primary w-100" disabled={salvar.isPending}>
        {salvar.isPending ? 'Salvando…' : 'Salvar conta'}
      </button>
    </form>
  );
}
