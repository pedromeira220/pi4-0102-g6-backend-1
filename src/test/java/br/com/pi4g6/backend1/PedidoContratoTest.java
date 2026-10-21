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
    void conversorGravaELeOMesmoValor() {
        Pedido.EtapaConverter conversor = new Pedido.EtapaConverter();
        assertThat(conversor.convertToDatabaseColumn(Etapa.EM_PREPARO)).isEqualTo("em_preparo");
        assertThat(conversor.convertToEntityAttribute("em_preparo")).isEqualTo(Etapa.EM_PREPARO);
        assertThat(conversor.convertToDatabaseColumn(null)).isNull();
        assertThat(conversor.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void respostaDaConsultaSaiNoFormatoDoContrato() throws Exception {
        PedidoController.PedidoResposta resposta = new PedidoController.PedidoResposta(
                42, Etapa.EM_PREPARO, LocalDateTime.of(2026, 10, 13, 19, 20, 0));

        assertThat(json.writeValueAsString(resposta))
                .isEqualTo("{\"senha\":42,\"etapa\":\"em_preparo\",\"atualizadoEm\":\"2026-10-13T19:20:00\"}");
    }
}
