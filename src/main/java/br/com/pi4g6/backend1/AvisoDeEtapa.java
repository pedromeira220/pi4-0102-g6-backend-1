package br.com.pi4g6.backend1;

import br.com.pi4g6.comunicado.EtapaGravada;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AvisoDeEtapa {

    private static final Logger log = LoggerFactory.getLogger(AvisoDeEtapa.class);

    private final String host;
    private final int porta;
    private final int timeoutMs;

    public AvisoDeEtapa(
            @Value("${backend2.host:localhost}") String host,
            @Value("${backend2.porta:3000}") int porta,
            @Value("${backend2.timeoutMs:2000}") int timeoutMs) {
        this.host = host;
        this.porta = porta;
        this.timeoutMs = timeoutMs;
    }

    public void enviar(EtapaGravada aviso) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, porta), timeoutMs);
            try (ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream())) {
                saida.writeObject(aviso);
                saida.flush();
            }
        } catch (IOException erro) {
            log.warn("Backend 2 indisponivel ao avisar a senha {}: {}", aviso.getSenha(), erro.getMessage());
        }
    }
}
