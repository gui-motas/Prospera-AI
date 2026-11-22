import { Link } from 'react-router-dom';
import type { ContaFinanceira } from '../../services/tipos';
import { ICONE_TIPO_CONTA, ROTULO_TIPO_CONTA, formatarMoeda } from '../../utils/formatacao';
import { Icone } from '../Icone';

/** Cartões das contas com o saldo calculado de cada uma (RF-003, RN-004). */
export function ContasSlider({ contas }: { contas: ContaFinanceira[] }) {
  return (
    <section className="secao" aria-labelledby="titulo-contas">
      <div className="section-title">
        <h2 id="titulo-contas" className="secao-titulo">Minhas contas</h2>
        <Link to="/contas" className="link-acao">Gerenciar</Link>
      </div>

      <div className="accounts-slider">
        {contas.length === 0 ? (
          <p className="text-muted text-sm">Nenhuma conta cadastrada.</p>
        ) : (
          contas.map((conta, indice) => (
            <article key={conta.id} className={`account-card ${indice % 2 === 0 ? 'card-dark' : 'card-gradient'}`}>
              <span className="account-card-icone" aria-hidden="true">
                <Icone nome={ICONE_TIPO_CONTA[conta.tipo]} tamanho={60} />
              </span>
              <div>
                <h3 className="account-card-nome">{conta.nome}</h3>
                <p className="account-card-saldo">{formatarMoeda(conta.saldoAtual)}</p>
              </div>
              <span className="account-card-tipo">{ROTULO_TIPO_CONTA[conta.tipo]}</span>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
