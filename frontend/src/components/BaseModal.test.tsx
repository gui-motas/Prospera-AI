import { describe, expect, it } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { useState } from 'react';
import { BaseModal } from './BaseModal';

function Exemplo() {
  const [aberto, setAberto] = useState(false);
  return (
    <div>
      <button type="button" onClick={() => setAberto(true)}>Abrir</button>
      <BaseModal isOpen={aberto} onClose={() => setAberto(false)} title="Teste">
        <label htmlFor="campo">Campo</label>
        <input id="campo" />
        <button type="button">Salvar</button>
      </BaseModal>
    </div>
  );
}

describe('BaseModal', () => {
  it('move o foco para dentro e devolve ao fechar', () => {
    render(<Exemplo />);
    const abrir = screen.getByRole('button', { name: 'Abrir' });

    // jsdom não move o foco ao clicar (o navegador move): simula o estado real
    abrir.focus();
    fireEvent.click(abrir);
    // foco vai para o primeiro campo do diálogo
    expect(document.activeElement).toBe(screen.getByLabelText('Campo'));

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    // e volta para quem abriu
    expect(document.activeElement).toBe(abrir);
  });

  it('prende o Tab dentro do diálogo', () => {
    render(<Exemplo />);
    fireEvent.click(screen.getByRole('button', { name: 'Abrir' }));

    const dialog = screen.getByRole('dialog');
    const focaveis = Array.from(dialog.querySelectorAll<HTMLElement>('button, input'));
    const ultimo = focaveis[focaveis.length - 1];
    ultimo.focus();

    // Tab no último elemento volta para o primeiro, sem escapar para a página
    fireEvent.keyDown(document, { key: 'Tab' });
    expect(dialog.contains(document.activeElement)).toBe(true);
    expect(document.activeElement).toBe(focaveis[0]);
  });

  it('Shift+Tab no primeiro elemento vai para o último', () => {
    render(<Exemplo />);
    fireEvent.click(screen.getByRole('button', { name: 'Abrir' }));

    const dialog = screen.getByRole('dialog');
    const focaveis = Array.from(dialog.querySelectorAll<HTMLElement>('button, input'));
    focaveis[0].focus();

    fireEvent.keyDown(document, { key: 'Tab', shiftKey: true });
    expect(document.activeElement).toBe(focaveis[focaveis.length - 1]);
  });
});
