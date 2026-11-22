import { useEffect, useId, useLayoutEffect, useRef } from 'react';
import type { MouseEvent, ReactNode } from 'react';
import { createPortal } from 'react-dom';
import { Icone } from './Icone';

interface BaseModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  largura?: 'sm' | 'md' | 'lg';
}

const FOCAVEIS = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

/**
 * Modal acessível: role="dialog", título associado, Esc para fechar, foco preso dentro
 * do diálogo e devolvido ao elemento que o abriu (RNF-007).
 */
export function BaseModal({ isOpen, onClose, title, children, largura = 'md' }: BaseModalProps) {
  const tituloId = useId();
  const caixaRef = useRef<HTMLDivElement>(null);
  const origemRef = useRef<HTMLElement | null>(null);

  // useLayoutEffect (e não useEffect) para ler quem tinha o foco antes de qualquer pintura;
  // o cleanup devolve o foco ao gatilho quando o modal fecha.
  useLayoutEffect(() => {
    if (!isOpen) return;
    origemRef.current = document.activeElement instanceof HTMLElement ? document.activeElement : null;

    return () => {
      origemRef.current?.focus();
      origemRef.current = null;
    };
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) return;

    const aoTeclar = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
        return;
      }
      if (e.key !== 'Tab' || !caixaRef.current) return;

      // Tab e Shift+Tab circulam apenas entre os focáveis do diálogo
      const focaveis = Array.from(caixaRef.current.querySelectorAll<HTMLElement>(FOCAVEIS));
      if (focaveis.length === 0) return;
      const primeiro = focaveis[0];
      const ultimo = focaveis[focaveis.length - 1];
      const ativo = document.activeElement;

      if (e.shiftKey && (ativo === primeiro || !caixaRef.current.contains(ativo))) {
        e.preventDefault();
        ultimo.focus();
      } else if (!e.shiftKey && (ativo === ultimo || !caixaRef.current.contains(ativo))) {
        e.preventDefault();
        primeiro.focus();
      }
    };

    document.addEventListener('keydown', aoTeclar);

    // foco inicial: o primeiro campo do formulário (o botão "Fechar" vem antes no DOM,
    // então preferir campo evita abrir o modal com o foco no "X")
    const campo = caixaRef.current?.querySelector<HTMLElement>(
      'input:not([disabled]), select:not([disabled]), textarea:not([disabled])');
    (campo ?? caixaRef.current)?.focus();

    return () => {
      document.removeEventListener('keydown', aoTeclar);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const aoClicarFundo = (e: MouseEvent) => {
    if (e.target === e.currentTarget) onClose();
  };

  return createPortal(
    <div className="modal-overlay" onClick={aoClicarFundo}>
      <div
        ref={caixaRef}
        className={`modal-content modal-${largura}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={tituloId}
        tabIndex={-1}
      >
        <div className="modal-header">
          <h2 id={tituloId} className="modal-title">{title}</h2>
          <button type="button" onClick={onClose} className="btn-close-modal" aria-label="Fechar">
            <Icone nome="close" />
          </button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>,
    document.body,
  );
}
