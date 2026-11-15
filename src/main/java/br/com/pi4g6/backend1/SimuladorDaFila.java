package br.com.pi4g6.backend1;

import br.com.pi4g6.comunicado.EtapaGravada;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SimuladorDaFila {

    private final PedidoRepository pedidos;
    private final AvisoDeEtapa avisos;
    private final FilaDePedidos fila;

    public SimuladorDaFila(PedidoRepository pedidos, AvisoDeEtapa avisos, FilaDePedidos fila) {
        this.pedidos = pedidos;
        this.avisos = avisos;
        this.fila = fila;
    }

    @Scheduled(fixedRateString = "${demo.intervaloMs:30000}")
    public void rodarCiclo() {
        executarCiclo(LocalDateTime.now());
    }

    public void executarCiclo(LocalDateTime agora) {
        Set<Integer> jaNaLista = new LinkedHashSet<>();
        List<Pedido> paraAvisar = new ArrayList<>();

        for (Pedido pedido : pedidos.naoEncerradosPorOrdemDeChegada()) {
            if (pedido.avancar(agora)) {
                paraAvisar.add(pedidos.save(pedido));
                jaNaLista.add(pedido.getSenha());
            }
        }

        Pedido novo = pedidos.save(new Pedido(pedidos.maiorSenha() + 1, agora));
        paraAvisar.add(novo);
        jaNaLista.add(novo.getSenha());

        for (Pedido pedido : pedidos.naFilaPorOrdemDeChegada()) {
            if (jaNaLista.add(pedido.getSenha())) {
                paraAvisar.add(pedido);
            }
        }

        for (Pedido pedido : paraAvisar) {
            avisos.enviar(montarAviso(pedido, agora));
        }
    }

    private EtapaGravada montarAviso(Pedido pedido, LocalDateTime agora) {
        return new EtapaGravada(
                pedido.getSenha(),
                pedido.getEtapa().valor(),
                fila.estimativaMinutosDe(pedido, agora),
                fila.posicaoDe(pedido),
                pedido.getAtualizadoEm());
    }
}
