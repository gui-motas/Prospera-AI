import { createContext } from 'react';
import type { Usuario } from '../services/tipos';

export interface AuthContextType {
  user: Usuario | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  checkAuth: () => Promise<void>;
  atualizarUsuario: (usuario: Usuario) => void;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
