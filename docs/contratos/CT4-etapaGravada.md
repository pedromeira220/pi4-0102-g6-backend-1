# CT4: etapaGravada

| Linha | Valor |
|---|---|
| Fronteira | Socket TCP (`java.net.Socket` com objetos Java serializados, no molde do Fazedor de Continhas) |
| Quem chama | Backend 1, sempre que grava uma mudança de etapa e, em seguida, para cada pedido que ficou na fila e teve a posição alterada por essa mudança |
| Quem responde | Backend 2. Não há resposta. |
| Atende | E3, E4 e E5 |
| Como chamar | O Backend 1 conecta no servidor de Socket do Backend 2 (porta 3000) e envia um objeto da classe `EtapaGravada`, subclasse de `Comunicado` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| senha | inteiro | sim | 42 |
| etapa | um de: recebido, em_preparo, pronto, encerrado | sim | em_preparo |
| estimativaMinutos | inteiro | não (vem só nas etapas recebido e em_preparo) | 8 |
| posicaoNaFila | inteiro | não (vem só nas etapas recebido e em_preparo) | 3 |
| atualizadoEm | data e hora | sim | 2026-10-13T19:20:00 |

Quando um pedido vira pronto ou encerrado, ele sai da fila e os pedidos que estavam atrás dele sobem uma posição. O Backend 1 envia uma etapaGravada para cada um deles, com a mesma etapa e a posição nova. A mensagem sai quando a etapa ou a posição muda.

## Saída

Não tem.

## Erros

| Quando | O que volta |
|---|---|
| O Backend 2 está fora do ar ou a conexão caiu | Nada para as páginas. O Backend 1 registra a falha e tenta reconectar na próxima mudança de etapa. A etapa já está gravada no banco, então a página mostra a etapa certa quando for recarregada (CT1). |
