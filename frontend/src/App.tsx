import { Navigate, Route, Routes } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AppShell } from './components/layout/AppShell';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AdminPage } from './pages/AdminPage';
import { CadastroPage } from './pages/CadastroPage';
import { CategoriasPage } from './pages/CategoriasPage';
import { ConfirmarContaPage } from './pages/ConfirmarContaPage';
import { ContasPage } from './pages/ContasPage';
import { LoginPage } from './pages/LoginPage';
import { MetasPage } from './pages/MetasPage';
import { MovimentacoesPage } from './pages/MovimentacoesPage';
import { OrcamentosPage } from './pages/OrcamentosPage';
import { PainelPage } from './pages/PainelPage';
import { PerfilPage } from './pages/PerfilPage';
import { PlanejamentoPage } from './pages/PlanejamentoPage';

export default function App() {
  return (
    <>
      <Toaster position="top-right" toastOptions={{ className: 'toast' }} />
      <Routes>
        {/* Públicas */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />
        <Route path="/confirmar-conta" element={<ConfirmarContaPage />} />

        {/* Exigem autenticação (RNF-004) */}
        <Route element={<ProtectedRoute><AppShell /></ProtectedRoute>}>
          <Route path="/painel" element={<PainelPage />} />
          <Route path="/movimentacoes" element={<MovimentacoesPage />} />
          <Route path="/contas" element={<ContasPage />} />
          <Route path="/categorias" element={<CategoriasPage />} />
          <Route path="/orcamentos" element={<OrcamentosPage />} />
          <Route path="/metas" element={<MetasPage />} />
          <Route path="/planejamento" element={<PlanejamentoPage />} />
          <Route path="/perfil" element={<PerfilPage />} />
          <Route path="/admin" element={<ProtectedRoute somenteAdmin><AdminPage /></ProtectedRoute>} />
        </Route>

        <Route path="/" element={<Navigate to="/painel" replace />} />
        <Route path="*" element={<Navigate to="/painel" replace />} />
      </Routes>
    </>
  );
}
