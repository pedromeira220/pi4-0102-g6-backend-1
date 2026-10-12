# CT2: pedido

| Linha | Valor |
|---|---|
| Fronteira | Banco |
| Quem chama | Backend 1, que é o único módulo que lê e grava nesta tabela |
| Quem responde | Banco |
| Atende | E1, E2, E3, E4, E5 e E6 |
| Como chamar | Tabela `pedido`, chave `id` |

## Entrada e saída

Os campos gravados são os mesmos campos lidos.

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| id | inteiro | sim | 7 |
| senha | inteiro | sim | 42 |
| etapa | um de: recebido, em_preparo, pronto, encerrado | sim | em_preparo |
| criado_em | data e hora | sim | 2026-10-13T19:05:00 |
| atualizado_em | data e hora | sim | 2026-10-13T19:20:00 |

## Erros

| Quando | O que volta |
|---|---|
| Já existe pedido com a mesma senha | O banco recusa a gravação |
| A etapa gravada não está na lista | O banco recusa a gravação |
