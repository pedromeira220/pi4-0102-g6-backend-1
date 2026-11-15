package br.com.pi4g6.backend1;

import br.com.pi4g6.comunicado.EtapaGravada;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AvisoDeEtapa implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(AvisoDeEtapa.class);

    private final String host;
    private final int porta;
    private final int timeoutMs;
    private Parceiro parceiro;

    public AvisoDeEtapa(
            @Value("${backend2.host:localhost}") String host,
            @Value("${backend2.porta:3000}") int porta,
            @Value("${backend2.timeoutMs:2000}") int timeoutMs) {
        this.host = host;
        this.porta = porta;
        this.timeoutMs = timeoutMs;
    }

    public synchronized void enviar(EtapaGravada aviso) {
        try {
            if (parceiro == null || !parceiro.estaConectado()) {
                parceiro = Parceiro.conectar(host, porta, timeoutMs);
            }
            parceiro.enviar(aviso);
        } catch (IOException erro) {
            desconectar();
            log.warn("Backend 2 indisponivel ao avisar a senha {}: {}", aviso.getSenha(), erro.getMessage());
        }
    }

    private void desconectar() {
        if (parceiro != null) {
            parceiro.close();
            parceiro = null;
        }
    }

    @PreDestroy
    @Override
    public synchronized void close() {
        desconectar();
    }
}
