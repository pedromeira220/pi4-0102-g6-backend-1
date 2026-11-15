package br.com.pi4g6.backend1;

import br.com.pi4g6.comunicado.Comunicado;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

final class Parceiro implements AutoCloseable {

    private final Socket socket;
    private final ObjectOutputStream saida;

    private Parceiro(Socket socket) throws IOException {
        this.socket = socket;
        this.saida = new ObjectOutputStream(socket.getOutputStream());
    }

    static Parceiro conectar(String host, int porta, int timeoutMs) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, porta), timeoutMs);
        socket.setSoTimeout(timeoutMs);
        return new Parceiro(socket);
    }

    void enviar(Comunicado comunicado) throws IOException {
        saida.writeObject(comunicado);
        saida.flush();
        saida.reset();
    }

    boolean estaConectado() {
        return socket.isConnected() && !socket.isClosed() && !socket.isOutputShutdown();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignorado) {
            return;
        }
    }
}
