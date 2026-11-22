import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { mensagemDeErro } from '../services/api-client';
import { useAtualizarDados } from './useDados';

/**
 * Executa uma ação que altera dados, mostra o resultado ao usuário e recarrega
 * as informações calculadas pela plataforma.
 */
export function useAcao<TArgs, TRes>(
  acao: (args: TArgs) => Promise<TRes>,
  mensagemSucesso: string,
  aoConcluir?: (resultado: TRes) => void,
) {
  const atualizar = useAtualizarDados();
  return useMutation({
    mutationFn: acao,
    onSuccess: (resultado: TRes) => {
      toast.success(mensagemSucesso);
      atualizar();
      aoConcluir?.(resultado);
    },
    onError: (erro: unknown) => {
      toast.error(mensagemDeErro(erro));
    },
  });
}
