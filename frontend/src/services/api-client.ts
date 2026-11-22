import axios from 'axios';
import toast from 'react-hot-toast';

/**
 * Cliente HTTP. Por padrão usa a mesma origem do frontend: em desenvolvimento o Vite
 * encaminha /api ao backend e, no Docker, o Nginx faz o mesmo. Assim o cookie de sessão
 * funciona sem configuração extra de CORS.
 */
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

/** Extrai a mensagem de erro enviada pelo backend no formato {"erro": "..."}. */
export function mensagemDeErro(erro: unknown, padrao = 'Não foi possível concluir a operação.'): string {
  if (axios.isAxiosError(erro)) {
    const dados = erro.response?.data as { erro?: string } | undefined;
    if (dados && typeof dados.erro === 'string') return dados.erro;
    if (!erro.response) return 'Não foi possível conectar ao servidor.';
  }
  if (erro instanceof Error && erro.message) return erro.message;
  return padrao;
}

/** RNF-09: a indisponibilidade da IA é tratada na própria tela, sem bloquear o restante. */
export function iaIndisponivel(erro: unknown): boolean {
  return axios.isAxiosError(erro) && erro.response?.status === 503;
}

api.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    if (axios.isAxiosError(erro)) {
      const status = erro.response?.status;
      const url = erro.config?.url ?? '';
      const rotaPublica = ['/login', '/cadastro', '/confirmar-conta'].some((r) => window.location.pathname.startsWith(r));

      if (status === 401 && !url.includes('/usuarios/me') && !url.includes('/login') && !rotaPublica) {
        toast.error('Sua sessão expirou. Faça login novamente.', { id: 'sessao' });
        window.location.assign('/login');
      } else if (status === 403) {
        toast.error(mensagemDeErro(erro, 'Você não tem permissão para esta ação.'), { id: 'permissao' });
      } else if (status !== undefined && status >= 500 && status !== 503) {
        toast.error('Erro no servidor. Tente novamente em instantes.', { id: 'servidor' });
      }
    }
    return Promise.reject(erro);
  },
);
