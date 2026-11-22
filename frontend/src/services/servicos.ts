import { api } from './api-client';
import type {
  AnaliseFinanceira,
  AporteRequest,
  Categoria,
  CategoriaRequest,
  CenarioFinanceiro,
  CenarioRequest,
  ContaFinanceira,
  ContaRequest,
  Diagnostico,
  FiltrosMovimentacao,
  MetaFinanceira,
  MetaRequest,
  Movimentacao,
  MovimentacaoRequest,
  Orcamento,
  OrcamentoRequest,
  ResumoFinanceiro,
  StatusIA,
  StatusOperacao,
  Usuario,
  UsuarioAdmin,
} from './tipos';

/** Remove filtros vazios para não enviar parâmetros em branco. */
function semVazios<T extends object>(obj: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(obj).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

// ---------- Autenticação e cadastro (RF-001, RF-002) ----------
export const authService = {
  login: (email: string, senha: string) => {
    const corpo = new URLSearchParams();
    corpo.append('username', email);
    corpo.append('password', senha);
    return api.post('/api/v1/login', corpo, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    });
  },
  logout: () => api.post('/api/v1/logout'),
  cadastrar: async (dados: { nome: string; email: string; senha: string }) =>
    (await api.post<{ mensagem: string; confirmacaoPendente: boolean }>('/api/v1/cadastro', dados)).data,
  confirmarEmail: async (token: string) =>
    (await api.get<{ mensagem: string }>('/api/v1/cadastro/confirmar', { params: { token } })).data,
};

// ---------- Perfil ----------
export const usuarioService = {
  obter: async () => (await api.get<Usuario>('/api/v1/usuarios/me')).data,
  atualizar: async (nome: string) => (await api.put<Usuario>('/api/v1/usuarios/me', { nome })).data,
  alterarSenha: (senhaAtual: string, novaSenha: string) =>
    api.put('/api/v1/usuarios/me/senha', { senhaAtual, novaSenha }),
};

// ---------- Painel (RF-007) ----------
export const painelService = {
  resumo: async (inicio?: string, fim?: string) =>
    (await api.get<ResumoFinanceiro>('/api/v1/painel', { params: semVazios({ inicio, fim }) })).data,
};

// ---------- Contas (RF-003) ----------
export const contaService = {
  listar: async () => (await api.get<ContaFinanceira[]>('/api/v1/contas')).data,
  criar: async (d: ContaRequest) => (await api.post<ContaFinanceira>('/api/v1/contas', d)).data,
  editar: async (id: string, d: ContaRequest) => (await api.put<ContaFinanceira>(`/api/v1/contas/${id}`, d)).data,
  excluir: (id: string) => api.delete(`/api/v1/contas/${id}`),
};

// ---------- Categorias (RF-004) ----------
export const categoriaService = {
  listar: async () => (await api.get<Categoria[]>('/api/v1/categorias')).data,
  criar: async (d: CategoriaRequest) => (await api.post<Categoria>('/api/v1/categorias', d)).data,
  editar: async (id: string, d: CategoriaRequest) => (await api.put<Categoria>(`/api/v1/categorias/${id}`, d)).data,
  excluir: (id: string) => api.delete(`/api/v1/categorias/${id}`),
};

// ---------- Movimentações (RF-005, RF-006) ----------
export const movimentacaoService = {
  listar: async (filtros: FiltrosMovimentacao) =>
    (await api.get<Movimentacao[]>('/api/v1/movimentacoes', { params: semVazios(filtros) })).data,
  registrar: async (d: MovimentacaoRequest) => (await api.post<Movimentacao>('/api/v1/movimentacoes', d)).data,
  editar: async (id: string, d: MovimentacaoRequest) =>
    (await api.put<Movimentacao>(`/api/v1/movimentacoes/${id}`, d)).data,
  excluir: (id: string) => api.delete(`/api/v1/movimentacoes/${id}`),
};

// ---------- Orçamentos (RN-005) ----------
export const orcamentoService = {
  listar: async () => (await api.get<Orcamento[]>('/api/v1/orcamentos')).data,
  criar: async (d: OrcamentoRequest) => (await api.post<Orcamento>('/api/v1/orcamentos', d)).data,
  editar: async (id: string, d: OrcamentoRequest) => (await api.put<Orcamento>(`/api/v1/orcamentos/${id}`, d)).data,
  excluir: (id: string) => api.delete(`/api/v1/orcamentos/${id}`),
};

// ---------- Metas (RF-08, RF-09) ----------
export const metaService = {
  listar: async () => (await api.get<MetaFinanceira[]>('/api/v1/metas')).data,
  criar: async (d: MetaRequest) => (await api.post<MetaFinanceira>('/api/v1/metas', d)).data,
  editar: async (id: string, d: MetaRequest) => (await api.put<MetaFinanceira>(`/api/v1/metas/${id}`, d)).data,
  registrarAporte: async (id: string, d: AporteRequest) =>
    (await api.post<MetaFinanceira>(`/api/v1/metas/${id}/aportes`, d)).data,
  excluir: (id: string) => api.delete(`/api/v1/metas/${id}`),
};

// ---------- Planejamento inteligente (RF-010) ----------
export const planejamentoService = {
  status: async () => (await api.get<StatusIA>('/api/v1/planejamento/status')).data,
  diagnostico: async () => (await api.get<Diagnostico>('/api/v1/planejamento/diagnostico')).data,
  analisarSituacao: async () =>
    (await api.post<AnaliseFinanceira>('/api/v1/planejamento/analises/situacao')).data,
  analisarMeta: async (metaId: string) =>
    (await api.post<AnaliseFinanceira>(`/api/v1/planejamento/metas/${metaId}/analises`)).data,
  historico: async () => (await api.get<AnaliseFinanceira[]>('/api/v1/planejamento/analises')).data,
  listarCenarios: async (metaId: string) =>
    (await api.get<CenarioFinanceiro[]>(`/api/v1/planejamento/metas/${metaId}/cenarios`)).data,
  simularCenario: async (metaId: string, d: CenarioRequest) =>
    (await api.post<CenarioFinanceiro>(`/api/v1/planejamento/metas/${metaId}/cenarios`, d)).data,
  interpretarCenario: async (cenarioId: string) =>
    (await api.post<AnaliseFinanceira>(`/api/v1/planejamento/cenarios/${cenarioId}/interpretacoes`)).data,
  excluirCenario: (cenarioId: string) => api.delete(`/api/v1/planejamento/cenarios/${cenarioId}`),
};

// ---------- Administração ----------
export const adminService = {
  usuarios: async () => (await api.get<UsuarioAdmin[]>('/api/v1/admin/usuarios')).data,
  alterarSituacao: async (id: string, situacao: { ativo?: boolean; bloqueado?: boolean }) =>
    (await api.patch<UsuarioAdmin>(`/api/v1/admin/usuarios/${id}/situacao`, situacao)).data,
  status: async () => (await api.get<StatusOperacao>('/api/v1/admin/status')).data,
};
