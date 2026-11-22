import { useTheme } from '../contexts/ThemeContext';
import { Icone } from './Icone';

export function ThemeToggle({ className = 'header-btn' }: { className?: string }) {
  const { theme, toggleTheme } = useTheme();
  const rotulo = `Alternar para tema ${theme === 'light' ? 'escuro' : 'claro'}`;

  return (
    <button type="button" onClick={toggleTheme} className={className} aria-label={rotulo} title={rotulo}>
      <Icone nome={theme === 'light' ? 'dark_mode' : 'light_mode'} />
    </button>
  );
}
