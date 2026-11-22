interface PasswordStrengthMeterProps {
  password: string;
}

interface PasswordStrength {
  score: number; // 0-4
  label: string;
  color: string;
  feedback: string[];
}

/**
 * Componente que exibe um medidor visual da força da senha
 */
export function PasswordStrengthMeter({ password }: PasswordStrengthMeterProps) {
  const getPasswordStrength = (pwd: string): PasswordStrength => {
    if (!pwd) {
      return { score: 0, label: '', color: '', feedback: [] };
    }

    let score = 0;
    const feedback: string[] = [];

    // Critério 1: Tamanho (mínimo 8 caracteres)
    if (pwd.length >= 8) {
      score++;
    } else {
      feedback.push('Use pelo menos 8 caracteres');
    }

    // Critério 2: Letras maiúsculas e minúsculas
    if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) {
      score++;
    } else {
      feedback.push('Combine letras maiúsculas e minúsculas');
    }

    // Critério 3: Números
    if (/\d/.test(pwd)) {
      score++;
    } else {
      feedback.push('Adicione números');
    }

    // Critério 4: Caracteres especiais
    // `[` no início do character class não precisa de escape.
    if (/[[!@#$%^&*()_+\-=\]{};':"\\|,.<>/?]/.test(pwd)) {
      score++;
    } else {
      feedback.push('Adicione caracteres especiais (!@#$%...)');
    }

    // Determinar label e cor baseado no score
    const strengths = [
      { label: 'Muito fraca', color: 'var(--texto-perigo)' },
      { label: 'Fraca', color: '#f59e0b' },
      { label: 'Média', color: '#fbbf24' },
      { label: 'Forte', color: '#10b981' },
      { label: 'Muito forte', color: '#059669' }
    ];

    return {
      score,
      label: strengths[score].label,
      color: strengths[score].color,
      feedback
    };
  };

  const strength = getPasswordStrength(password);

  if (!password) return null;

  return (
    <div style={{ marginTop: '0.75rem' }}>
      {/* Barras de força */}
      <div style={{
        display: 'flex',
        gap: '4px',
        marginBottom: '0.5rem'
      }}>
        {[1, 2, 3, 4].map((level) => (
          <div
            key={level}
            style={{
              flex: 1,
              height: '4px',
              borderRadius: '2px',
              backgroundColor: level <= strength.score
                ? strength.color
                : 'var(--borda)',
              transition: 'all 0.3s ease'
            }}
          />
        ))}
      </div>

      {/* Label de força */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        fontSize: '0.875rem'
      }}>
        <span style={{
          color: strength.color,
          fontWeight: 600
        }}>
          {strength.label}
        </span>
        {strength.score >= 3 && (
          <span style={{ color: 'var(--texto-sucesso)' }}>✓</span>
        )}
      </div>

      {/* Feedback de melhorias */}
      {strength.feedback.length > 0 && strength.score < 3 && (
        <div style={{
          marginTop: '0.5rem',
          fontSize: '0.75rem',
          color: 'var(--texto-fraco)'
        }}>
          {strength.feedback.map((tip, idx) => (
            <div key={idx} style={{ marginBottom: '0.25rem' }}>
              • {tip}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
