import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { GastoCategoria, ResumoMensal } from '../../services/tipos';
import { formatarMes, formatarMoeda, formatarPercentual } from '../../utils/formatacao';

const CORES_CATEGORIAS = ['#137fec', '#a855f7', '#f59e0b', '#10b981', '#ef4444', '#06b6d4', '#ec4899', '#64748b'];

/** Distribuição dos gastos por categoria (RF-007), com legenda em texto (RNF-007). */
export function GraficoCategorias({ gastos }: { gastos: GastoCategoria[] }) {
  if (gastos.length === 0) {
    return <p className="text-muted text-sm">Sem despesas no período.</p>;
  }
  const dados = gastos.map((g) => ({ nome: g.categoria, valor: Number(g.total), percentual: Number(g.percentual) }));

  return (
    <div className="grafico-categorias">
      <div className="grafico-area grafico-rosca" aria-hidden="true">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie data={dados} dataKey="valor" nameKey="nome" innerRadius="58%" outerRadius="90%" paddingAngle={2} stroke="none">
              {dados.map((d, i) => <Cell key={d.nome} fill={CORES_CATEGORIAS[i % CORES_CATEGORIAS.length]} />)}
            </Pie>
            <Tooltip formatter={(valor) => formatarMoeda(Number(valor))} />
          </PieChart>
        </ResponsiveContainer>
      </div>
      <ul className="legenda-categorias">
        {dados.map((d, i) => (
          <li key={d.nome}>
            <span className="legenda-cor" style={{ background: CORES_CATEGORIAS[i % CORES_CATEGORIAS.length] }} aria-hidden="true" />
            <span className="legenda-nome">{d.nome}</span>
            <span className="legenda-valor">{formatarMoeda(d.valor)}</span>
            <span className="legenda-percentual">{formatarPercentual(d.percentual)}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

/** Evolução financeira mensal (receitas × despesas), com tabela equivalente para leitores de tela. */
export function GraficoEvolucao({ meses }: { meses: ResumoMensal[] }) {
  const dados = meses.map((m) => ({ mes: formatarMes(m.mes), Receitas: Number(m.receitas), Despesas: Number(m.despesas) }));

  return (
    <>
      <div className="grafico-area" aria-hidden="true">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={dados} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--borda)" />
            <XAxis dataKey="mes" tickLine={false} axisLine={false} tick={{ fill: 'var(--texto-fraco)', fontSize: 12 }} />
            <YAxis tickLine={false} axisLine={false} width={64} tick={{ fill: 'var(--texto-fraco)', fontSize: 12 }}
              tickFormatter={(v) => Number(v).toLocaleString('pt-BR', { notation: 'compact' })} />
            <Tooltip formatter={(valor) => formatarMoeda(Number(valor))} cursor={{ fill: 'var(--primaria-suave)' }} />
            <Legend />
            <Bar dataKey="Receitas" fill="#10b981" radius={[6, 6, 0, 0]} />
            <Bar dataKey="Despesas" fill="#137fec" radius={[6, 6, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
      <table className="sr-only">
        <caption>Receitas e despesas por mês</caption>
        <thead><tr><th scope="col">Mês</th><th scope="col">Receitas</th><th scope="col">Despesas</th></tr></thead>
        <tbody>
          {meses.map((m) => (
            <tr key={m.mes}><th scope="row">{formatarMes(m.mes)}</th><td>{formatarMoeda(m.receitas)}</td><td>{formatarMoeda(m.despesas)}</td></tr>
          ))}
        </tbody>
      </table>
    </>
  );
}
