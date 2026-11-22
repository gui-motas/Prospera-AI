import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { APP_NOME, APP_SUBTITULO } from '../../config/app';
import { useAuth } from '../../contexts/useAuth';
import { Icone } from '../Icone';
import { ThemeToggle } from '../ThemeToggle';

interface ItemMenu {
  rota: string;
  rotulo: string;
  icone: string;
  somenteAdmin?: boolean;
  /** Aparece direto no menu inferior do celular. */
  principalMobile?: boolean;
}

const MENU: ItemMenu[] = [
  { rota: '/painel', rotulo: 'Painel', icone: 'grid_view', principalMobile: true },
  { rota: '/movimentacoes', rotulo: 'Movimentações', icone: 'receipt_long', principalMobile: true },
  { rota: '/contas', rotulo: 'Contas', icone: 'account_balance_wallet' },
  { rota: '/categorias', rotulo: 'Categorias', icone: 'sell' },
  { rota: '/orcamentos', rotulo: 'Orçamentos', icone: 'donut_large' },
  { rota: '/metas', rotulo: 'Metas', icone: 'flag', principalMobile: true },
  { rota: '/planejamento', rotulo: 'ProsperAI', icone: 'auto_awesome', principalMobile: true },
  { rota: '/perfil', rotulo: 'Meu perfil', icone: 'person' },
  { rota: '/admin', rotulo: 'Administração', icone: 'admin_panel_settings', somenteAdmin: true },
];

function saudacao(): string {
  const hora = new Date().getHours();
  if (hora < 12) return 'Bom dia,';
  if (hora < 18) return 'Boa tarde,';
  return 'Boa noite,';
}

/**
 * Estrutura das páginas internas: menu lateral no computador e menu inferior no celular
 * (RNF-006), com link para pular ao conteúdo e navegação identificada (RNF-007).
 */
export function AppShell() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [maisAberto, setMaisAberto] = useState(false);

  const itens = MENU.filter((i) => !i.somenteAdmin || isAdmin);
  const primeiroNome = user?.nome.split(' ')[0] ?? '';
  const inicial = (user?.nome.charAt(0) ?? 'U').toUpperCase();

  const sair = async () => {
    setMaisAberto(false);
    await logout();
    navigate('/login');
  };

  const classeLink = ({ isActive }: { isActive: boolean }) => `sidebar-link${isActive ? ' ativo' : ''}`;

  return (
    <div className="app-shell">
      <a href="#conteudo" className="skip-link">Pular para o conteúdo</a>

      {/* Computador */}
      <aside className="sidebar">
        <div className="sidebar-marca">
          <span className="marca-icone marca-icone-logo" aria-label="Logo Prospera">
            <img src="/WhatsApp%20Image%202026-11-16%20at%2020.12.57.jpeg" alt="Prospera" />
          </span>
          <span className="marca-texto">
            <span className="marca-nome">{APP_NOME}</span>
            <span className="marca-sub">{APP_SUBTITULO}</span>
          </span>
        </div>

        <nav aria-label="Menu principal" className="sidebar-nav">
          {itens.map((item) => (
            <NavLink key={item.rota} to={item.rota} className={classeLink}>
              <Icone nome={item.icone} />
              <span>{item.rotulo}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-rodape">
          <div className="usuario-resumo">
            <span className="header-avatar-container" aria-hidden="true">{inicial}</span>
            <span className="usuario-resumo-texto">
              <span className="usuario-nome">{user?.nome}</span>
              <span className="usuario-perfil">{isAdmin ? 'Administrador' : 'Usuário'}</span>
            </span>
          </div>
          <div className="d-flex gap-2">
            <ThemeToggle />
            <button type="button" className="header-btn" onClick={sair} aria-label="Sair da conta" title="Sair">
              <Icone nome="logout" />
            </button>
          </div>
        </div>
      </aside>

      {/* Celular */}
      <header className="mobile-header">
        <div className="d-flex align-center gap-3">
          <span className="header-avatar-container" aria-hidden="true">{inicial}</span>
          <div>
            <p className="text-xs text-muted m-0">{saudacao()}</p>
            <p className="mobile-header-nome m-0">{primeiroNome}</p>
          </div>
        </div>
        <ThemeToggle />
      </header>

      <main id="conteudo" className="app-conteudo" tabIndex={-1}>
        <Outlet />
      </main>

      <nav className="bottom-nav" aria-label="Menu inferior">
        {itens.filter((i) => i.principalMobile).map((item) => (
          <NavLink key={item.rota} to={item.rota} className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
            <Icone nome={item.icone} />
            <span>{item.rotulo.replace(' IA', '')}</span>
          </NavLink>
        ))}
        <button
          type="button"
          className="nav-item"
          onClick={() => setMaisAberto(true)}
          aria-haspopup="dialog"
          aria-expanded={maisAberto}
        >
          <Icone nome="menu" />
          <span>Mais</span>
        </button>
      </nav>

      {maisAberto && (
        <div className="sheet-overlay" onClick={(e) => e.target === e.currentTarget && setMaisAberto(false)}>
          <div className="sheet" role="dialog" aria-modal="true" aria-label="Mais opções">
            <div className="sheet-alca" aria-hidden="true" />
            <nav aria-label="Mais opções">
              {itens.filter((i) => !i.principalMobile).map((item) => (
                <NavLink key={item.rota} to={item.rota} className={classeLink} onClick={() => setMaisAberto(false)}>
                  <Icone nome={item.icone} />
                  <span>{item.rotulo}</span>
                </NavLink>
              ))}
            </nav>
            <button type="button" className="sidebar-link sidebar-link-sair" onClick={sair}>
              <Icone nome="logout" />
              <span>Sair</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
