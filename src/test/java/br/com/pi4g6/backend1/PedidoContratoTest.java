package br.com.pi4g6.backend1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.pi4g6.backend1.Pedido.Etapa;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

@JsonTest
class PedidoContratoTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 13, 19, 20, 0);

    @Autowired
    private ObjectMapper json;

    @Test
    void etapaViajaEmMinusculo() {
        assertThat(Etapa.RECEBIDO.valor()).isEqualTo("recebido");
        assertThat(Etapa.EM_PREPARO.valor()).isEqualTo("em_preparo");
        assertThat(Etapa.PRONTO.valor()).isEqualTo("pronto");
        assertThat(Etapa.ENCERRADO.valor()).isEqualTo("encerrado");
    }

    @Test
    void etapaVoltaDoValorGravado() {
        for (Etapa etapa : Etapa.values()) {
            assertThat(Etapa.deValor(etapa.valor())).isEqualTo(etapa);
        }
    }

    @Test
    void etapaForaDaListaEstoura() {
        assertThatThrownBy(() -> Etapa.deValor("em_entrega"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void conversorDoBancoGravaEmMinusculo() {
        Pedido.EtapaConverter conversor = new Pedido.EtapaConverter();
        assertThat(conversor.convertToDatabaseColumn(Etapa.EM_PREPARO)).isEqualTo("em_preparo");
        assertThat(conversor.convertToEntityAttribute("em_preparo")).isEqualTo(Etapa.EM_PREPARO);
        assertThat(conversor.convertToDatabaseColumn(null)).isNull();
        assertThat(conversor.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void somenteRecebidoEEmPreparoEstaoNaFila() {
        assertThat(Etapa.RECEBIDO.estaNaFila()).isTrue();
        assertThat(Etapa.EM_PREPARO.estaNaFila()).isTrue();
        assertThat(Etapa.PRONTO.estaNaFila()).isFalse();
        assertThat(Etapa.ENCERRADO.estaNaFila()).isFalse();
    }

    @Test
    void comandaEhEstavelParaOMesmoPedido() {
        Pedido pedido = new Pedido(42, INICIO);
        assertThat(pedido.comanda()).isEqualTo(pedido.comanda());
        assertThat(pedido.minutosDePreparo()).isEqualTo(pedido.minutosDePreparo());
    }

    @Test
    void comandaVariaEntrePedidos() {
        Set<Integer> preparos = new HashSet<>();
        for (int senha = 1; senha <= 30; senha++) {
            preparos.add(new Pedido(senha, INICIO).minutosDePreparo());
        }
        assertThat(preparos).hasSizeGreaterThan(3);
    }

    @Test
    void comandaVariaEntreRodadasMesmoComAMesmaSenha() {
        Pedido deHoje = new Pedido(1, INICIO);
        Pedido deAmanha = new Pedido(1, INICIO.plusDays(1));
        assertThat(deHoje.comanda()).isNotEqualTo(deAmanha.comanda());
    }

    @Test
    void comandaTemUmPrincipalEAteTresAcompanhamentos() {
        for (int senha = 1; senha <= 50; senha++) {
            List<Cardapio> comanda = new Pedido(senha, INICIO).comanda();
            assertThat(comanda).hasSizeBetween(1, 4);
            assertThat(comanda.get(0).ehPrincipal()).isTrue();
            assertThat(comanda.subList(1, comanda.size()))
                    .allMatch(item -> !item.ehPrincipal());
        }
    }

    @Test
    void preparoEhASomaDosItensDaComanda() {
        Pedido pedido = new Pedido(42, INICIO);
        int somaManual = pedido.comanda().stream().mapToInt(Cardapio::minutosDePreparo).sum();
        assertThat(pedido.minutosDePreparo()).isEqualTo(somaManual);
    }

    @Test
    void todoItemDoCardapioLevaAlgumTempo() {
        for (Cardapio item : Cardapio.values()) {
            assertThat(item.minutosDePreparo()).isPositive();
        }
    }




    @Test
    void respostaNaFilaTrazEstimativaEPosicao() throws Exception {
        PedidoController.PedidoResposta resposta =
                new PedidoController.PedidoResposta(42, Etapa.EM_PREPARO, 8, 3, INICIO);

        assertThat(json.writeValueAsString(resposta)).isEqualTo(
                "{\"senha\":42,\"etapa\":\"em_preparo\",\"estimativaMinutos\":8,"
                        + "\"posicaoNaFila\":3,\"atualizadoEm\":\"2026-10-13T19:20:00\"}");
    }

    @Test
    void respostaProntaOmiteEstimativaEPosicao() throws Exception {
        PedidoController.PedidoResposta resposta =
                new PedidoController.PedidoResposta(42, Etapa.PRONTO, null, null, INICIO);

        assertThat(json.writeValueAsString(resposta))
                .isEqualTo("{\"senha\":42,\"etapa\":\"pronto\",\"atualizadoEm\":\"2026-10-13T19:20:00\"}");
    }
}
