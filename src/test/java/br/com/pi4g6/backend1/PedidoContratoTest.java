package br.com.pi4g6.backend1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.pi4g6.backend1.Pedido.Etapa;
import java.time.LocalDateTime;
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
    void estimativaCaiComOTempo() {
        Pedido pedido = new Pedido(42, INICIO);
        assertThat(pedido.estimativaMinutos(INICIO)).isEqualTo(15);
        assertThat(pedido.estimativaMinutos(INICIO.plusMinutes(7))).isEqualTo(8);
    }

    @Test
    void estimativaTemPisoDeUmMinuto() {
        Pedido pedido = new Pedido(42, INICIO);
        assertThat(pedido.estimativaMinutos(INICIO.plusMinutes(100))).isEqualTo(1);
        assertThat(pedido.estimativaMinutos(INICIO.plusDays(30))).isEqualTo(1);
    }

    @Test
    void estimativaNaoPassaDoNominalComRelogioAtrasado() {
        Pedido pedido = new Pedido(42, INICIO);
        assertThat(pedido.estimativaMinutos(INICIO.minusMinutes(50))).isEqualTo(15);
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
