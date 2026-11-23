# CT8: mudarRitmo

| Linha | Valor |
|---|---|
| Fronteira | HTTP |
| Quem chama | Frontend, pelos botões "Movimento normal" e "Horário de pico" do painel da cozinha (`/prototipo-fluxo`) |
| Quem responde | Backend 1 |
| Atende | E2 (muda o ritmo em que a fila de demonstração recebe pedidos) |
| Como chamar | `POST /demo/ritmo/{ritmo}` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| ritmo | um de: normal, pico | sim | pico |

No ritmo normal chega um pedido a cada 20 a 30 s, sorteado. No pico, a cada 10 a 20 s. O ritmo vale até ser trocado de novo ou até o Backend 1 reiniciar, quando volta a normal.

## Saída

Não tem. A resposta sem conteúdo indica que o ritmo mudou.

## Erros

| Quando | O que volta |
|---|---|
| O ritmo não é normal nem pico | Erro RITMO_INVALIDO |
