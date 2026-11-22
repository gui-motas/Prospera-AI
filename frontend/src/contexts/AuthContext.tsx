import { useCallback, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { authService, usuarioService } from '../services/servicos';
import type { Usuario } from '../services/tipos';
import { AuthContext } from './AuthProviderContext';

/** RF-002 – mantém o usuário autenticado (sessão do backend). */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Usuario | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const queryClient = useQueryClient();

  const checkAuth = useCallback(async () => {
    try {
      setUser(await usuarioService.obter());
    } catch {
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void checkAuth();
  }, [checkAuth]);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      setUser(null);
      // RN-001: nenhum dado do usuário anterior permanece em cache
      queryClient.clear();
    }
  }, [queryClient]);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isAdmin: user?.perfil === 'ADMINISTRADOR',
        isLoading,
        checkAuth,
        atualizarUsuario: setUser,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
