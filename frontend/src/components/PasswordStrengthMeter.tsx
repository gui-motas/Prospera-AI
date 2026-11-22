import { useMemo } from 'react';
import { TAMANHO_MINIMO_SENHA, getPasswordScore, getPasswordStrength } from '../utils/password';

/**
 * Indicador visual da força da senha. A regra que vale é a de `isPasswordValid`
 * (mesma do backend); aqui só há dica, nunca bloqueio.
 */
export function PasswordStrengthMeter({ password }: { password: string }) {
  const { score, label, feedback } = useMemo(() => {
    const pontos = getPasswordScore(password);
    const dicas: string[] = [];
    if (password.length < TAMANHO_MINIMO_SENHA) dicas.push(`Use pelo menos ${TAMANHO_MINIMO_SENHA} caracteres`);
    if (!/[a-z]/.test(password) || !/[A-Z]/.test(password)) dicas.push('Combine letras maiúsculas e minúsculas');
    if (!/\d/.test(password)) dicas.push('Adicione números');
    if (!/[^A-Za-z0-9]/.test(password)) dicas.push('Caracteres especiais (!@#$…) deixam a senha mais forte');
    return { score: pontos, label: getPasswordStrength(pontos), feedback: dicas };
  }, [password]);

  if (!password) return null;

  const cor = ['var(--texto-perigo)', '#f59e0b', '#fbbf24', '#10b981', '#059669'][score] ?? 'var(--borda)';

  return (
    <div className="forca-senha">
      <div className="forca-senha-barras" aria-hidden="true">
        {[1, 2, 3, 4].map((nivel) => (
          <span key={nivel} className="forca-senha-barra" style={{ backgroundColor: nivel <= score ? cor : 'var(--borda)' }} />
        ))}
      </div>
      <p className="forca-senha-rotulo" style={{ color: cor }}>{label}</p>
      {feedback.length > 0 && score < 3 && (
        <ul className="forca-senha-dicas">
          {feedback.map((dica) => <li key={dica}>{dica}</li>)}
        </ul>
      )}
    </div>
  );
}
