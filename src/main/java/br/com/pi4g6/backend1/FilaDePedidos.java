package br.com.pi4g6.backend1;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilaDePedidos {

    private final PedidoRepository pedidos;
    private final int postosDaCozinha;
    private final int segundosPorMinuto;

    @Autowired
    public FilaDePedidos(
            PedidoRepository pedidos,
            @Value("${cozinha.postos:3}") int postosDaCozinha,
            @Value("${demo.segundosPorMinuto:60}") int segundosPorMinuto) {
        this.pedidos = pedidos;
        this.postosDaCozinha = Math.max(1, postosDaCozinha);
        this.segundosPorMinuto = Math.max(1, segundosPorMinuto);
    }

    /** Sem compressão de tempo: um minuto do cardápio é um minuto de relógio. */
    FilaDePedidos(PedidoRepository pedidos, int postosDaCozinha) {
        this(pedidos, postosDaCozinha, 60);
    }

    public int postosDaCozinha() {
        return postosDaCozinha;
    }

    public int segundosPorMinuto() {
        return segundosPorMinuto;
    }

    public Integer posicaoDe(Pedido pedido) {
        if (!pedido.getEtapa().estaNaFila()) {
            return null;
        }
        return pedidos.contarNaFilaCriadosAntesDe(pedido.getCriadoEm()) + 1;
    }

    public Integer estimativaMinutosDe(Pedido pedido, LocalDateTime agora) {
        if (!pedido.getEtapa().estaNaFila()) {
            return null;
        }
        List<Pedido> aFrente = pedidos.naFilaCriadosAntesDe(pedido.getCriadoEm());
        long esperaAteSobrarPosto = distribuirNosPostos(aFrente, agora);
        long segundos = esperaAteSobrarPosto + preparoRestanteDe(pedido, agora);
        return (int) Math.max(1, (segundos + 59) / 60);
    }

    private long distribuirNosPostos(List<Pedido> aFrente, LocalDateTime agora) {
        long[] ocupacao = new long[postosDaCozinha];
        for (Pedido anterior : aFrente) {
            int posto = postoQueLiberaPrimeiro(ocupacao);
            ocupacao[posto] += preparoRestanteDe(anterior, agora);
        }
        return ocupacao[postoQueLiberaPrimeiro(ocupacao)];
    }

    private int postoQueLiberaPrimeiro(long[] ocupacao) {
        int escolhido = 0;
        for (int posto = 1; posto < ocupacao.length; posto++) {
            if (ocupacao[posto] < ocupacao[escolhido]) {
                escolhido = posto;
            }
        }
        return escolhido;
    }

    /** Em segundos. Quem ainda espera cozinheiro tem o preparo inteiro pela frente. */
    private long preparoRestanteDe(Pedido pedido, LocalDateTime agora) {
        long preparo = pedido.segundosDePreparo(segundosPorMinuto);
        if (pedido.getEtapa() != Pedido.Etapa.EM_PREPARO) {
            return preparo;
        }
        long decorridos = Duration.between(pedido.inicioDoPreparo(), agora).toSeconds();
        return Math.max(0, Math.min(preparo, preparo - decorridos));
    }
}
