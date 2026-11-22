import { formatarMoeda } from '../../utils/formatacao';
import { Icone } from '../Icone';

interface HeroSaldoProps {
  saldo: number;
  onReceita: () => void;
  onDespesa: () => void;
  onMeta: () => void;
}

/** Saldo total calculado pela plataforma (RN-004) com atalhos de registro. */
export function HeroSaldo({ saldo, onReceita, onDespesa, onMeta }: HeroSaldoProps) {
  return (
    <section className="gradiente-saldo" aria-labelledby="titulo-saldo">
      <p id="titulo-saldo" className="hero-rotulo">Saldo total</p>
      <p className={`hero-saldo ${saldo < 0 ? 'negativo' : ''}`}>{formatarMoeda(saldo)}</p>

      <div className="hero-actions-grid">
        <div className="hero-acao">
          <button type="button" className="action-btn-circle btn-primary-solid" onClick={onReceita} aria-label="Registrar receita">
            <Icone nome="add" tamanho={28} />
          </button>
          <span aria-hidden="true">Receita</span>
        </div>
        <div className="hero-acao">
          <button type="button" className="action-btn-circle btn-surface" onClick={onDespesa} aria-label="Registrar despesa">
            <Icone nome="remove" tamanho={28} />
          </button>
          <span aria-hidden="true">Despesa</span>
        </div>
        <div className="hero-acao">
          <button type="button" className="action-btn-circle btn-surface" onClick={onMeta} aria-label="Criar meta financeira">
            <Icone nome="flag" tamanho={28} />
          </button>
          <span aria-hidden="true">Nova meta</span>
        </div>
      </div>
    </section>
  );
}
