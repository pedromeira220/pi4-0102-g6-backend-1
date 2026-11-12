package br.com.pi4g6.backend1;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "demo.intervaloMs=600000")
class EstimativaDeEsperaTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 11, 12, 0, 0);
    private static final LocalDateTime SEGUNDO = INICIO.plusSeconds(20);
    private static final LocalDateTime TERCEIRO = INICIO.plusSeconds(40);

    @Autowired
    private PedidoRepository pedidos;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void limpar() {
        jdbc.update("delete from pedido");
    }

    private FilaDePedidos cozinhaCom(int postos) {
        return new FilaDePedidos(pedidos, postos);
    }

    private void inserir(int senha, String etapa, LocalDateTime criadoEm) {
        jdbc.update("insert into pedido (senha, etapa, criado_em, atualizado_em) values (?, ?, ?, ?)",
                senha, etapa, criadoEm, criadoEm);
    }

    private int preparoDe(int senha, LocalDateTime criadoEm) {
        return new Pedido(senha, criadoEm).minutosDePreparo();
    }

    private Pedido buscar(int senha) {
        return pedidos.findBySenha(senha).orElseThrow();
    }

    @Test
    void sozinhoNaFilaAEsperaEhOProprioPreparo() {
        inserir(1, "recebido", INICIO);

        assertThat(cozinhaCom(3).estimativaMinutosDe(buscar(1), INICIO))
                .isEqualTo(preparoDe(1, INICIO));
    }

    @Test
    void cozinhaEmSerieSomaTodosOsDaFrente() {
        inserir(1, "em_preparo", INICIO);
        inserir(2, "recebido", SEGUNDO);
        inserir(3, "recebido", TERCEIRO);

        int esperado = preparoDe(1, INICIO) + preparoDe(2, SEGUNDO) + preparoDe(3, TERCEIRO);

        assertThat(cozinhaCom(1).estimativaMinutosDe(buscar(3), INICIO)).isEqualTo(esperado);
    }

    @Test
    void comDoisPostosEsperaOPostoQueLiberaPrimeiro() {
        inserir(1, "em_preparo", INICIO);
        inserir(2, "recebido", SEGUNDO);
        inserir(3, "recebido", TERCEIRO);

        int esperado = Math.min(preparoDe(1, INICIO), preparoDe(2, SEGUNDO))
                + preparoDe(3, TERCEIRO);

        assertThat(cozinhaCom(2).estimativaMinutosDe(buscar(3), INICIO)).isEqualTo(esperado);
    }

    @Test
    void comPostoSobrandoOPedidoNaoEspera() {
        inserir(1, "em_preparo", INICIO);
        inserir(2, "recebido", SEGUNDO);
        inserir(3, "recebido", TERCEIRO);

        assertThat(cozinhaCom(3).estimativaMinutosDe(buscar(3), INICIO))
                .isEqualTo(preparoDe(3, TERCEIRO));
    }

    @Test
    void maisPostosNuncaAumentamAEspera() {
        for (int senha = 1; senha <= 6; senha++) {
            inserir(senha, "recebido", INICIO.plusSeconds(10L * senha));
        }

        Pedido ultimo = buscar(6);
        int comUmPosto = cozinhaCom(1).estimativaMinutosDe(ultimo, INICIO);
        int comTresPostos = cozinhaCom(3).estimativaMinutosDe(ultimo, INICIO);
        int comDezPostos = cozinhaCom(10).estimativaMinutosDe(ultimo, INICIO);

        assertThat(comTresPostos).isLessThanOrEqualTo(comUmPosto);
        assertThat(comDezPostos).isLessThanOrEqualTo(comTresPostos);
        assertThat(comDezPostos).isEqualTo(preparoDe(6, INICIO.plusSeconds(60)));
    }

    @Test
    void quandoODaFrenteSaiAEsperaDeQuemFicouDiminui() {
        inserir(1, "em_preparo", INICIO);
        inserir(2, "recebido", SEGUNDO);

        int antes = cozinhaCom(1).estimativaMinutosDe(buscar(2), INICIO);
        jdbc.update("update pedido set etapa = 'pronto' where senha = 1");
        int depois = cozinhaCom(1).estimativaMinutosDe(buscar(2), INICIO);

        assertThat(depois).isLessThan(antes).isEqualTo(preparoDe(2, SEGUNDO));
    }

    @Test
    void prontoEEncerradoNaoTemEsperaNemPosicao() {
        inserir(1, "pronto", INICIO);
        inserir(2, "encerrado", SEGUNDO);

        FilaDePedidos cozinha = cozinhaCom(3);
        for (int senha : new int[] {1, 2}) {
            assertThat(cozinha.estimativaMinutosDe(buscar(senha), INICIO)).isNull();
            assertThat(cozinha.posicaoDe(buscar(senha))).isNull();
        }
    }

    @Test
    void aEsperaTemPisoDeUmMinuto() {
        inserir(1, "em_preparo", INICIO);

        assertThat(cozinhaCom(3).estimativaMinutosDe(buscar(1), INICIO.plusHours(5)))
                .isEqualTo(1);
    }
}
