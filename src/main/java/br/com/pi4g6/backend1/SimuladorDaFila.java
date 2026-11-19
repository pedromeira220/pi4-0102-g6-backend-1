package br.com.pi4g6.backend1;

import br.com.pi4g6.comunicado.EtapaGravada;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Simula a cozinha no lugar do painel do restaurante.
 *
 * <p>Pedidos chegam em intervalos sorteados. Cada um espera em Recebido até sobrar um cozinheiro
 * ({@code cozinha.postos}), fica Em preparo pelo tempo do cardápio e então Pronto; depois da
 * tolerância, Encerrado. O relógio roda a cada {@code demo.intervaloMs}.
 */
@Component
public class SimuladorDaFila {

    public enum Ritmo {
        NORMAL(20, 30),
        PICO(10, 20);

        public final int chegadaMinSeg;
        public final int chegadaMaxSeg;

        Ritmo(int chegadaMinSeg, int chegadaMaxSeg) {
            this.chegadaMinSeg = chegadaMinSeg;
            this.chegadaMaxSeg = chegadaMaxSeg;
        }
    }

    private final PedidoRepository pedidos;
    private final AvisoDeEtapa avisos;
    private final FilaDePedidos fila;
    private final int toleranciaProntoSeg;
    private final Random sorteio = new Random();

    private volatile Ritmo ritmo = Ritmo.NORMAL;
    private LocalDateTime proximaChegada;

    public SimuladorDaFila(
            PedidoRepository pedidos,
            AvisoDeEtapa avisos,
            FilaDePedidos fila,
            @Value("${demo.toleranciaProntoSeg:15}") int toleranciaProntoSeg) {
        this.pedidos = pedidos;
        this.avisos = avisos;
        this.fila = fila;
        this.toleranciaProntoSeg = toleranciaProntoSeg;
    }

    public Ritmo ritmo() {
        return ritmo;
    }

    public void mudarRitmo(Ritmo novo) {
        this.ritmo = novo;
    }

    public int toleranciaProntoSeg() {
        return toleranciaProntoSeg;
    }

    /** Para os testes: o próximo ciclo trata o relógio como recém-ligado. */
    synchronized void reiniciarRelogio() {
        proximaChegada = null;
    }

    @Scheduled(fixedRateString = "${demo.intervaloMs:1000}", initialDelayString = "${demo.intervaloMs:1000}")
    public void rodarCiclo() {
        executarCiclo(LocalDateTime.now());
    }

    public synchronized void executarCiclo(LocalDateTime agora) {
        Set<Integer> mudaram = new LinkedHashSet<>();
        int segundosPorMinuto = fila.segundosPorMinuto();

        for (Pedido pronto : pedidos.findByEtapaOrderByCriadoEm(Pedido.Etapa.PRONTO)) {
            LocalDateTime ficouPronto = pronto.getProntoEm() != null ? pronto.getProntoEm() : pronto.getAtualizadoEm();
            if (!agora.isBefore(ficouPronto.plusSeconds(toleranciaProntoSeg))) {
                pronto.encerrar(agora);
                pedidos.save(pronto);
                mudaram.add(pronto.getSenha());
            }
        }

        List<Pedido> emPreparo = pedidos.findByEtapaOrderByCriadoEm(Pedido.Etapa.EM_PREPARO);
        int ocupados = emPreparo.size();
        for (Pedido pedido : emPreparo) {
            long decorridos = Duration.between(pedido.inicioDoPreparo(), agora).toSeconds();
            if (decorridos >= pedido.segundosDePreparo(segundosPorMinuto)) {
                pedido.ficarPronto(agora);
                pedidos.save(pedido);
                mudaram.add(pedido.getSenha());
                ocupados--;
            }
        }

        if (proximaChegada == null || pedidos.count() == 0) {
            proximaChegada = agora;
        }
        while (!agora.isBefore(proximaChegada)) {
            Pedido novo = pedidos.save(new Pedido(pedidos.maiorSenha() + 1, agora));
            mudaram.add(novo.getSenha());
            proximaChegada = proximaChegada.plusSeconds(sortearIntervalo());
        }

        for (Pedido esperando : pedidos.findByEtapaOrderByCriadoEm(Pedido.Etapa.RECEBIDO)) {
            if (ocupados >= fila.postosDaCozinha()) {
                break;
            }
            esperando.iniciarPreparo(agora);
            pedidos.save(esperando);
            mudaram.add(esperando.getSenha());
            ocupados++;
        }

        if (!mudaram.isEmpty()) {
            avisar(mudaram, agora);
        }
    }

    /** Quem mudou de etapa, e todos ainda na fila: a posição e a estimativa deles também mudaram. */
    private void avisar(Set<Integer> mudaram, LocalDateTime agora) {
        List<Pedido> paraAvisar = new ArrayList<>();
        for (int senha : mudaram) {
            pedidos.findBySenha(senha).ifPresent(paraAvisar::add);
        }
        for (Pedido pedido : pedidos.naFilaPorOrdemDeChegada()) {
            if (!mudaram.contains(pedido.getSenha())) {
                paraAvisar.add(pedido);
            }
        }
        for (Pedido pedido : paraAvisar) {
            avisos.enviar(new EtapaGravada(
                    pedido.getSenha(),
                    pedido.getEtapa().valor(),
                    fila.estimativaMinutosDe(pedido, agora),
                    fila.posicaoDe(pedido),
                    pedido.getAtualizadoEm()));
        }
    }

    private int sortearIntervalo() {
        Ritmo atual = ritmo;
        return atual.chegadaMinSeg + sorteio.nextInt(atual.chegadaMaxSeg - atual.chegadaMinSeg + 1);
    }
}
