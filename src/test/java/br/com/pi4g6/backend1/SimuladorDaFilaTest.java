package br.com.pi4g6.backend1;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "demo.intervaloMs=600000")
class SimuladorDaFilaTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 13, 19, 0, 0);

    @Autowired
    private SimuladorDaFila simulador;

    @Autowired
    private PedidoRepository pedidos;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void limpar() {
        jdbc.update("delete from pedido");
    }

    private List<String> etapasPorSenha() {
        return jdbc.queryForList("select etapa from pedido order by senha", String.class);
    }

    @Test
    void cadaCicloCriaUmPedidoNovoEmRecebido() {
        simulador.executarCiclo(INICIO);

        assertThat(pedidos.count()).isEqualTo(1);
        assertThat(etapasPorSenha()).containsExactly("recebido");
    }

    @Test
    void emQuatroCiclosEntramQuatroPedidosEOPrimeiroEncerra() {
        for (int ciclo = 0; ciclo < 4; ciclo++) {
            simulador.executarCiclo(INICIO.plusSeconds(30L * ciclo));
        }

        assertThat(pedidos.count()).isEqualTo(4);
        assertThat(etapasPorSenha()).containsExactly("encerrado", "pronto", "em_preparo", "recebido");
    }

    @Test
    void senhaNovaNuncaRepeteADeOutroPedido() {
        for (int ciclo = 0; ciclo < 5; ciclo++) {
            simulador.executarCiclo(INICIO.plusSeconds(30L * ciclo));
        }

        assertThat(jdbc.queryForList("select senha from pedido order by senha", Integer.class))
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void cicloSegueFuncionandoComOBackend2ForaDoAr() {
        simulador.executarCiclo(INICIO);
        simulador.executarCiclo(INICIO.plusSeconds(30));

        assertThat(pedidos.count()).isEqualTo(2);
        assertThat(etapasPorSenha()).containsExactly("em_preparo", "recebido");
    }

    @Test
    void posicaoNaFilaAcompanhaQuemSai() {
        for (int ciclo = 0; ciclo < 3; ciclo++) {
            simulador.executarCiclo(INICIO.plusSeconds(30L * ciclo));
        }

        Pedido terceiro = pedidos.findBySenha(3).orElseThrow();
        assertThat(pedidos.contarNaFilaCriadosAntesDe(terceiro.getCriadoEm()) + 1).isEqualTo(2);
    }

    @Test
    void reiniciarApagaTudoEAsSenhasRecomecam() {
        simulador.executarCiclo(INICIO);
        simulador.executarCiclo(INICIO.plusSeconds(30));
        jdbc.update("delete from pedido");

        simulador.executarCiclo(INICIO.plusSeconds(60));

        assertThat(jdbc.queryForList("select senha from pedido", Integer.class)).containsExactly(1);
    }
}
