package br.com.pi4g6.backend1;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    Optional<Pedido> findBySenha(int senha);
}
