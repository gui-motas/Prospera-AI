import type { ReactNode } from 'react';

/** Renderizador mínimo para a resposta da IA: negrito, itálico, títulos e listas. Nunca usa innerHTML. */

function inline(texto: string): ReactNode[] {
  const partes = texto.split(/(\*\*[^*]+\*\*|\*[^*\s][^*]*\*)/g);
  return partes.map((p, i) => {
    if (p.startsWith('**') && p.endsWith('**') && p.length > 4) return <strong key={i}>{p.slice(2, -2)}</strong>;
    if (p.startsWith('*') && p.endsWith('*') && p.length > 2) return <em key={i}>{p.slice(1, -1)}</em>;
    return p;
  });
}

type Bloco =
  | { tipo: 'p'; texto: string }
  | { tipo: 'h'; texto: string }
  | { tipo: 'ul' | 'ol'; itens: string[] };

function blocos(markdown: string): Bloco[] {
  const saida: Bloco[] = [];
  for (const bruta of markdown.split('\n')) {
    const linha = bruta.trim();
    if (!linha || /^(-{3,}|\*{3,})$/.test(linha)) continue;
    const titulo = linha.match(/^#{1,6}\s+(.*)$/);
    const ponto = linha.match(/^[-*•]\s+(.*)$/);
    const numero = linha.match(/^\d+[.)]\s+(.*)$/);
    const ultimo = saida[saida.length - 1];
    if (titulo) saida.push({ tipo: 'h', texto: titulo[1] });
    else if (ponto || numero) {
      const tipo = ponto ? 'ul' : 'ol';
      const texto = (ponto ?? numero)![1];
      if (ultimo && ultimo.tipo === tipo) ultimo.itens.push(texto);
      else saida.push({ tipo, itens: [texto] });
    } else if (/^\*\*[^*]+\*\*:?$/.test(linha)) saida.push({ tipo: 'h', texto: linha.replace(/\*\*/g, '') });
    else saida.push({ tipo: 'p', texto: linha });
  }
  return saida;
}

export function TextoMarkdown({ texto }: { texto: string }) {
  return (
    <div className="analise-texto">
      {blocos(texto).map((b, i) => {
        if (b.tipo === 'h') return <h3 key={i} className="analise-titulo">{inline(b.texto)}</h3>;
        if (b.tipo === 'p') return <p key={i}>{inline(b.texto)}</p>;
        const Lista = b.tipo;
        return <Lista key={i}>{b.itens.map((t, j) => <li key={j}>{inline(t)}</li>)}</Lista>;
      })}
    </div>
  );
}
