package br.com.pi4g6.backend1;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
public class PedidoController {

    private final PedidoRepository pedidos;

    public PedidoController(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    public record PedidoResposta(int senha, Pedido.Etapa etapa, LocalDateTime atualizadoEm) {
    }

    @GetMapping("/pedidos/{senha}")
    public ResponseEntity<?> consultarPedido(@PathVariable int senha) {
        return pedidos.findBySenha(senha)
                .<ResponseEntity<?>>map(pedido -> ResponseEntity.ok(
                        new PedidoResposta(pedido.getSenha(), pedido.getEtapa(), pedido.getAtualizadoEm())))
                .orElseGet(() -> erro(HttpStatus.NOT_FOUND, "PEDIDO_NAO_ENCONTRADO"));
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
