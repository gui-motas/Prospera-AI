import type { ReactNode } from 'react';
import { APP_NOME, APP_SUBTITULO } from '../config/app';
import { ThemeToggle } from './ThemeToggle';

interface AuthLayoutProps {
  children: ReactNode;
  title: string;
  subtitle: string;
}

/** Layout das telas públicas (login, cadastro, confirmação). */
export function AuthLayout({ children, title, subtitle }: AuthLayoutProps) {
  return (
    <div className="auth-container">
      <div className="auth-background" aria-hidden="true">
        <span className="auth-blob auth-blob-1" />
        <span className="auth-blob auth-blob-2" />
        <span className="auth-grid" />
      </div>
      <div className="auth-theme-toggle">
        <ThemeToggle />
      </div>
      <main className="auth-card fade-in">
        <div className="auth-brand">
          <span className="auth-brand-mark" aria-label="Logo Prospera">
            <img src="/WhatsApp%20Image%202026-11-16%20at%2020.12.57.jpeg" alt="Prospera" />
          </span>
          <span className="auth-brand-text">
            <span className="auth-brand-name">{APP_NOME}</span>
            <span className="auth-brand-sub">{APP_SUBTITULO}</span>
          </span>
        </div>
        <div className="auth-header">
          {title && <h1 className="auth-title">{title}</h1>}
          <p className="auth-subtitle">{subtitle}</p>
        </div>
        {children}
      </main>
    </div>
  );
}
