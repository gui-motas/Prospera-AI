import type { ReactNode } from 'react';
import { Icone } from './Icone';

/** Cabeçalho padrão das páginas internas. */
export function PageHeader({ titulo, descricao, acoes }: { titulo: string; descricao?: string; acoes?: ReactNode }) {
  return (
    <header className="page-header">
      <div>
        <h1 className="page-title">{titulo}</h1>
        {descricao && <p className="page-desc">{descricao}</p>}
      </div>
      {acoes && <div className="page-acoes">{acoes}</div>}
    </header>
  );
}

/** Mensagem para listas vazias, com ação opcional. */
export function EstadoVazio({ icone, titulo, texto, acao }: { icone: string; titulo: string; texto?: string; acao?: ReactNode }) {
  return (
    <div className="estado-vazio">
      <span className="estado-vazio-icone">
        <Icone nome={icone} tamanho={32} />
      </span>
      <p className="estado-vazio-titulo">{titulo}</p>
      {texto && <p className="text-muted text-sm">{texto}</p>}
      {acao}
    </div>
  );
}

/** Barra de progresso acessível (valor de 0 a 100). */
export function BarraProgresso({
  valor,
  rotulo,
  variante = 'primaria',
  marcador,
}: {
  valor: number;
  rotulo: string;
  variante?: 'primaria' | 'sucesso' | 'alerta' | 'perigo';
  /** Posição (0–100) de um marcador, ex.: progresso esperado pelo prazo. */
  marcador?: number;
}) {
  const largura = Math.max(0, Math.min(100, valor));
  return (
    <div
      className="barra-progresso"
      role="progressbar"
      aria-label={rotulo}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={Math.round(largura)}
    >
      <div className={`barra-progresso-preenchimento barra-${variante}`} style={{ width: `${largura}%` }} />
      {marcador !== undefined && (
        <span className="barra-progresso-marcador" style={{ left: `${Math.max(0, Math.min(100, marcador))}%` }} />
      )}
    </div>
  );
}

/** Aviso de erro com opção de tentar novamente. */
export function ErroCarregamento({ mensagem, onTentar }: { mensagem: string; onTentar?: () => void }) {
  return (
    <div className="aviso aviso-perigo" role="alert">
      <Icone nome="error" />
      <span>{mensagem}</span>
      {onTentar && (
        <button type="button" className="btn btn-sm btn-secondary" onClick={onTentar}>
          Tentar novamente
        </button>
      )}
    </div>
  );
}
