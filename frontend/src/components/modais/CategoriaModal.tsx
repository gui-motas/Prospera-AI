import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import { useAcao } from '../../hooks/useAcao';
import { categoriaService } from '../../services/servicos';
import type { Categoria, CategoriaRequest, TipoMovimentacao } from '../../services/tipos';
import { BaseModal } from '../BaseModal';
import { SeletorTipo } from './SeletorTipo';

interface CategoriaModalProps {
  aberto: boolean;
  onClose: () => void;
  categoria?: Categoria | null;
  tipoInicial?: TipoMovimentacao;
}

/** RF-004 – criar e editar categorias, com classificação receita/despesa (RN-003). */
export function CategoriaModal({ aberto, onClose, categoria, tipoInicial = 'DESPESA' }: CategoriaModalProps) {
  return (
    <BaseModal isOpen={aberto} onClose={onClose} title={categoria ? 'Editar categoria' : 'Nova categoria'} largura="sm">
      {aberto && <Formulario categoria={categoria ?? null} tipoInicial={tipoInicial} onClose={onClose} />}
    </BaseModal>
  );
}

function Formulario({ categoria, tipoInicial, onClose }: {
  categoria: Categoria | null;
  tipoInicial: TipoMovimentacao;
  onClose: () => void;
}) {
  const id = useId();
  const [nome, setNome] = useState(categoria?.nome ?? '');
  const [tipo, setTipo] = useState<TipoMovimentacao>(categoria?.tipo ?? tipoInicial);

  const salvar = useAcao(
    (dados: CategoriaRequest) => (categoria ? categoriaService.editar(categoria.id, dados) : categoriaService.criar(dados)),
    categoria ? 'Categoria atualizada.' : 'Categoria criada.',
    onClose,
  );

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    salvar.mutate({ nome: nome.trim(), tipo });
  };

  return (
    <form onSubmit={enviar} className="form-grid">
      <div className="form-group">
        <label className="form-label" htmlFor={`${id}-nome`}>Nome</label>
        <input id={`${id}-nome`} className="form-control" value={nome} onChange={(e) => setNome(e.target.value)}
          placeholder="Ex.: Alimentação" maxLength={60} required autoFocus />
      </div>
      <SeletorTipo valor={tipo} onChange={setTipo} legenda="Classificação" />
      <p className="form-ajuda">
        A categoria só poderá ser usada em movimentações do mesmo tipo.
      </p>
      <button type="submit" className="btn btn-primary w-100" disabled={salvar.isPending}>
        {salvar.isPending ? 'Salvando…' : 'Salvar categoria'}
      </button>
    </form>
  );
}
