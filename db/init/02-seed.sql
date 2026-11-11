-- Pedido de teste do card H01.1: senha 42, para a tela de senha ter o que achar.
-- Roda depois do 01-schema.sql (ordem alfabética), só na primeira subida com o volume vazio.
INSERT INTO pedido (senha, etapa) VALUES (42, 'recebido');
