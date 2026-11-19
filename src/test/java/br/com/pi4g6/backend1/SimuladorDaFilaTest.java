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
@TestPropertySource(properties = {
        "demo.intervaloMs=600000",
        "demo.segundosPorMinuto=10",
        "demo.toleranciaProntoSeg=15",
        "cozinha.postos=3"})
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
        simulador.reiniciarRelogio();
        simulador.mudarRitmo(SimuladorDaFila.Ritmo.NORMAL);
    }

    private void rodarSegundoASegundo(LocalDateTime de, int segundos) {
        for (int s = 0; s <= segundos; s++) {
            simulador.executarCiclo(de.plusSeconds(s));
        }
    }

    private Pedido senha(int senha) {
        return pedidos.findBySenha(senha).orElseThrow();
    }

    private void inserirEmPreparo(int senha, LocalDateTime inicio) {
        jdbc.update("""
                insert into pedido (senha, etapa, criado_em, atualizado_em, inicio_preparo)
                values (?, 'em_preparo', ?, ?, ?)""", senha, inicio, inicio, inicio);
    }

    @Test
    void oPrimeiroPedidoChegaEJaPegaUmCozinheiroLivre() {
        simulador.executarCiclo(INICIO);

        assertThat(pedidos.count()).isEqualTo(1);
        assertThat(senha(1).getEtapa()).isEqualTo(Pedido.Etapa.EM_PREPARO);
        assertThat(senha(1).getInicioPreparo()).isEqualTo(INICIO);
    }

    @Test
    void comTodosOsCozinheirosOcupadosOPedidoNovoEsperaEmRecebido() {
        for (int s = 1; s <= 3; s++) {
            inserirEmPreparo(s, INICIO);
        }

        simulador.executarCiclo(INICIO.plusSeconds(1));

        assertThat(senha(4).getEtapa()).isEqualTo(Pedido.Etapa.RECEBIDO);
        assertThat(senha(4).getInicioPreparo()).isNull();
    }

    @Test
    void ficaProntoQuandoAcabaOTempoDoCardapio() {
        simulador.executarCiclo(INICIO);
        int preparo = senha(1).segundosDePreparo(10);

        rodarSegundoASegundo(INICIO.plusSeconds(1), preparo - 2);
        assertThat(senha(1).getEtapa()).isEqualTo(Pedido.Etapa.EM_PREPARO);

        simulador.executarCiclo(INICIO.plusSeconds(preparo));
        assertThat(senha(1).getEtapa()).isEqualTo(Pedido.Etapa.PRONTO);
        assertThat(senha(1).getProntoEm()).isEqualTo(INICIO.plusSeconds(preparo));
    }

    @Test
    void encerraDepoisDaTolerancia() {
        simulador.executarCiclo(INICIO);
        int preparo = senha(1).segundosDePreparo(10);

        rodarSegundoASegundo(INICIO.plusSeconds(1), preparo + 13);
        assertThat(senha(1).getEtapa()).isEqualTo(Pedido.Etapa.PRONTO);

        simulador.executarCiclo(INICIO.plusSeconds(preparo + 15));
        assertThat(senha(1).getEtapa()).isEqualTo(Pedido.Etapa.ENCERRADO);
    }

    @Test
    void quandoUmCozinheiroLiberaOPrimeiroDaFilaEntraEmPreparo() {
        for (int s = 1; s <= 3; s++) {
            inserirEmPreparo(s, INICIO);
        }
        simulador.executarCiclo(INICIO.plusSeconds(1));
        assertThat(senha(4).getEtapa()).isEqualTo(Pedido.Etapa.RECEBIDO);

        int preparoDo1 = senha(1).segundosDePreparo(10);
        jdbc.update("update pedido set inicio_preparo = ? where senha in (2, 3)", INICIO.plusHours(1));
        simulador.executarCiclo(INICIO.plusSeconds(preparoDo1));

        assertThat(senha(1).getEtapa()).isEqualTo(Pedido.Etapa.PRONTO);
        assertThat(senha(4).getEtapa()).isEqualTo(Pedido.Etapa.EM_PREPARO);
    }

    @Test
    void chegadasNoRitmoNormalVemACada20a30Segundos() {
        rodarSegundoASegundo(INICIO, 300);

        assertThat(pedidos.count()).isBetween(300L / 30 + 1, 300L / 20 + 1);
    }

    @Test
    void noPicoChegamMaisPedidos() {
        simulador.mudarRitmo(SimuladorDaFila.Ritmo.PICO);
        rodarSegundoASegundo(INICIO, 300);

        assertThat(pedidos.count()).isBetween(300L / 20 + 1, 300L / 10 + 1);
    }

    @Test
    void nuncaHaMaisPedidosEmPreparoQueCozinheiros() {
        simulador.mudarRitmo(SimuladorDaFila.Ritmo.PICO);
        for (int s = 0; s <= 300; s++) {
            simulador.executarCiclo(INICIO.plusSeconds(s));
            assertThat(pedidos.findByEtapaOrderByCriadoEm(Pedido.Etapa.EM_PREPARO)).hasSizeLessThanOrEqualTo(3);
        }
        assertThat(pedidos.findByEtapaOrderByCriadoEm(Pedido.Etapa.RECEBIDO)).isNotEmpty();
    }

    @Test
    void senhaNovaNuncaRepeteADeOutroPedido() {
        rodarSegundoASegundo(INICIO, 120);

        List<Integer> senhas = jdbc.queryForList("select senha from pedido order by senha", Integer.class);
        for (int i = 0; i < senhas.size(); i++) {
            assertThat(senhas.get(i)).isEqualTo(i + 1);
        }
    }

    @Test
    void reiniciarApagaTudoEAsSenhasRecomecam() {
        rodarSegundoASegundo(INICIO, 60);
        jdbc.update("delete from pedido");

        simulador.executarCiclo(INICIO.plusSeconds(61));

        assertThat(jdbc.queryForList("select senha from pedido", Integer.class)).containsExactly(1);
    }
}
