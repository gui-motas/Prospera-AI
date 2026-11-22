import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import { LoadingSpinner } from './LoadingSpinner';

interface ProtectedRouteProps {
  children: ReactNode;
  /** Restringe a rota ao perfil Administrador. */
  somenteAdmin?: boolean;
}

/** RNF-004 / RN-008: páginas internas exigem autenticação. */
export function ProtectedRoute({ children, somenteAdmin = false }: ProtectedRouteProps) {
  const { isAuthenticated, isAdmin, isLoading } = useAuth();

  if (isLoading) return <LoadingSpinner telaCheia />;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (somenteAdmin && !isAdmin) return <Navigate to="/painel" replace />;

  return <>{children}</>;
}
