# CT3: reiniciarDemo

| Linha | Valor |
|---|---|
| Fronteira | HTTP |
| Quem chama | Frontend, pela página `/demo` |
| Quem responde | Backend 1 |
| Atende | E2 |
| Como chamar | `POST /demo/reiniciar` |

## Entrada

Não tem.

## Saída

Não tem. A resposta sem conteúdo indica que a fila foi esvaziada.

## Erros

| Quando | O que volta |
|---|---|
| O banco não está disponível | Erro BANCO_INDISPONIVEL. A fila continua como estava. |
| Uma página estava aberta em um pedido que foi apagado | Nada. A página continua na última etapa até ser recarregada; ao recarregar, consultarPedido (CT1) devolve PEDIDO_NAO_ENCONTRADO. |
