import { useId, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { AuthLayout } from '../components/AuthLayout';
import { useAuth } from '../contexts/useAuth';
import { mensagemDeErro } from '../services/api-client';
import { authService } from '../services/servicos';

/** RF-002 – Autenticação de usuário. */
export function LoginPage() {
  const id = useId();
  const navigate = useNavigate();
  const { checkAuth, isAuthenticated } = useAuth();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);

  if (isAuthenticated) return <Navigate to="/painel" replace />;

  const entrar = async (e: FormEvent) => {
    e.preventDefault();
    setErro('');
    setCarregando(true);
    try {
      await authService.login(email.trim(), senha);
      await checkAuth();
      toast.success('Bem-vindo(a)!');
      navigate('/painel');
    } catch (err) {
      setErro(mensagemDeErro(err, 'Não foi possível entrar.'));
    } finally {
      setCarregando(false);
    }
  };

  return (
    <AuthLayout title="Entrar" subtitle="Acesse para acompanhar suas finanças e metas">
      <form onSubmit={entrar} noValidate>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-email`}>E-mail</label>
          <input id={`${id}-email`} className="form-control" type="email" autoComplete="email" required
            value={email} onChange={(e) => setEmail(e.target.value)} placeholder="seu@email.com" />
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor={`${id}-senha`}>Senha</label>
          <input id={`${id}-senha`} className="form-control" type="password" autoComplete="current-password" required
            value={senha} onChange={(e) => setSenha(e.target.value)} />
        </div>

        {erro && <p className="form-erro text-center" role="alert">{erro}</p>}

        <button type="submit" className="btn btn-primary w-100" disabled={carregando || !email || !senha}>
          {carregando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>

      <p className="mt-4 text-center text-sm">
        <span className="text-muted">Ainda não tem conta? </span>
        <Link to="/cadastro" className="btn-link">Cadastre-se</Link>
      </p>
    </AuthLayout>
  );
}
