import { useState } from 'react';
import { EstadoVazio, ErroCarregamento, PageHeader } from '../components/Comuns';
import { ConfirmModal } from '../components/ConfirmModal';
import { Icone } from '../components/Icone';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ContaModal } from '../components/modais/ContaModal';
import { useAcao } from '../hooks/useAcao';
import { useContas } from '../hooks/useDados';
import { mensagemDeErro } from '../services/api-client';
import { contaService } from '../services/servicos';
import type { ContaFinanceira } from '../services/tipos';
import { ICONE_TIPO_CONTA, ROTULO_TIPO_CONTA, formatarMoeda } from '../utils/formatacao';

/** RF-003 – Gerenciamento de contas financeiras. */
export function ContasPage() {
  const consulta = useContas();
  const contas = consulta.data ?? [];
  const [modal, setModal] = useState<{ aberto: boolean; conta: ContaFinanceira | null }>({ aberto: false, conta: null });
  const [excluindo, setExcluindo] = useState<ContaFinanceira | null>(null);

  const excluir = useAcao((c: ContaFinanceira) => contaService.excluir(c.id), 'Conta excluída.', () => setExcluindo(null));
  const saldoTotal = contas.reduce((s, c) => s + c.saldoAtual, 0);

  return (
    <div className="pagina fade-in">
      <PageHeader
        titulo="Contas"
        descricao={`Saldo total: ${formatarMoeda(saldoTotal)}`}
        acoes={
          <button type="button" className="btn btn-primary btn-sm" onClick={() => setModal({ aberto: true, conta: null })}>
            <Icone nome="add" /> Nova conta
          </button>
        }
      />

      {consulta.isLoading ? (
        <LoadingSpinner />
      ) : consulta.isError ? (
        <ErroCarregamento mensagem={mensagemDeErro(consulta.error)} onTentar={() => void consulta.refetch()} />
      ) : contas.length === 0 ? (
        <EstadoVazio icone="account_balance_wallet" titulo="Nenhuma conta cadastrada"
          texto="Cadastre onde seu dinheiro está: banco, carteira, poupança…" />
      ) : (
        <div className="grade-contas">
          {contas.map((conta, indice) => (
            <article key={conta.id} className={`account-card account-card-grande ${indice % 2 === 0 ? 'card-dark' : 'card-gradient'}`}>
              <span className="account-card-icone" aria-hidden="true">
                <Icone nome={ICONE_TIPO_CONTA[conta.tipo]} tamanho={64} />
              </span>
              <div>
                <h2 className="account-card-nome">{conta.nome}</h2>
                <p className="account-card-saldo">{formatarMoeda(conta.saldoAtual)}</p>
                <p className="account-card-detalhe">
                  Saldo inicial {formatarMoeda(conta.saldoInicial)} · {conta.quantidadeMovimentacoes} movimentação(ões)
                </p>
              </div>
              <div className="account-card-rodape">
                <span className="account-card-tipo">{ROTULO_TIPO_CONTA[conta.tipo]}</span>
                <span className="d-flex gap-2">
                  <button type="button" className="btn-icone btn-icone-claro" onClick={() => setModal({ aberto: true, conta })}
                    aria-label={`Editar conta ${conta.nome}`}>
                    <Icone nome="edit" />
                  </button>
                  <button type="button" className="btn-icone btn-icone-claro" onClick={() => setExcluindo(conta)}
                    aria-label={`Excluir conta ${conta.nome}`}>
                    <Icone nome="delete" />
                  </button>
                </span>
              </div>
            </article>
          ))}
        </div>
      )}

      <ContaModal aberto={modal.aberto} conta={modal.conta} onClose={() => setModal({ aberto: false, conta: null })} />
      <ConfirmModal
        isOpen={excluindo !== null}
        titulo="Excluir conta"
        textoConfirmar="Excluir conta"
        perigo
        carregando={excluir.isPending}
        onClose={() => setExcluindo(null)}
        onConfirmar={() => excluindo && excluir.mutate(excluindo)}
      >
        <p>Excluir a conta <strong>{excluindo?.nome}</strong>?</p>
        {excluindo && excluindo.quantidadeMovimentacoes > 0 && (
          <p className="aviso aviso-perigo">
            <Icone nome="warning" />
            <span>As {excluindo.quantidadeMovimentacoes} movimentação(ões) desta conta também serão excluídas. Esta ação não pode ser desfeita.</span>
          </p>
        )}
      </ConfirmModal>
    </div>
  );
}
