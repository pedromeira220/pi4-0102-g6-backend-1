package br.com.pi4g6.backend1;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilaDePedidos {

    private final PedidoRepository pedidos;
    private final int postosDaCozinha;

    public FilaDePedidos(PedidoRepository pedidos, @Value("${cozinha.postos:3}") int postosDaCozinha) {
        this.pedidos = pedidos;
        this.postosDaCozinha = Math.max(1, postosDaCozinha);
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
        int esperaAteSobrarPosto = distribuirNosPostos(aFrente, agora);
        return Math.max(1, esperaAteSobrarPosto + preparoRestanteDe(pedido, agora));
    }

    private int distribuirNosPostos(List<Pedido> aFrente, LocalDateTime agora) {
        int[] ocupacao = new int[postosDaCozinha];
        for (Pedido anterior : aFrente) {
            int posto = postoQueLiberaPrimeiro(ocupacao);
            ocupacao[posto] += preparoRestanteDe(anterior, agora);
        }
        return ocupacao[postoQueLiberaPrimeiro(ocupacao)];
    }

    private int postoQueLiberaPrimeiro(int[] ocupacao) {
        int escolhido = 0;
        for (int posto = 1; posto < ocupacao.length; posto++) {
            if (ocupacao[posto] < ocupacao[escolhido]) {
                escolhido = posto;
            }
        }
        return escolhido;
    }

    private int preparoRestanteDe(Pedido pedido, LocalDateTime agora) {
        int preparo = pedido.minutosDePreparo();
        long decorridos = Duration.between(pedido.getCriadoEm(), agora).toMinutes();
        long restante = preparo - decorridos;
        return (int) Math.max(0, Math.min(preparo, restante));
    }
}
