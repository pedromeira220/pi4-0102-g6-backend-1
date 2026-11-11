package br.com.pi4g6.backend1;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class AvisoDeEtapa {

    private static final Logger log = LoggerFactory.getLogger(AvisoDeEtapa.class);

    private final ObjectMapper json;
    private final String host;
    private final int porta;
    private final int timeoutMs;

    public AvisoDeEtapa(
            ObjectMapper json,
            @Value("${backend2.host:localhost}") String host,
            @Value("${backend2.porta:3000}") int porta,
            @Value("${backend2.timeoutMs:2000}") int timeoutMs) {
        this.json = json;
        this.host = host;
        this.porta = porta;
        this.timeoutMs = timeoutMs;
    }

    public void enviar(EtapaGravada aviso) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, porta), timeoutMs);
            socket.setSoTimeout(timeoutMs);
            try (Writer saida = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)) {
                saida.write(linhaDe(aviso));
                saida.flush();
            }
        } catch (IOException erro) {
            log.warn("Backend 2 indisponivel ao avisar a senha {}: {}", aviso.senha(), erro.getMessage());
        }
    }

    String linhaDe(EtapaGravada aviso) {
        return json.writeValueAsString(aviso) + "\n";
    }
}
