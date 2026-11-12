package br.com.pi4g6.backend1;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    Optional<Pedido> findBySenha(int senha);

    @Query("""
            select count(p) from Pedido p
            where p.etapa in (br.com.pi4g6.backend1.Pedido$Etapa.RECEBIDO,
                              br.com.pi4g6.backend1.Pedido$Etapa.EM_PREPARO)
              and p.criadoEm < :criadoEm
            """)
    int contarNaFilaCriadosAntesDe(@Param("criadoEm") LocalDateTime criadoEm);

    @Query("select coalesce(max(p.senha), 0) from Pedido p")
    int maiorSenha();

    @Query("""
            select p from Pedido p
            where p.etapa in (br.com.pi4g6.backend1.Pedido$Etapa.RECEBIDO,
                              br.com.pi4g6.backend1.Pedido$Etapa.EM_PREPARO)
            order by p.criadoEm
            """)
    List<Pedido> naFilaPorOrdemDeChegada();

    @Query("select p from Pedido p where p.etapa <> br.com.pi4g6.backend1.Pedido$Etapa.ENCERRADO order by p.criadoEm")
    List<Pedido> naoEncerradosPorOrdemDeChegada();

    @Query("""
            select p from Pedido p
            where p.etapa in (br.com.pi4g6.backend1.Pedido$Etapa.RECEBIDO,
                              br.com.pi4g6.backend1.Pedido$Etapa.EM_PREPARO)
              and p.criadoEm < :criadoEm
            order by p.criadoEm
            """)
    List<Pedido> naFilaCriadosAntesDe(@Param("criadoEm") LocalDateTime criadoEm);
}
