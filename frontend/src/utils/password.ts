/**
 * Regra de senha do frontend, espelhando ValidadorSenha (backend):
 * mínimo de 8 caracteres, com letras e números.
 *
 * A pontuação é só um indicador visual; ela nunca libera nem bloqueia o envio.
 */
export const TAMANHO_MINIMO_SENHA = 8;

export function isPasswordValid(password: string): boolean {
  return password.length >= TAMANHO_MINIMO_SENHA && /[A-Za-zÀ-ÿ]/.test(password) && /\d/.test(password);
}

/** 0 a 4, para a barra de força. */
export function getPasswordScore(password: string): number {
  if (!password) return 0;
  let score = 0;
  if (password.length >= TAMANHO_MINIMO_SENHA) score++;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
  if (/\d/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  return score;
}

export function getPasswordStrength(score: number): string {
  const levels = ['Muito fraca', 'Fraca', 'Média', 'Forte', 'Muito forte'];
  return levels[score] ?? levels[0];
}
