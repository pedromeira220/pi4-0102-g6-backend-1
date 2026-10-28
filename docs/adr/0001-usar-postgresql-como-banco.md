# ADR 0001: usar PostgreSQL como banco

**Status:** aceito

**Contexto:** O serviço HTTP guarda os pedidos: a senha, a etapa atual e o horário da última mudança de etapa. A fila de demonstração cria um pedido novo a cada 30 s e avança os existentes sozinha, então o banco recebe escrita contínua durante a apresentação. O sistema inteiro sobe com `docker compose up` num clone limpo (entregável E6). O modelo de dados é pequeno e tem campos fixos.

**Decisão:** Usar PostgreSQL, rodando em um contêiner próprio no Docker Compose, com os dados em um volume.

**Alternativas consideradas:**
- H2 em memória durante o protótipo, como previa o relatório de Ideação: descartada porque os pedidos somem a cada reinício do contêiner, e trocar para PostgreSQL depois exigiria refazer a configuração e revisar as consultas.
- Banco de documentos (MongoDB): descartada porque os pedidos têm estrutura fixa e não há dado de formato variável que justifique abrir mão do modelo relacional.

**Consequências:**
- Positivas: os pedidos sobrevivem ao reinício do contêiner; o banco da demonstração é o mesmo que um uso real exigiria; o esquema relacional impede pedido sem senha ou com etapa inválida.
- Negativas: um contêiner a mais no Docker Compose e um volume para manter; o serviço HTTP precisa esperar o banco ficar pronto antes de subir, o que exige configurar a ordem de inicialização no Compose.
