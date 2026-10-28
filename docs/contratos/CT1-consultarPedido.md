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
| posicaoNaFila | inteiro | não (vem só nas etapas recebido e em_preparo) | 3 |
| atualizadoEm | data e hora | sim | 2026-10-13T19:20:00 |

A posição é 1 mais o número de pedidos em `recebido` ou `em_preparo` com `criado_em` anterior. O pedido que espera há mais tempo é o 1. Pronto e encerrado saem da fila e vêm sem posição. O Backend 1 calcula a posição na hora da consulta, a partir da tabela pedido (CT2).

## Erros

| Quando | O que volta |
|---|---|
| Não existe pedido com essa senha | Erro PEDIDO_NAO_ENCONTRADO. O Frontend mostra "Pedido não encontrado, confira o número na sua senha". |
| A senha enviada não é um número inteiro | Erro SENHA_INVALIDA |
| O banco não está disponível | Erro BANCO_INDISPONIVEL |
