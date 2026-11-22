import { useQuery } from '@tanstack/react-query';
import { ErroCarregamento, PageHeader } from '../components/Comuns';
import { Icone } from '../components/Icone';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { useAuth } from '../contexts/useAuth';
import { useAcao } from '../hooks/useAcao';
import { mensagemDeErro } from '../services/api-client';
import { adminService } from '../services/servicos';
import type { UsuarioAdmin } from '../services/tipos';
import { formatarDataHora } from '../utils/formatacao';

/**
 * Perfil Administrador (stakeholder 1.3.2): gerenciamento de usuários e acompanhamento da operação.
 * O administrador não tem acesso aos dados financeiros dos usuários (RN-001).
 */
export function AdminPage() {
  const { user } = useAuth();
  const usuarios = useQuery({ queryKey: ['admin', 'usuarios'], queryFn: adminService.usuarios });
  const status = useQuery({ queryKey: ['admin', 'status'], queryFn: adminService.status, refetchInterval: 30_000 });

  const alterar = useAcao(
    (v: { usuario: UsuarioAdmin; situacao: { ativo?: boolean; bloqueado?: boolean } }) =>
      adminService.alterarSituacao(v.usuario.id, v.situacao),
    'Situação do usuário atualizada.',
    () => {
      void usuarios.refetch();
      void status.refetch();
    },
  );

  return (
    <div className="pagina fade-in">
      <PageHeader titulo="Administração" descricao="Gerenciamento de usuários e monitoramento da plataforma" />

      <section className="cartao" aria-labelledby="titulo-operacao">
        <h2 id="titulo-operacao" className="secao-titulo">Funcionamento da plataforma</h2>
        {status.isLoading ? <LoadingSpinner /> : status.data && (
          <dl className="indicadores">
            <div className="indicador"><dt>Usuários</dt><dd>{status.data.totalUsuarios}</dd></div>
            <div className="indicador"><dt>Ativos</dt><dd>{status.data.usuariosAtivos}</dd></div>
            <div className="indicador"><dt>Bloqueados</dt><dd>{status.data.usuariosBloqueados}</dd></div>
            <div className="indicador"><dt>Análises de IA geradas</dt><dd>{status.data.analisesGeradas}</dd></div>
            <div className="indicador">
              <dt>API de IA</dt>
              <dd className={status.data.iaDisponivel ? 'valor-positivo' : 'valor-negativo'}>
                {status.data.iaDisponivel ? 'Configurada' : 'Indisponível'}
              </dd>
            </div>
            <div className="indicador"><dt>Hora do servidor</dt><dd className="text-sm">{formatarDataHora(status.data.horaServidor)}</dd></div>
          </dl>
        )}
      </section>

      <section className="cartao cartao-tabela" aria-labelledby="titulo-usuarios">
        <h2 id="titulo-usuarios" className="secao-titulo">Usuários</h2>
        {usuarios.isLoading ? (
          <LoadingSpinner />
        ) : usuarios.isError ? (
          <ErroCarregamento mensagem={mensagemDeErro(usuarios.error)} onTentar={() => void usuarios.refetch()} />
        ) : (
          <table className="tabela tabela-responsiva">
            <caption className="sr-only">Usuários cadastrados</caption>
            <thead>
              <tr>
                <th scope="col">Nome</th>
                <th scope="col">E-mail</th>
                <th scope="col">Perfil</th>
                <th scope="col">Situação</th>
                <th scope="col">Cadastro</th>
                <th scope="col" className="col-acoes"><span className="sr-only">Ações</span></th>
              </tr>
            </thead>
            <tbody>
              {(usuarios.data ?? []).map((u) => {
                const situacao = u.bloqueado ? 'Bloqueado' : u.ativo ? 'Ativo' : 'Aguardando confirmação';
                const ehVoce = u.id === user?.id;
                return (
                  <tr key={u.id}>
                    <td data-rotulo="Nome" className="celula-destaque">{u.nome}{ehVoce && ' (você)'}</td>
                    <td data-rotulo="E-mail">{u.email}</td>
                    <td data-rotulo="Perfil">{u.perfil === 'ADMINISTRADOR' ? 'Administrador' : 'Usuário'}</td>
                    <td data-rotulo="Situação">
                      <span className={`badge ${u.bloqueado ? 'badge-perigo' : u.ativo ? 'badge-sucesso' : 'badge-alerta'}`}>{situacao}</span>
                    </td>
                    <td data-rotulo="Cadastro">{formatarDataHora(u.criadoEm)}</td>
                    <td className="col-acoes">
                      {!ehVoce && (
                        <span className="d-flex gap-2">
                          {!u.ativo && (
                            <button type="button" className="btn btn-secondary btn-sm" disabled={alterar.isPending}
                              onClick={() => alterar.mutate({ usuario: u, situacao: { ativo: true } })}>
                              Ativar
                            </button>
                          )}
                          <button type="button" className={`btn btn-sm ${u.bloqueado ? 'btn-secondary' : 'btn-danger'}`} disabled={alterar.isPending}
                            onClick={() => alterar.mutate({ usuario: u, situacao: { bloqueado: !u.bloqueado } })}>
                            <Icone nome={u.bloqueado ? 'lock_open' : 'block'} /> {u.bloqueado ? 'Desbloquear' : 'Bloquear'}
                          </button>
                        </span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
