import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import toast from 'react-hot-toast';
import { PageHeader } from '../components/Comuns';
import { PasswordStrengthMeter } from '../components/PasswordStrengthMeter';
import { ThemeToggle } from '../components/ThemeToggle';
import { useAuth } from '../contexts/useAuth';
import { mensagemDeErro } from '../services/api-client';
import { usuarioService } from '../services/servicos';
import { formatarDataHora } from '../utils/formatacao';
import { isPasswordValid } from '../utils/password';

/** Gerenciamento do perfil do usuário (escopo 1.2): nome e senha. */
export function PerfilPage() {
  const id = useId();
  const { user, atualizarUsuario, isAdmin } = useAuth();
  const [nome, setNome] = useState(user?.nome ?? '');
  const [salvandoNome, setSalvandoNome] = useState(false);

  const [senhaAtual, setSenhaAtual] = useState('');
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [erroSenha, setErroSenha] = useState('');
  const [salvandoSenha, setSalvandoSenha] = useState(false);

  const salvarNome = async (e: FormEvent) => {
    e.preventDefault();
    setSalvandoNome(true);
    try {
      atualizarUsuario(await usuarioService.atualizar(nome.trim()));
      toast.success('Perfil atualizado.');
    } catch (err) {
      toast.error(mensagemDeErro(err));
    } finally {
      setSalvandoNome(false);
    }
  };

  const salvarSenha = async (e: FormEvent) => {
    e.preventDefault();
    if (!isPasswordValid(novaSenha)) {
      setErroSenha('A nova senha deve ter pelo menos 8 caracteres, com letras e números.');
      return;
    }
    if (novaSenha !== confirmacao) {
      setErroSenha('As senhas não coincidem.');
      return;
    }
    setErroSenha('');
    setSalvandoSenha(true);
    try {
      await usuarioService.alterarSenha(senhaAtual, novaSenha);
      toast.success('Senha alterada.');
      setSenhaAtual('');
      setNovaSenha('');
      setConfirmacao('');
    } catch (err) {
      setErroSenha(mensagemDeErro(err));
    } finally {
      setSalvandoSenha(false);
    }
  };

  return (
    <div className="pagina pagina-estreita fade-in">
      <PageHeader titulo="Meu perfil" descricao={`${user?.email ?? ''} · ${isAdmin ? 'Administrador' : 'Usuário'}`} />

      <section className="cartao" aria-labelledby="titulo-dados">
        <h2 id="titulo-dados" className="secao-titulo">Dados pessoais</h2>
        <form onSubmit={salvarNome} className="form-grid">
          <div className="form-group">
            <label className="form-label" htmlFor={`${id}-nome`}>Nome</label>
            <input id={`${id}-nome`} className="form-control" value={nome} maxLength={120} required
              onChange={(e) => setNome(e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor={`${id}-email`}>E-mail (usado no login)</label>
            <input id={`${id}-email`} className="form-control" value={user?.email ?? ''} readOnly />
          </div>
          <p className="form-ajuda">Conta criada em {formatarDataHora(user?.criadoEm)}.</p>
          <button type="submit" className="btn btn-primary" disabled={salvandoNome || !nome.trim() || nome.trim() === user?.nome}>
            {salvandoNome ? 'Salvando…' : 'Salvar'}
          </button>
        </form>
      </section>

      <section className="cartao" aria-labelledby="titulo-senha">
        <h2 id="titulo-senha" className="secao-titulo">Alterar senha</h2>
        <form onSubmit={salvarSenha} className="form-grid" noValidate>
          <div className="form-group">
            <label className="form-label" htmlFor={`${id}-atual`}>Senha atual</label>
            <input id={`${id}-atual`} type="password" className="form-control" autoComplete="current-password"
              value={senhaAtual} onChange={(e) => setSenhaAtual(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor={`${id}-nova`}>Nova senha</label>
            <input id={`${id}-nova`} type="password" className="form-control" autoComplete="new-password"
              value={novaSenha} onChange={(e) => setNovaSenha(e.target.value)} required />
            <PasswordStrengthMeter password={novaSenha} />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor={`${id}-confirmacao`}>Confirmar nova senha</label>
            <input id={`${id}-confirmacao`} type="password" className="form-control" autoComplete="new-password"
              value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)} required />
          </div>
          {erroSenha && <p className="form-erro" role="alert">{erroSenha}</p>}
          <button type="submit" className="btn btn-primary" disabled={salvandoSenha || !senhaAtual || !novaSenha}>
            {salvandoSenha ? 'Salvando…' : 'Alterar senha'}
          </button>
        </form>
      </section>

      <section className="cartao" aria-labelledby="titulo-preferencias">
        <h2 id="titulo-preferencias" className="secao-titulo">Aparência</h2>
        <div className="d-flex align-center gap-3">
          <ThemeToggle className="btn btn-secondary btn-sm" />
          <span className="text-sm text-muted">Alternar entre tema claro e escuro</span>
        </div>
      </section>

      <section className="cartao" aria-labelledby="titulo-privacidade">
        <h2 id="titulo-privacidade" className="secao-titulo">Privacidade (LGPD)</h2>
        <p className="text-sm text-muted">
          Guardamos apenas o necessário para a plataforma funcionar: nome, e-mail, senha protegida por hash e os dados
          financeiros que você registra. Somente você acessa seus dados financeiros. No planejamento inteligente, a API de
          IA recebe apenas valores consolidados — nunca seu nome, e-mail, senha ou a descrição das suas movimentações.
        </p>
      </section>
    </div>
  );
}
