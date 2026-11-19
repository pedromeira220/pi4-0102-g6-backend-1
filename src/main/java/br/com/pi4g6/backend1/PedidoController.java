package br.com.pi4g6.backend1;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
public class PedidoController {

    private final PedidoRepository pedidos;
    private final FilaDePedidos fila;
    private final SimuladorDaFila simulador;

    public PedidoController(PedidoRepository pedidos, FilaDePedidos fila, SimuladorDaFila simulador) {
        this.pedidos = pedidos;
        this.fila = fila;
        this.simulador = simulador;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PedidoResposta(
            int senha,
            Pedido.Etapa etapa,
            Integer estimativaMinutos,
            Integer posicaoNaFila,
            LocalDateTime atualizadoEm) {
    }

    @GetMapping("/pedidos/{senha}")
    public ResponseEntity<?> consultarPedido(@PathVariable int senha) {
        return pedidos.findBySenha(senha)
                .<ResponseEntity<?>>map(pedido -> ResponseEntity.ok(montarResposta(pedido, LocalDateTime.now())))
                .orElseGet(() -> erro(HttpStatus.NOT_FOUND, "PEDIDO_NAO_ENCONTRADO"));
    }

    @PostMapping("/demo/reiniciar")
    @Transactional
    public ResponseEntity<Void> reiniciarDemo() {
        pedidos.deleteAll();
        return ResponseEntity.noContent().build();
    }

    /** Um pedido no painel da cozinha: as etapas e os horários de cada passagem. */
    public record PedidoNoPainel(
            int senha,
            Pedido.Etapa etapa,
            int segundosDePreparo,
            LocalDateTime criadoEm,
            LocalDateTime inicioPreparo,
            LocalDateTime prontoEm) {
    }

    /** O painel da cozinha: todos os pedidos desde o último reinício e o que o simulador está usando. */
    public record Painel(
            LocalDateTime agora,
            int cozinheiros,
            String ritmo,
            int chegadaMinSeg,
            int chegadaMaxSeg,
            int segundosPorMinuto,
            int toleranciaProntoSeg,
            List<PedidoNoPainel> pedidos) {
    }

    @GetMapping("/painel")
    public Painel painel() {
        SimuladorDaFila.Ritmo ritmo = simulador.ritmo();
        int segundosPorMinuto = fila.segundosPorMinuto();
        List<PedidoNoPainel> lista = pedidos.findAllByOrderByCriadoEm().stream()
                .map(p -> new PedidoNoPainel(
                        p.getSenha(),
                        p.getEtapa(),
                        p.segundosDePreparo(segundosPorMinuto),
                        p.getCriadoEm(),
                        p.getEtapa() == Pedido.Etapa.RECEBIDO ? null : p.inicioDoPreparo(),
                        p.getProntoEm()))
                .toList();
        return new Painel(
                LocalDateTime.now(),
                fila.postosDaCozinha(),
                ritmo.name().toLowerCase(),
                ritmo.chegadaMinSeg,
                ritmo.chegadaMaxSeg,
                segundosPorMinuto,
                simulador.toleranciaProntoSeg(),
                lista);
    }

    @PostMapping("/demo/ritmo/{ritmo}")
    public ResponseEntity<?> mudarRitmo(@PathVariable String ritmo) {
        try {
            simulador.mudarRitmo(SimuladorDaFila.Ritmo.valueOf(ritmo.toUpperCase()));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException desconhecido) {
            return erro(HttpStatus.BAD_REQUEST, "RITMO_INVALIDO");
        }
    }

    PedidoResposta montarResposta(Pedido pedido, LocalDateTime agora) {
        return new PedidoResposta(
                pedido.getSenha(),
                pedido.getEtapa(),
                fila.estimativaMinutosDe(pedido, agora),
                fila.posicaoDe(pedido),
                pedido.getAtualizadoEm());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> senhaInvalida() {
        return erro(HttpStatus.BAD_REQUEST, "SENHA_INVALIDA");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> bancoIndisponivel() {
        return erro(HttpStatus.SERVICE_UNAVAILABLE, "BANCO_INDISPONIVEL");
    }

    private static ResponseEntity<?> erro(HttpStatus status, String codigo) {
        return ResponseEntity.status(status).body(Map.of("erro", codigo));
    }
}
