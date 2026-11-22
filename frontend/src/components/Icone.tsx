interface IconeProps {
  nome: string;
  tamanho?: number;
  preenchido?: boolean;
  className?: string;
}

/** Ícone Material Symbols. Decorativo: o texto acessível fica no elemento que o contém (RNF-007). */
export function Icone({ nome, tamanho, preenchido = false, className = '' }: IconeProps) {
  return (
    <span
      className={`material-symbols-outlined ${preenchido ? 'icone-preenchido' : ''} ${className}`.trim()}
      style={tamanho ? { fontSize: `${tamanho}px` } : undefined}
      aria-hidden="true"
    >
      {nome}
    </span>
  );
}
