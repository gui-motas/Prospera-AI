import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { AuthLayout } from '../components/AuthLayout';
import { Icone } from '../components/Icone';
import { PasswordStrengthMeter } from '../components/PasswordStrengthMeter';
import { mensagemDeErro } from '../services/api-client';
import { authService } from '../services/servicos';
import { isPasswordValid } from '../utils/password';

/** RF-001 – Cadastro de usuário (somente os dados necessários: nome, e-mail e senha). */
export function CadastroPage() {
  const id = useId();
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);
  const [resultado, setResultado] = useState<{ mensagem: string; confirmacaoPendente: boolean } | null>(null);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    setErro('');
    if (!isPasswordValid(senha)) {
      setErro('A senha deve ter pelo menos 8 caracteres, com letras e números.');
      return;
    }
    if (senha !== confirmacao) {
      setErro('As senhas não coincidem.');
      return;
    }
    setCarregando(true);
    try {
      setResultado(await authService.cadastrar({ nome: nome.trim(), email: email.trim(), senha }));
    } catch (err) {
      setErro(mensagemDeErro(err, 'Não foi possível concluir o cadastro.'));
    } finally {
      setCarregando(false);
    }
  };

  if (resultado) {
    return (
      <AuthLayout title="Cadastro concluído" subtitle={resultado.mensagem}>
        <div className="register-success-box">
          <Icone nome={resultado.confirmacaoPendente ? 'mark_email_unread' : 'check_circle'} tamanho={40} />
          <p className="register-success-text">
            {resultado.confirmacaoPendente
              ? <>Abra o link enviado para <strong>{email}</strong> para ativar sua conta.</>
              : <>Sua conta está ativa. Já criamos uma conta “Carteira” e categorias iniciais para você começar.</>}
          </p>
        </div>
        <Link to="/login" className="btn btn-primary w-100">Ir para o login</Link>
      </AuthLayout>
    );
  }

  const senhasDiferentes = confirmacao.length > 0 && senha !== confirmacao;

  return (
    <AuthLayout title="Criar conta" subtitle="Organize suas finanças e planeje suas metas">
      <form onSubmit={enviar} noValidate>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-nome`}>Nome</label>
          <input id={`${id}-nome`} className="form-control" autoComplete="name" required maxLength={120}
            value={nome} onChange={(e) => setNome(e.target.value)} placeholder="Seu nome" />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-email`}>E-mail</label>
          <input id={`${id}-email`} className="form-control" type="email" autoComplete="email" required
            value={email} onChange={(e) => setEmail(e.target.value)} placeholder="seu@email.com" />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-senha`}>Senha</label>
          <input id={`${id}-senha`} className="form-control" type="password" autoComplete="new-password" required minLength={8}
            value={senha} onChange={(e) => setSenha(e.target.value)} aria-describedby={`${id}-regra`} />
          <p id={`${id}-regra`} className="form-ajuda">Mínimo de 8 caracteres, com letras e números.</p>
          <PasswordStrengthMeter password={senha} />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-confirmacao`}>Confirmar senha</label>
          <input id={`${id}-confirmacao`} className={`form-control ${senhasDiferentes ? 'input-invalido' : ''}`} type="password"
            autoComplete="new-password" required value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)}
            aria-invalid={senhasDiferentes} />
          {senhasDiferentes && <p className="form-erro">As senhas não coincidem.</p>}
        </div>

        {erro && <p className="form-erro text-center" role="alert">{erro}</p>}

        <button type="submit" className="btn btn-primary w-100" disabled={carregando}>
          {carregando ? 'Criando conta…' : 'Cadastrar'}
        </button>
      </form>

      <p className="mt-4 text-center text-sm">
        <span className="text-muted">Já tem conta? </span>
        <Link to="/login" className="btn-link">Entrar</Link>
      </p>
    </AuthLayout>
  );
}
