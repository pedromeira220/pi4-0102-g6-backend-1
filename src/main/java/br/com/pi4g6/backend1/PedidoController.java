package br.com.pi4g6.backend1;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
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

    public PedidoController(PedidoRepository pedidos) {
        this.pedidos = pedidos;
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

    PedidoResposta montarResposta(Pedido pedido, LocalDateTime agora) {
        Integer posicaoNaFila = null;
        if (pedido.getEtapa().estaNaFila()) {
            posicaoNaFila = pedidos.contarNaFilaCriadosAntesDe(pedido.getCriadoEm()) + 1;
        }
        return new PedidoResposta(
                pedido.getSenha(),
                pedido.getEtapa(),
                pedido.estimativaMinutos(agora),
                posicaoNaFila,
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
