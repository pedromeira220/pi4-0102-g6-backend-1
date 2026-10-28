-- Tabela pedido, conforme o contrato CT2 (docs/contratos/CT2-pedido.md).
-- Roda só na primeira subida, com o volume vazio. Para recriar: docker compose down -v
CREATE TABLE pedido (
    id            integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    senha         integer   NOT NULL UNIQUE,
    etapa         text      NOT NULL CHECK (etapa IN ('recebido', 'em_preparo', 'pronto', 'encerrado')),
    criado_em     timestamp NOT NULL DEFAULT now(),
    atualizado_em timestamp NOT NULL DEFAULT now()
);
