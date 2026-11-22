interface LoadingSpinnerProps {
  telaCheia?: boolean;
  texto?: string;
}

export function LoadingSpinner({ telaCheia = false, texto = 'Carregando…' }: LoadingSpinnerProps) {
  return (
    <div className={telaCheia ? 'carregando carregando-tela' : 'carregando'} role="status" aria-live="polite">
      <div className="loading-spinner" aria-hidden="true" />
      <span className="sr-only">{texto}</span>
    </div>
  );
}
