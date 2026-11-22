import type { SituacaoOrcamento, StatusMeta, TipoAnalise, TipoConta, TipoMovimentacao } from '../services/tipos';

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export function formatarMoeda(valor: number | null | undefined): string {
  return moeda.format(Number(valor ?? 0));
}

export function formatarPercentual(valor: number | null | undefined): string {
  return `${Number(valor ?? 0).toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`;
}

/** Converte "AAAA-MM-DD" em "DD/MM/AAAA" sem passar por Date (evita erro de fuso horário). */
export function formatarData(iso: string | null | undefined): string {
  if (!iso) return '—';
  const [ano, mes, dia] = iso.slice(0, 10).split('-');
  return dia && mes && ano ? `${dia}/${mes}/${ano}` : iso;
}

export function formatarDataHora(iso: string | null | undefined): string {
  if (!iso) return '—';
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) return iso;
  return data.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

/** "2026-11" → "nov/26" */
export function formatarMes(anoMes: string): string {
  const [ano, mes] = anoMes.split('-');
  const indice = Number(mes) - 1;
  return MESES[indice] ? `${MESES[indice]}/${ano.slice(2)}` : anoMes;
}

/** Data local de hoje no formato AAAA-MM-DD. */
export function hojeIso(): string {
  return paraIso(new Date());
}

export function paraIso(data: Date): string {
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}

/** Primeiro e último dia do mês da data informada. */
export function periodoDoMes(referencia: Date = new Date()): { inicio: string; fim: string } {
  const inicio = new Date(referencia.getFullYear(), referencia.getMonth(), 1);
  const fim = new Date(referencia.getFullYear(), referencia.getMonth() + 1, 0);
  return { inicio: paraIso(inicio), fim: paraIso(fim) };
}

export const ROTULO_TIPO_CONTA: Record<TipoConta, string> = {
  CARTEIRA: 'Carteira',
  CONTA_CORRENTE: 'Conta corrente',
  POUPANCA: 'Poupança',
  INVESTIMENTO: 'Investimento',
};

export const ICONE_TIPO_CONTA: Record<TipoConta, string> = {
  CARTEIRA: 'account_balance_wallet',
  CONTA_CORRENTE: 'account_balance',
  POUPANCA: 'savings',
  INVESTIMENTO: 'trending_up',
};

export const ROTULO_TIPO_MOVIMENTACAO: Record<TipoMovimentacao, string> = {
  RECEITA: 'Receita',
  DESPESA: 'Despesa',
};

export const ROTULO_STATUS_META: Record<StatusMeta, string> = {
  EM_ANDAMENTO: 'Em andamento',
  CONCLUIDA: 'Concluída',
  ATRASADA: 'Prazo encerrado',
};

export const ROTULO_SITUACAO_ORCAMENTO: Record<SituacaoOrcamento, string> = {
  DENTRO_DO_LIMITE: 'Dentro do limite',
  ALERTA: 'Perto do limite',
  EXCEDIDO: 'Limite ultrapassado',
};

export const ROTULO_TIPO_ANALISE: Record<TipoAnalise, string> = {
  SITUACAO_FINANCEIRA: 'Situação financeira',
  PLANEJAMENTO_META: 'Planejamento de meta',
  INTERPRETACAO_CENARIO: 'Interpretação de cenário',
};

/** Ícone sugerido a partir do nome da categoria (apenas visual). */
export function iconeCategoria(nome: string | undefined, tipo: TipoMovimentacao): string {
  const n = (nome ?? '').toLowerCase();
  if (tipo === 'RECEITA') return n.includes('salár') ? 'payments' : 'savings';
  if (n.includes('aliment') || n.includes('restaur') || n.includes('mercado')) return 'restaurant';
  if (n.includes('morad') || n.includes('aluguel')) return 'home';
  if (n.includes('transp') || n.includes('combust')) return 'directions_car';
  if (n.includes('saúde') || n.includes('saude') || n.includes('farm')) return 'favorite';
  if (n.includes('educa')) return 'school';
  if (n.includes('lazer') || n.includes('viag')) return 'beach_access';
  if (n.includes('vest') || n.includes('roupa')) return 'checkroom';
  if (n.includes('conta') || n.includes('servi')) return 'receipt_long';
  return 'shopping_bag';
}
