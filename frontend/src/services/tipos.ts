/**
 * Tipos trocados com a API. Os nomes seguem o diagrama de classes da ERS.
 * Datas no formato ISO (AAAA-MM-DD); valores monetários em número.
 */

export type PerfilUsuario = 'USUARIO' | 'ADMINISTRADOR';

export interface Usuario {
  id: string;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  criadoEm: string;
}

// ---------- Contas (RF-003) ----------
export type TipoConta = 'CARTEIRA' | 'CONTA_CORRENTE' | 'POUPANCA' | 'INVESTIMENTO';

export interface ContaFinanceira {
  id: string;
  nome: string;
  tipo: TipoConta;
  saldoInicial: number;
  saldoAtual: number;
  quantidadeMovimentacoes: number;
}

export interface ContaRequest {
  nome: string;
  tipo: TipoConta;
  saldoInicial: number;
}

// ---------- Categorias (RF-004) ----------
export type TipoMovimentacao = 'RECEITA' | 'DESPESA';

export interface Categoria {
  id: string;
  nome: string;
  tipo: TipoMovimentacao;
}

export interface CategoriaRequest {
  nome: string;
  tipo: TipoMovimentacao;
}

// ---------- Movimentações (RF-005, RF-006) ----------
export interface Movimentacao {
  id: string;
  tipo: TipoMovimentacao;
  descricao: string;
  valor: number;
  data: string;
  contaId: string;
  contaNome: string;
  categoriaId: string;
  categoriaNome: string;
}

export interface MovimentacaoRequest {
  tipo: TipoMovimentacao;
  descricao: string;
  valor: number;
  data: string;
  contaId: string;
  categoriaId: string;
}

export interface FiltrosMovimentacao {
  dataInicio?: string;
  dataFim?: string;
  categoriaId?: string;
  contaId?: string;
  tipo?: TipoMovimentacao;
  texto?: string;
}

// ---------- Orçamentos (RN-005) ----------
export type SituacaoOrcamento = 'DENTRO_DO_LIMITE' | 'ALERTA' | 'EXCEDIDO';

export interface Orcamento {
  id: string;
  categoriaId: string;
  categoriaNome: string;
  limite: number;
  inicioPeriodo: string;
  fimPeriodo: string;
  totalGasto: number;
  saldoRestante: number;
  percentualUtilizado: number;
  situacao: SituacaoOrcamento;
}

export interface OrcamentoRequest {
  categoriaId: string;
  limite: number;
  inicioPeriodo: string;
  fimPeriodo: string;
}

// ---------- Metas (RF-08, RF-09) ----------
export type StatusMeta = 'EM_ANDAMENTO' | 'CONCLUIDA' | 'ATRASADA';

export interface MetaFinanceira {
  id: string;
  nome: string;
  valorDesejado: number;
  valorAcumulado: number;
  valorRestante: number;
  progresso: number;
  progressoEsperado: number;
  noRitmo: boolean;
  prazo: string;
  mesesRestantes: number;
  valorMensalNecessario: number;
  status: StatusMeta;
  criadaEm: string;
}

export interface MetaRequest {
  nome: string;
  valorDesejado: number;
  prazo: string;
}

export type OperacaoAporte = 'APORTE' | 'RESGATE';

export interface AporteRequest {
  operacao: OperacaoAporte;
  valor: number;
}

// ---------- Painel (RF-007) ----------
export interface GastoCategoria {
  categoriaId: string;
  categoria: string;
  total: number;
  percentual: number;
}

export interface ResumoMensal {
  mes: string;
  receitas: number;
  despesas: number;
  resultado: number;
}

export interface ResumoFinanceiro {
  inicio: string;
  fim: string;
  saldoTotal: number;
  totalReceitas: number;
  totalDespesas: number;
  resultadoPeriodo: number;
  capacidadeMensalEconomia: number;
  gastosPorCategoria: GastoCategoria[];
  evolucaoMensal: ResumoMensal[];
}

// ---------- Planejamento inteligente (RF-010) ----------
export interface StatusIA {
  iaDisponivel: boolean;
  aviso: string;
}

export interface ViabilidadeMeta {
  metaId: string;
  nome: string;
  status: StatusMeta;
  prazo: string;
  valorRestante: number;
  valorMensalNecessario: number;
  mesesRestantes: number;
  viavelComCapacidadeAtual: boolean;
  mesesComCapacidadeAtual: number | null;
  prazoProjetadoComCapacidadeAtual: string | null;
}

export interface Diagnostico {
  dataReferencia: string;
  saldoTotal: number;
  capacidadeMensalEconomia: number;
  mesesConsiderados: number;
  metas: ViabilidadeMeta[];
}

export interface CenarioRequest {
  descricao: string;
  economiaMensal: number;
  categoriaReducaoId?: string;
  percentualReducao?: number;
}

export interface ComparacaoCenario {
  prazoMeta: string;
  valorRestante: number;
  valorMensalNecessario: number;
  economiaMensalAtual: number;
  mesesSituacaoAtual: number | null;
  prazoProjetadoAtual: string | null;
  situacaoAtualCumprePrazo: boolean;
  economiaMensalSimulada: number;
  mesesCenario: number;
  prazoProjetadoCenario: string;
  cenarioCumprePrazo: boolean;
  mesesAntecipados: number | null;
  economiaAdicional: number;
}

export interface CenarioFinanceiro {
  id: string;
  metaId: string;
  metaNome: string;
  descricao: string;
  economiaMensalSimulada: number;
  reducaoMensalAplicada: number | null;
  prazoProjetado: string;
  criadoEm: string;
  comparacao: ComparacaoCenario;
}

export type TipoAnalise = 'SITUACAO_FINANCEIRA' | 'PLANEJAMENTO_META' | 'INTERPRETACAO_CENARIO';

export interface AnaliseFinanceira {
  id: string;
  tipo: TipoAnalise;
  dataGeracao: string;
  resultado: string;
  metaId: string | null;
  metaNome: string | null;
  cenarioId: string | null;
  aviso: string;
  dadosEnviados: Record<string, unknown> | null;
}

// ---------- Administração ----------
export interface UsuarioAdmin {
  id: string;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  ativo: boolean;
  bloqueado: boolean;
  criadoEm: string;
}

export interface StatusOperacao {
  totalUsuarios: number;
  usuariosAtivos: number;
  usuariosBloqueados: number;
  analisesGeradas: number;
  iaDisponivel: boolean;
  horaServidor: string;
}
