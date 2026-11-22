import { useEffect, useId } from 'react';
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

/** Modal acessível: role="dialog", título associado e fechamento com Esc (RNF-007). */
export function BaseModal({ isOpen, onClose, title, children, largura = 'md' }: BaseModalProps) {
  const tituloId = useId();

  useEffect(() => {
    if (!isOpen) return;
    const aoTeclar = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', aoTeclar);
    return () => document.removeEventListener('keydown', aoTeclar);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const aoClicarFundo = (e: MouseEvent) => {
    if (e.target === e.currentTarget) onClose();
  };

  return createPortal(
    <div className="modal-overlay" onClick={aoClicarFundo}>
      <div className={`modal-content modal-${largura}`} role="dialog" aria-modal="true" aria-labelledby={tituloId}>
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
