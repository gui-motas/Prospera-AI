-- Baseline do schema (V1).
--
-- O projeto nasceu com ddl-auto=update e nao tinha migracoes. Este script cria o
-- schema que o Hibernate gerava, com nomes de constraint explicitos, para que
-- alteracoes futuras sejam versionadas e revisaveis.

create table usuarios (
    id uuid not null,
    nome varchar(120) not null,
    email varchar(160) not null,
    senha_hash varchar(255) not null,
    perfil varchar(20) not null,
    ativo boolean not null,
    bloqueado boolean not null,
    criado_em timestamp(6) not null,
    primary key (id),
    constraint uk_usuarios_email unique (email)
);

create table contas_financeiras (
    id uuid not null,
    usuario_id uuid not null,
    nome varchar(80) not null,
    tipo varchar(20) not null,
    saldo_inicial numeric(15, 2) not null,
    primary key (id),
    -- duas contas com o mesmo nome para o mesmo usuario nao fazem sentido
    constraint uk_contas_financeiras_usuario_nome unique (usuario_id, nome),
    constraint fk_contas_financeiras_usuario foreign key (usuario_id) references usuarios (id)
);

create table categorias (
    id uuid not null,
    usuario_id uuid not null,
    nome varchar(60) not null,
    tipo varchar(10) not null,
    primary key (id),
    -- o servico ja recusa nome repetido; a constraint fecha a corrida entre duas requisicoes
    constraint uk_categorias_usuario_nome_tipo unique (usuario_id, nome, tipo),
    constraint fk_categorias_usuario foreign key (usuario_id) references usuarios (id)
);

create table movimentacoes (
    id uuid not null,
    conta_id uuid not null,
    categoria_id uuid not null,
    descricao varchar(140) not null,
    valor numeric(15, 2) not null,
    data date not null,
    tipo varchar(10) not null,
    primary key (id),
    constraint fk_movimentacoes_conta foreign key (conta_id) references contas_financeiras (id),
    constraint fk_movimentacoes_categoria foreign key (categoria_id) references categorias (id)
);

-- as consultas do painel filtram pelo dono da conta e por periodo
create index idx_movimentacoes_conta_data on movimentacoes (conta_id, data);
create index idx_movimentacoes_data on movimentacoes (data);

create table orcamentos (
    id uuid not null,
    categoria_id uuid not null,
    limite numeric(15, 2) not null,
    inicio_periodo date not null,
    fim_periodo date not null,
    primary key (id),
    constraint fk_orcamentos_categoria foreign key (categoria_id) references categorias (id)
);

create table metas_financeiras (
    id uuid not null,
    usuario_id uuid not null,
    nome varchar(120) not null,
    valor_desejado numeric(15, 2) not null,
    valor_acumulado numeric(15, 2) not null,
    prazo date not null,
    status varchar(20) not null,
    criada_em date not null,
    primary key (id),
    constraint fk_metas_financeiras_usuario foreign key (usuario_id) references usuarios (id)
);

create table cenarios_financeiros (
    id uuid not null,
    meta_id uuid not null,
    descricao varchar(200) not null,
    economia_mensal_simulada numeric(15, 2) not null,
    prazo_projetado date not null,
    criado_em timestamp(6) not null,
    primary key (id),
    constraint fk_cenarios_financeiros_meta foreign key (meta_id) references metas_financeiras (id)
);

create table analises_financeiras (
    id uuid not null,
    usuario_id uuid not null,
    meta_id uuid,
    cenario_id uuid,
    data_geracao timestamp(6) not null,
    tipo varchar(30) not null,
    resultado varchar(8000) not null,
    primary key (id),
    constraint fk_analises_financeiras_usuario foreign key (usuario_id) references usuarios (id),
    constraint fk_analises_financeiras_meta foreign key (meta_id) references metas_financeiras (id),
    constraint fk_analises_financeiras_cenario foreign key (cenario_id) references cenarios_financeiros (id)
);

create table tokens_confirmacao (
    id uuid not null,
    usuario_id uuid not null,
    token varchar(64) not null,
    criado_em timestamp(6) not null,
    expira_em timestamp(6) not null,
    confirmado_em timestamp(6),
    primary key (id),
    constraint uk_tokens_confirmacao_token unique (token),
    constraint fk_tokens_confirmacao_usuario foreign key (usuario_id) references usuarios (id)
);

-- usado para contar as analises do dia no limite de custo da IA
create index idx_analises_financeiras_usuario_data on analises_financeiras (usuario_id, data_geracao);
