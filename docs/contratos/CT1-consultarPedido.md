# CT1: consultarPedido

| Linha | Valor |
|---|---|
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E1, E2, E4 e E6 |
| Como chamar | `GET /pedidos/{senha}` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| senha | inteiro | sim | 42 |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| senha | inteiro | sim | 42 |
| etapa | um de: recebido, em_preparo, pronto, encerrado | sim | em_preparo |
| estimativaMinutos | inteiro | não (vem só nas etapas recebido e em_preparo) | 8 |
| atualizadoEm | data e hora | sim | 2026-10-13T19:20:00 |

## Erros

| Quando | O que volta |
|---|---|
| Não existe pedido com essa senha | Erro PEDIDO_NAO_ENCONTRADO. O Frontend mostra "Pedido não encontrado, confira o número na sua senha". |
| A senha enviada não é um número inteiro | Erro SENHA_INVALIDA |
| O banco não está disponível | Erro BANCO_INDISPONIVEL |
