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

## Estimativa de minutos

Não há coluna para a estimativa, como não há para a posição. O Backend 1 a calcula na
hora da consulta, em três passos.

**A comanda.** A tabela não guarda itens, então o Backend 1 sorteia uma comanda a partir
de `senha` e `criado_em`, que não mudam depois do pedido nascer. O mesmo pedido devolve
sempre a mesma comanda, pedidos diferentes recebem comandas diferentes, e uma nova
rodada da demonstração não repete a anterior. Sem isso o número saltaria a cada vez que
o cliente recarregasse a tela.

**O preparo.** Cada item do cardápio tem um tempo médio, e o preparo do pedido é a soma
dos tempos dos seus itens. Os valores estão no enum `Cardapio`.

**A fila.** A espera não é só o preparo do próprio pedido: é também o que falta preparar
de quem chegou antes. A cozinha trabalha em paralelo, com um número de postos definido
em `cozinha.postos`, e cada pedido da frente ocupa o posto que liberar primeiro. A espera
é o tempo até sobrar um posto, mais o preparo do próprio pedido, com piso de um minuto.

Com um posto a cozinha fica em série e a espera é a soma de todos os que estão à frente.
Com posto sobrando, o pedido não espera por ninguém.

Isso é um modelo para a demonstração, não um cadastro de itens. Se o sistema passar a
ter itens de verdade, eles entram como tabela e este cálculo passa a ler dados em vez de
sorteá-los.

## Erros

| Quando | O que volta |
|---|---|
| Já existe pedido com a mesma senha | O banco recusa a gravação |
| A etapa gravada não está na lista | O banco recusa a gravação |

## Posição na fila

Não há coluna para a posição. O Backend 1 calcula a partir de `etapa` e `criado_em`: a posição é 1 mais o número de pedidos em `recebido` ou `em_preparo` com `criado_em` anterior. O pedido que espera há mais tempo é o 1. Pronto e encerrado saem da fila e vêm sem posição. Assim, quando um pedido sai da fila, a posição dos que estão atrás dele muda sem nenhuma gravação extra.
