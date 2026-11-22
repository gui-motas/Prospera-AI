import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  categoriaService,
  contaService,
  metaService,
  movimentacaoService,
  orcamentoService,
  painelService,
  planejamentoService,
} from '../services/servicos';
import type { FiltrosMovimentacao } from '../services/tipos';

/** Consultas compartilhadas entre as telas (cache do React Query). */

export function useContas() {
  return useQuery({ queryKey: ['contas'], queryFn: contaService.listar });
}

export function useCategorias() {
  return useQuery({ queryKey: ['categorias'], queryFn: categoriaService.listar });
}

export function useMetas() {
  return useQuery({ queryKey: ['metas'], queryFn: metaService.listar });
}

export function useOrcamentos() {
  return useQuery({ queryKey: ['orcamentos'], queryFn: orcamentoService.listar });
}

export function usePainel(inicio: string, fim: string) {
  return useQuery({ queryKey: ['painel', inicio, fim], queryFn: () => painelService.resumo(inicio, fim) });
}

export function useMovimentacoes(filtros: FiltrosMovimentacao) {
  return useQuery({ queryKey: ['movimentacoes', filtros], queryFn: () => movimentacaoService.listar(filtros) });
}

export function useStatusIA() {
  return useQuery({ queryKey: ['planejamento', 'status'], queryFn: planejamentoService.status, staleTime: 60_000 });
}

/**
 * Após qualquer alteração, recarrega tudo o que depende de movimentações, contas e metas:
 * saldo, painel, orçamentos e diagnóstico são sempre recalculados pela plataforma (RN-004, RN-012).
 */
export function useAtualizarDados() {
  const queryClient = useQueryClient();
  return () => {
    for (const chave of ['contas', 'categorias', 'movimentacoes', 'painel', 'orcamentos', 'metas', 'planejamento']) {
      void queryClient.invalidateQueries({ queryKey: [chave] });
    }
  };
}
