# CT7: painel

| Linha | Valor |
|---|---|
| Fronteira | HTTP |
| Quem chama | Frontend, pela página `/prototipo-fluxo` (painel da cozinha), a cada segundo |
| Quem responde | Backend 1 |
| Atende | Nenhum E. É o protótipo do painel da cozinha, feito para mostrar o sistema a donos de restaurante. Só lê: não cria nem avança pedido. |
| Como chamar | `GET /painel` |

## Entrada

Não tem.

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| agora | data e hora | sim | 2026-11-23T19:43:38 |
| cozinheiros | inteiro | sim | 3 |
| ritmo | um de: normal, pico | sim | normal |
| chegadaMinSeg | inteiro | sim | 20 |
| chegadaMaxSeg | inteiro | sim | 30 |
| segundosPorMinuto | inteiro | sim | 10 |
| toleranciaProntoSeg | inteiro | sim | 15 |
| pedidos | lista | sim | (abaixo) |

Cada item de `pedidos`, do mais antigo para o mais novo:

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| senha | inteiro | sim | 42 |
| etapa | um de: recebido, em_preparo, pronto, encerrado | sim | em_preparo |
| segundosDePreparo | inteiro | sim | 60 |
| criadoEm | data e hora | sim | 2026-11-23T19:42:58 |
| inicioPreparo | data e hora | não (vazio em recebido) | 2026-11-23T19:43:07 |
| prontoEm | data e hora | não (vazio até ficar pronto) | 2026-11-23T19:44:08 |

## Erros

| Quando | O que volta |
|---|---|
| O banco não está disponível | Erro BANCO_INDISPONIVEL. O painel mostra "Não foi possível falar com o Backend 1." |
