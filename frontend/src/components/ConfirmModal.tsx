import type { ReactNode } from 'react';
import { BaseModal } from './BaseModal';

interface ConfirmModalProps {
  isOpen: boolean;
  titulo: string;
  children: ReactNode;
  textoConfirmar?: string;
  perigo?: boolean;
  carregando?: boolean;
  onConfirmar: () => void;
  onClose: () => void;
}

/** Confirmação explícita do usuário antes de ações que alteram ou apagam dados (RN-011). */
export function ConfirmModal({
  isOpen,
  titulo,
  children,
  textoConfirmar = 'Confirmar',
  perigo = false,
  carregando = false,
  onConfirmar,
  onClose,
}: ConfirmModalProps) {
  return (
    <BaseModal isOpen={isOpen} onClose={onClose} title={titulo} largura="sm">
      <div className="confirm-texto">{children}</div>
      <div className="modal-acoes">
        <button type="button" className="btn btn-secondary" onClick={onClose} disabled={carregando}>
          Cancelar
        </button>
        <button
          type="button"
          className={`btn ${perigo ? 'btn-danger' : 'btn-primary'}`}
          onClick={onConfirmar}
          disabled={carregando}
        >
          {carregando ? 'Aguarde…' : textoConfirmar}
        </button>
      </div>
    </BaseModal>
  );
}
