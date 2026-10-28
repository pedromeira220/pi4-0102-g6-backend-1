# Sistema de Gestão de Filas: Backend 1

Consulta de pedidos por HTTP e simulador da fila de demonstração. Projeto Integrador IV, Turma 0102, Grupo 06.

- Visão geral e fluxos: [pi4-0102-g6-frontend/docs/fluxos](https://github.com/pedromeira220/pi4-0102-g6-frontend/tree/main/docs/fluxos)
- Contratos deste módulo: [docs/contratos](docs/contratos)
- Decisões de arquitetura: [docs/adr](docs/adr)

## Banco

PostgreSQL em contêiner, com a tabela `pedido` do contrato [CT2](docs/contratos/CT2-pedido.md).

```bash
docker compose up -d db
```

- Conexão: `jdbc:postgresql://localhost:5432/pi4`, usuário `pi4`, senha `pi4` (dentro do Compose, o host é `db`).
- O schema está em `db/init/01-schema.sql` e roda só com o volume vazio. Depois de mudar o schema: `docker compose down -v && docker compose up -d db` (apaga os pedidos).
- No Spring, use `spring.jpa.hibernate.ddl-auto=validate`: o schema vem do script, não do Hibernate.
