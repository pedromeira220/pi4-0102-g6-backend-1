# CT4: etapaGravada

| Linha | Valor |
|---|---|
| Fronteira | Socket TCP, uma mensagem por linha em JSON, codificada em UTF-8 |
| Quem chama | Backend 1, sempre que grava uma mudança de etapa e, em seguida, para cada pedido que ficou na fila e teve a posição alterada por essa mudança |
| Quem responde | Backend 2. Não há resposta. |
| Atende | E3, E4 e E5 |
| Como chamar | O Backend 1 abre um socket na porta 3000 do Backend 2, escreve o JSON da mensagem e termina com `\n` |

## Formato da mensagem

Cada mensagem é um objeto JSON em uma única linha, terminada por `\n`. O Backend 2 lê
caractere a caractere até a quebra de linha e recusa linha com mais de 8192
caracteres. Várias mensagens podem ir na mesma conexão, uma por linha.

A versão anterior desta ficha pedia objetos Java serializados, no molde do Fazedor de
Continhas. Os dois módulos usam JSON, que dispensa classe compartilhada entre os
repositórios e não amarra os dois ao mesmo `serialVersionUID`.

Exemplo do que sai do Backend 1:

```json
{"senha":42,"etapa":"em_preparo","estimativaMinutos":8,"posicaoNaFila":3,"atualizadoEm":"2026-10-13T19:20:00"}
```

Campo ausente significa campo sem valor. Em `pronto` e `encerrado`, `estimativaMinutos`
e `posicaoNaFila` não aparecem na mensagem.

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
