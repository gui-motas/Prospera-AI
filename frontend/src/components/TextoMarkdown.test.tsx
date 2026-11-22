import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TextoMarkdown } from './TextoMarkdown';

describe('TextoMarkdown', () => {
  it('renderiza negrito, títulos e listas sem exibir os marcadores', () => {
    const { container } = render(
      <TextoMarkdown texto={'**Padrões de consumo**\n- Gasto em **Lazer** alto\n- Segundo\n\n1. Um\n2. Dois'} />,
    );
    expect(screen.getByRole('heading', { name: 'Padrões de consumo' })).toBeInTheDocument();
    expect(container.querySelectorAll('ul li')).toHaveLength(2);
    expect(container.querySelectorAll('ol li')).toHaveLength(2);
    expect(container.querySelector('strong')?.textContent).toBe('Lazer');
    expect(container.textContent).not.toContain('**');
  });

  it('não interpreta HTML do texto', () => {
    const { container } = render(<TextoMarkdown texto={'<img src=x onerror=alert(1)>'} />);
    expect(container.querySelector('img')).toBeNull();
  });
});
