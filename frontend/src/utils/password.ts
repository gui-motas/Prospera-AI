/**
 * Força da senha (0 a 4) exibida no cadastro.
 * A regra mínima aceita pelo backend é: 8 caracteres, com letras e números.
 */
export function getPasswordScore(password: string): number {
  if (!password) return 0;
  let score = 0;
  if (password.length >= 8) score++;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
  if (/\d/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  return score;
}

export function getPasswordStrength(score: number): string {
  const levels = ['Muito fraca', 'Fraca', 'Média', 'Forte', 'Muito forte'];
  return levels[score] ?? levels[0];
}

/** Mesma regra do backend (ValidadorSenha). */
export function isPasswordValid(password: string): boolean {
  return password.length >= 8 && /[A-Za-zÀ-ÿ]/.test(password) && /\d/.test(password);
}
