# ADR 0002: simular a cozinha por cozinheiros, com chegadas sorteadas

**Status:** aceito (novembro de 2026; substitui o ciclo fixo de 30 s da Entrega 1)

**Contexto:** A Entrega 1 previa uma fila de demonstração em que, a cada 30 s, entrava um pedido e todos os existentes avançavam uma etapa. Nesse modelo todo pedido leva o mesmo tempo, a fila nunca passa de dois pedidos e a estimativa de tempo (E4) e a posição na fila não têm o que mostrar: a resposta seria sempre a mesma. Para apresentar o sistema a donos de restaurante, o grupo também quis um painel da cozinha que mostrasse a fila se formando no horário de pico.

**Decisão:** O simulador do Backend 1 roda um relógio a cada 1 s (`demo.intervaloMs`) e trata a cozinha como 3 cozinheiros (`cozinha.postos`). Os pedidos chegam em intervalos sorteados: 20 a 30 s no ritmo normal, 10 a 20 s no pico (CT8). Cada pedido espera em recebido até sobrar um cozinheiro, fica em preparo pelo tempo dos itens da comanda (enum `Cardapio`, 1 min do cardápio = 10 s na demonstração), passa a pronto e é encerrado 15 s depois. A tabela `pedido` ganhou `inicio_preparo` e `pronto_em` (CT2).

**Alternativas consideradas:**
- Manter o ciclo fixo de 30 s: descartada porque a estimativa e a posição na fila ficariam constantes, sem demonstrar o E4.
- Painel do atendente avançando os pedidos à mão: descartada por custo (cerca de 44 h, fora do escopo da Entrega 1) e porque a demonstração precisaria de uma pessoa operando o painel.

**Consequências:** O "feito quando" do E2 ("a cada 30 s entra um pedido novo") passa a valer como "a cada 20 a 30 s, sorteado". A demonstração fica menos previsível: o tempo de cada pedido depende da comanda sorteada e da fila. A estimativa continua em minutos de cozinha real, então o pedido fica pronto antes do que ela diz (1 min = 10 s).
