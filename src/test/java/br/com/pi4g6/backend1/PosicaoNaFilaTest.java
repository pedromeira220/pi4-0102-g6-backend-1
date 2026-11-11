package br.com.pi4g6.backend1;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class PosicaoNaFilaTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 13, 19, 0, 0);

    @Autowired
    private PedidoRepository pedidos;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void limpar() {
        jdbc.update("delete from pedido");
    }

    private void inserir(int senha, String etapa, LocalDateTime criadoEm) {
        jdbc.update("insert into pedido (senha, etapa, criado_em, atualizado_em) values (?, ?, ?, ?)",
                senha, etapa, criadoEm, criadoEm);
    }

    @Test
    void oMaisAntigoDaFilaEOPrimeiro() {
        inserir(1, "recebido", INICIO);
        inserir(2, "recebido", INICIO.plusMinutes(1));
        inserir(3, "em_preparo", INICIO.plusMinutes(2));

        assertThat(pedidos.contarNaFilaCriadosAntesDe(INICIO) + 1).isEqualTo(1);
        assertThat(pedidos.contarNaFilaCriadosAntesDe(INICIO.plusMinutes(1)) + 1).isEqualTo(2);
        assertThat(pedidos.contarNaFilaCriadosAntesDe(INICIO.plusMinutes(2)) + 1).isEqualTo(3);
    }

    @Test
    void prontoEEncerradoNaoContamNaFila() {
        inserir(1, "pronto", INICIO);
        inserir(2, "encerrado", INICIO.plusMinutes(1));
        inserir(3, "recebido", INICIO.plusMinutes(2));

        assertThat(pedidos.contarNaFilaCriadosAntesDe(INICIO.plusMinutes(2)) + 1).isEqualTo(1);
    }

    @Test
    void quandoOPrimeiroSaiOsDeTrasSobem() {
        inserir(1, "em_preparo", INICIO);
        inserir(2, "recebido", INICIO.plusMinutes(1));
        inserir(3, "recebido", INICIO.plusMinutes(2));

        assertThat(pedidos.contarNaFilaCriadosAntesDe(INICIO.plusMinutes(2)) + 1).isEqualTo(3);

        jdbc.update("update pedido set etapa = 'pronto' where senha = 1");

        assertThat(pedidos.contarNaFilaCriadosAntesDe(INICIO.plusMinutes(2)) + 1).isEqualTo(2);
    }

    @Test
    void filaVaziaDaPrimeiraPosicao() {
        assertThat(pedidos.contarNaFilaCriadosAntesDe(INICIO) + 1).isEqualTo(1);
    }

    @Test
    void etapaGravadaPeloJavaVoltaComoEnum() {
        pedidos.save(new Pedido(77, INICIO));

        assertThat(pedidos.findBySenha(77)).isPresent().get()
                .extracting(Pedido::getEtapa).isEqualTo(Pedido.Etapa.RECEBIDO);
        assertThat(jdbc.queryForObject("select etapa from pedido where senha = 77", String.class))
                .isEqualTo("recebido");
    }
}
