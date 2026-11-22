import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '../components/AuthLayout';
import { Icone } from '../components/Icone';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { mensagemDeErro } from '../services/api-client';
import { authService } from '../services/servicos';

/** Confirmação de e-mail do cadastro (quando habilitada no backend). */
export function ConfirmarContaPage() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const [estado, setEstado] = useState<{ ok: boolean; mensagem: string } | null>(
    token ? null : { ok: false, mensagem: 'Link de confirmação inválido.' },
  );

  useEffect(() => {
    if (!token) return;
    authService
      .confirmarEmail(token)
      .then((r) => setEstado({ ok: true, mensagem: r.mensagem }))
      .catch((err) => setEstado({ ok: false, mensagem: mensagemDeErro(err, 'Link inválido ou expirado.') }));
  }, [token]);

  return (
    <AuthLayout title="Confirmação de e-mail" subtitle={estado ? '' : 'Validando seu link…'}>
      {!estado ? (
        <LoadingSpinner />
      ) : (
        <div className="text-center">
          <span className={`confirmacao-icone ${estado.ok ? 'ok' : 'erro'}`}>
            <Icone nome={estado.ok ? 'check' : 'close'} tamanho={32} />
          </span>
          <p className="mb-4" role="status">{estado.mensagem}</p>
          <Link to="/login" className="btn btn-primary w-100">Ir para o login</Link>
        </div>
      )}
    </AuthLayout>
  );
}
