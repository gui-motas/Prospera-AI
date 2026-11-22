import type { TipoMovimentacao } from '../../services/tipos';
import { Icone } from '../Icone';

interface SeletorTipoProps {
  valor: TipoMovimentacao;
  onChange: (tipo: TipoMovimentacao) => void;
  legenda: string;
}

/** Escolha entre receita e despesa como grupo de opções acessível. */
export function SeletorTipo({ valor, onChange, legenda }: SeletorTipoProps) {
  return (
    <fieldset className="seletor-tipo">
      <legend className="form-label">{legenda}</legend>
      <div className="seletor-tipo-opcoes">
        {(['DESPESA', 'RECEITA'] as const).map((tipo) => (
          <label key={tipo} className={`seletor-tipo-opcao ${valor === tipo ? `selecionado ${tipo.toLowerCase()}` : ''}`}>
            <input
              type="radio"
              name={legenda}
              value={tipo}
              checked={valor === tipo}
              onChange={() => onChange(tipo)}
            />
            <Icone nome={tipo === 'RECEITA' ? 'arrow_downward' : 'arrow_upward'} />
            {tipo === 'RECEITA' ? 'Receita' : 'Despesa'}
          </label>
        ))}
      </div>
    </fieldset>
  );
}
