# Plataforma de Gestão Financeira Pessoal com Planejamento Inteligente de Metas

Projeto da disciplina **Engenharia de Software** — Escola de Tecnologias, Universidade Católica do Salvador (UCSAL).

**Equipe:** Fernanda Velame, Samara Parcero, Anna Luíza, Murilo Moreno e Marcelo Gomes.

A plataforma ajuda o usuário a organizar receitas, despesas, contas, categorias e orçamentos e a transformar
objetivos financeiros em metas com valor e prazo. Uma API de Inteligência Artificial apoia o planejamento:
analisa os dados já consolidados pela plataforma, avalia a viabilidade das metas e interpreta cenários em
linguagem natural. Os cálculos financeiros são sempre feitos pela própria plataforma; a IA é complementar.

Este repositório implementa a Especificação de Requisitos de Software (ERS) da equipe. A seção
[Rastreabilidade](#rastreabilidade-ers--código) mostra onde cada requisito está no código.

---

## Funcionalidades (escopo da ERS, seção 1.2)

| Funcionalidade | Onde usar |
|---|---|
| Cadastro e autenticação de usuários | Telas **Cadastro** e **Entrar** |
| Gerenciamento do perfil (nome e senha) | **Meu perfil** |
| Contas financeiras (CRUD, saldo calculado) | **Contas** |
| Categorias de receita/despesa (CRUD) | **Categorias** |
| Registro, edição e exclusão de receitas e despesas | **Movimentações** e atalhos do **Painel** |
| Histórico filtrado por período, categoria, tipo, conta e texto | **Movimentações** |
| Painel com saldo, receitas, despesas, distribuição dos gastos e evolução mensal | **Painel** |
| Limites de gastos por categoria e período, com alerta a partir de 80% | **Orçamentos** e **Painel** |
| Metas com valor e prazo, progresso, ritmo e valor mensal necessário | **Metas** |
| Diagnóstico de viabilidade das metas (cálculo da plataforma) | **Planejamento IA** |
| Simulação de cenários ("e se eu guardar X por mês / reduzir Y% em uma categoria?") | **Planejamento IA** |
| Análises com IA: situação financeira, viabilidade de meta e interpretação de cenário | **Planejamento IA** |
| Gerenciamento de usuários e monitoramento da operação (perfil Administrador) | **Administração** |

**Fora do escopo (ERS 1.2):** transferências bancárias, pagamentos, PIX, boletos, crédito, contratação de
produtos financeiros, compra/venda de investimentos, Open Finance, execução automática de sugestões da IA e
consultoria profissional de investimentos.

---

## Rastreabilidade ERS → código

Backend em `src/main/java/br/ucsal/gestaofinanceira/`; frontend em `frontend/src/`.

### Requisitos funcionais

| Requisito | Backend | Frontend |
|---|---|---|
| RF-001 Cadastro de usuário | `cadastro/CadastroController`, `CadastroService` | `pages/CadastroPage` |
| RF-002 Autenticação | `seguranca/SegurancaConfig` (POST `/api/v1/login`), `usuario/UsuarioService` | `pages/LoginPage`, `contexts/AuthContext` |
| RF-003 Contas financeiras | `conta/*` | `pages/ContasPage`, `modais/ContaModal` |
| RF-004 Categorias | `categoria/*` | `pages/CategoriasPage`, `modais/CategoriaModal` |
| RF-005 Registro de despesas | `movimentacao/MovimentacaoService.registrar` | `modais/MovimentacaoModal` |
| RF-006 Gerenciamento de movimentações | `movimentacao/*`, `MovimentacaoEspecificacoes` (filtros) | `pages/MovimentacoesPage` |
| RF-007 Dashboard financeiro | `financeiro/PainelController`, `ServicoFinanceiro.consolidarDados` | `pages/PainelPage`, `components/painel/*` |
| RF-08 Cadastro de metas | `meta/*` | `pages/MetasPage`, `modais/MetaModal` |
| RF-09 Acompanhamento de metas | `MetaFinanceira.calcularProgresso`, `estaNoRitmo`, `calcularValorMensalNecessario` | `pages/MetasPage`, `painel/MetasResumo` |
| RF-010 Planejamento inteligente | `planejamento/ServicoIA`, `PlanejamentoService`, `PlanejamentoController` | `pages/PlanejamentoPage` |

### Regras de negócio

| Regra | Implementação |
|---|---|
| RN-001 Propriedade dos dados | Toda consulta filtra pelo usuário da sessão (`seguranca/UsuarioAutenticado` + métodos `buscarDoUsuario`); recurso de outro usuário responde 404 |
| RN-002 Valor das movimentações > 0 | `Movimentacao.validarValor()` + `@Positive` no `MovimentacaoRequest` + validação no formulário |
| RN-003 Classificação das categorias | `Categoria.validarCompatibilidade(tipo)`; o formulário só oferece categorias do mesmo tipo |
| RN-004 Cálculo do saldo | `ContaFinanceira.calcularSaldo()` e `ServicoFinanceiro.calcularSaldo()` — saldo inicial + receitas − despesas, nunca armazenado |
| RN-005 Limite de orçamento | `Orcamento.editar()` (limite > 0, período válido, categoria de despesa) |
| RN-006 Dados obrigatórios da meta | `MetaFinanceira.editar()` + `MetaRequest` |
| RN-007 Valor da meta > 0 | `MetaFinanceira.editar()` + `@Positive` |
| RN-008 Acesso à IA só autenticado | `SegurancaConfig` (`/api/v1/planejamento/**` exige login) |
| RN-009 Proteção dos dados enviados à IA | `ServicoIA.prepararDados*`: só valores consolidados; a tela mostra os dados enviados |
| RN-010 Caráter informativo da IA | Instruções do `ServicoIA` + aviso exibido em toda análise (`ServicoIA.AVISO`) |
| RN-011 Autonomia das operações | A IA só devolve texto; ajustes (ex.: "Adotar novo prazo") exigem confirmação do usuário |
| RN-012 Precisão dos cálculos | `ServicoFinanceiro` e métodos das entidades calculam tudo; a IA recebe os números prontos |

### Requisitos não funcionais

| Requisito | Implementação |
|---|---|
| RNF-001 Tempo de resposta | Consultas agregadas no banco, índice por data, cache no frontend (React Query) |
| RNF-003 Proteção de senhas | BCrypt (`SegurancaConfig.passwordEncoder`), política mínima em `ValidadorSenha` |
| RNF-004 Controle de acesso | Sessão obrigatória; área `/api/v1/admin/**` só para o perfil Administrador |
| RNF-005 Privacidade e LGPD | Cadastro coleta apenas nome, e-mail e senha; minimização dos dados enviados à IA; sem credenciais no código |
| RNF-006 Responsividade | Menu lateral no computador e menu inferior no celular (`components/layout/AppShell`, `styles/layout.css`) |
| RNF-007 Acessibilidade | Rótulos em todos os campos, `aria-label` nos botões de ícone, modais com `role="dialog"`, link "Pular para o conteúdo", contraste AA, tabelas equivalentes aos gráficos |
| RNF-008 Disponibilidade | Health check em `/actuator/health`, `restart: unless-stopped` no Docker |
| RNF-09 Falha da API de IA | `IAIndisponivelException` → HTTP 503 com aviso; diagnóstico e simulação continuam funcionando |

### Diagrama de classes → código

| Classe (ERS) | Arquivo |
|---|---|
| Usuario | `usuario/Usuario.java` |
| ContaFinanceira | `conta/ContaFinanceira.java` |
| Categoria | `categoria/Categoria.java` |
| Movimentacao | `movimentacao/Movimentacao.java` |
| Orcamento | `orcamento/Orcamento.java` |
| MetaFinanceira | `meta/MetaFinanceira.java` |
| CenarioFinanceiro | `planejamento/CenarioFinanceiro.java` |
| AnaliseFinanceira | `planejamento/AnaliseFinanceira.java` |
| «service» ServicoFinanceiro | `financeiro/ServicoFinanceiro.java` |
| «service» ServicoIA | `planejamento/ServicoIA.java` (+ clientes em `planejamento/ia/`) |

### Processo de negócio (BPMN) → fluxo

1. **Informar nome, valor e prazo da meta** → tela Metas → `POST /api/v1/metas`
2. **Validar dados da meta (RN-006, RN-007)** → `MetaFinanceira.editar()`
3. **Calcular saldo e progresso (RN-004, RN-012)** → `ServicoFinanceiro`, `MetaFinanceira`
4. **Solicitar análise inteligente?** → botões da tela Planejamento IA
5. **Padronizar dados para envio (RN-009)** → `ServicoIA.prepararDados*`
6. **Enviar à API de IA / aguardar** → `ClienteIA` (timeout configurável)
7. **Exibir análise em linguagem natural (RN-010)** ou **informar indisponibilidade (RNF-09)**
8. **Simular cenário / ajustar plano** → simulador de cenários
9. **Confirmar novo prazo ou valor (RN-011)** → modal de confirmação → **Atualizar meta**

### Casos de uso → telas

| Ator | Caso de uso | Tela |
|---|---|---|
| Usuário | Cadastrar e autenticar usuário | Cadastro, Entrar |
| Usuário | Gerenciar contas, categorias e orçamentos | Contas, Categorias, Orçamentos |
| Usuário | Registrar e gerenciar movimentações | Movimentações, Painel |
| Usuário | Consultar dashboard financeiro | Painel |
| Usuário | Cadastrar e acompanhar metas | Metas |
| Usuário / API de IA | Solicitar planejamento inteligente e simular cenários | Planejamento IA |
| Administrador | Gerenciar usuários e monitorar operação | Administração |

---

## Tecnologias

- **Backend:** Java 21, Spring Boot 3.4 (Web, Data JPA, Security, Validation, Mail, Actuator), PostgreSQL, springdoc-openapi (Swagger)
- **Frontend:** React 19, TypeScript, Vite, React Router, TanStack Query, Axios, Recharts, react-hot-toast
- **Infra:** Docker Compose (PostgreSQL + backend + frontend com Nginx)
- **Testes:** JUnit 5 e Mockito (backend), Vitest (frontend)

## Como executar

### Com Docker (recomendado)

```bash
cp .env.example .env        # ajuste os valores, se quiser (IA, administrador…)
docker compose up -d --build
```

- Aplicação: http://localhost:5173
- API e Swagger: http://localhost:8090/swagger-ui.html
- Health check: http://localhost:8090/actuator/health

O Nginx do frontend encaminha `/api` para o backend, então frontend e API ficam na mesma origem.

### Em modo de desenvolvimento

Pré-requisitos: Java 21, Node.js 20+ e um PostgreSQL com o banco `gestao_financeira`
(ou suba só o banco: `docker compose up -d postgres`).

```bash
# Backend (porta 8090)
./mvnw spring-boot:run

# Frontend (porta 5173) – o Vite encaminha /api para http://localhost:8090
cd frontend
npm install
npm run dev
```

Variáveis do backend podem ser passadas pelo ambiente (veja `.env.example`), por exemplo:
`IA_CHAVE_API=... ADMIN_EMAIL=admin@exemplo.com ADMIN_PASSWORD=Senha123 ./mvnw spring-boot:run`.

## Configuração da IA (RF-010)

| Variável | Descrição |
|---|---|
| `IA_PROVEDOR` | `anthropic` (Claude) ou `openai` (qualquer API compatível com o formato da OpenAI) |
| `IA_CHAVE_API` | Chave da API. **Sem chave, o recurso inteligente fica indisponível e o resto funciona normalmente** |
| `IA_MODELO` | Nome do modelo (obrigatório para `openai`; para `anthropic`, vazio usa o padrão de `ConfiguracaoIA`) |
| `IA_URL_BASE` | URL base (opcional). Ex.: Gemini, Groq, OpenRouter ou Ollama local via provedor `openai` |
| `IA_TIMEOUT_SEGUNDOS` | Tempo máximo de espera pela resposta (padrão 30) |

**Dados enviados à IA (RN-009):** saldo total, capacidade mensal de economia, receitas e despesas por mês,
gastos por categoria e dados das metas (nome, valores, prazo, progresso). **Nunca** são enviados nome,
e-mail, senha, identificadores, descrições de movimentações ou nomes de contas. Na tela de Planejamento é
possível ver exatamente o que foi enviado em cada análise.

## Primeiro acesso

- Qualquer pessoa pode se cadastrar em **Cadastro**. O usuário já começa com categorias padrão e uma conta "Carteira".
- Para ter um **Administrador**, defina `ADMIN_EMAIL` e `ADMIN_PASSWORD` antes de subir o backend; ele é criado na inicialização.
- Confirmação de cadastro por e-mail é opcional (`APP_CONFIRMACAO_EMAIL=true` + SMTP). Sem SMTP, o link aparece no log do backend.

## Principais endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/cadastro` | Cadastrar usuário (RF-001) |
| POST | `/api/v1/login` · `/api/v1/logout` | Entrar (form `username`, `password`) e sair (RF-002) |
| GET/PUT | `/api/v1/usuarios/me` · PUT `/senha` | Perfil do usuário |
| GET | `/api/v1/painel?inicio=&fim=` | Dashboard (RF-007) |
| CRUD | `/api/v1/contas` | Contas (RF-003) |
| CRUD | `/api/v1/categorias` | Categorias (RF-004) |
| CRUD | `/api/v1/movimentacoes?dataInicio=&dataFim=&categoriaId=&tipo=&contaId=&texto=` | Movimentações (RF-005, RF-006) |
| CRUD | `/api/v1/orcamentos` | Limites de gastos (RN-005) |
| CRUD | `/api/v1/metas` · POST `/{id}/aportes` | Metas (RF-08, RF-09) |
| GET | `/api/v1/planejamento/status` · `/diagnostico` · `/analises` | Disponibilidade da IA, viabilidade e histórico |
| POST | `/api/v1/planejamento/analises/situacao` | Análise da situação financeira com IA |
| POST | `/api/v1/planejamento/metas/{id}/analises` | Viabilidade de uma meta com IA |
| GET/POST | `/api/v1/planejamento/metas/{id}/cenarios` | Simulação de cenários |
| POST | `/api/v1/planejamento/cenarios/{id}/interpretacoes` | Interpretação de cenário com IA |
| GET/PATCH | `/api/v1/admin/usuarios` · `/usuarios/{id}/situacao` · GET `/admin/status` | Administração |

Documentação completa e interativa no Swagger.

## Testes

```bash
./mvnw test                 # regras de negócio (RegrasDeNegocioTest), IA (ServicoIATest) e contexto
cd frontend && npm test     # utilitários de formatação e senha
```

## Estrutura

```
.
├── src/main/java/br/ucsal/gestaofinanceira/
│   ├── usuario/  cadastro/  seguranca/   # Usuario, cadastro, autenticação e acesso
│   ├── conta/  categoria/  movimentacao/ # ContaFinanceira, Categoria, Movimentacao
│   ├── orcamento/  meta/                 # Orcamento, MetaFinanceira
│   ├── financeiro/                       # ServicoFinanceiro e painel
│   ├── planejamento/                     # CenarioFinanceiro, AnaliseFinanceira, ServicoIA
│   │   └── ia/                           # clientes da API externa de IA
│   ├── admin/                            # perfil Administrador
│   └── comum/                            # exceções, tratamento de erros, Swagger
├── src/test/java/...                     # testes do backend
├── frontend/                             # aplicação React (Vite)
│   └── src/{pages,components,services,hooks,contexts,styles,utils,config}
├── compose.yaml · Dockerfile · Makefile · .env.example
```

## Segurança

- Nenhuma credencial fica no código: tudo vem de variáveis de ambiente (`.env` não é versionado).
- Senhas armazenadas apenas como hash BCrypt.
- Cookie de sessão `HttpOnly` e `SameSite=Lax` (frontend e API na mesma origem). Para hospedar em domínios
  diferentes, use `APP_COOKIE_SAMESITE=none` e `APP_COOKIE_SECURE=true` (veja `render.env`).
