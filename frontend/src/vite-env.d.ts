/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL do backend. Vazio = mesma origem (proxy do Vite em dev e do Nginx no Docker). */
  readonly VITE_API_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
