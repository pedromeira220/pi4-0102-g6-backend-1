package br.com.pi4g6.backend1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import br.com.pi4g6.comunicado.Comunicado;
import br.com.pi4g6.comunicado.EtapaGravada;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ApertoDeMaoBackend2Test {

    private static final LocalDateTime INSTANTE = LocalDateTime.of(2026, 10, 13, 19, 20, 0);

    private CompletableFuture<List<Comunicado>> servidorQueLe(ServerSocket servidor, int quantos) {
        return CompletableFuture.supplyAsync(() -> {
            List<Comunicado> recebidos = new ArrayList<>();
            try (Socket conexao = servidor.accept();
                    ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream())) {
                for (int i = 0; i < quantos; i++) {
                    recebidos.add((Comunicado) entrada.readObject());
                }
            } catch (IOException | ClassNotFoundException erro) {
                throw new IllegalStateException(erro);
            }
            return recebidos;
        });
    }

    @Test
    void oBackend2RecebeOObjetoSerializado() throws Exception {
        try (ServerSocket servidor = new ServerSocket(0)) {
            CompletableFuture<List<Comunicado>> recebido = servidorQueLe(servidor, 1);

            try (AvisoDeEtapa avisos = new AvisoDeEtapa("localhost", servidor.getLocalPort(), 2000)) {
                avisos.enviar(new EtapaGravada(42, "em_preparo", 8, 3, INSTANTE));
            }

            Comunicado comunicado = recebido.get(10, TimeUnit.SECONDS).get(0);
            assertThat(comunicado).isInstanceOf(EtapaGravada.class);

            EtapaGravada aviso = (EtapaGravada) comunicado;
            assertThat(aviso.getSenha()).isEqualTo(42);
            assertThat(aviso.getEtapa()).isEqualTo("em_preparo");
            assertThat(aviso.getEstimativaMinutos()).isEqualTo(8);
            assertThat(aviso.getPosicaoNaFila()).isEqualTo(3);
            assertThat(aviso.getAtualizadoEm()).isEqualTo(INSTANTE);
        }
    }

    @Test
    void aConexaoEhAbertaUmaVezEReaproveitada() throws Exception {
        try (ServidorDeUmaConexao servidor = new ServidorDeUmaConexao(3)) {
            try (AvisoDeEtapa avisos = new AvisoDeEtapa("localhost", servidor.porta(), 2000)) {
                avisos.enviar(new EtapaGravada(1, "recebido", 5, 1, INSTANTE));
                avisos.enviar(new EtapaGravada(2, "recebido", 7, 2, INSTANTE));
                avisos.enviar(new EtapaGravada(3, "em_preparo", 3, 3, INSTANTE));
            }

            List<Comunicado> recebidos = servidor.esperarMensagens();
            assertThat(recebidos).hasSize(3);
            assertThat(recebidos).extracting(c -> ((EtapaGravada) c).getSenha())
                    .containsExactly(1, 2, 3);
            assertThat(servidor.conexoesAceitas()).isEqualTo(1);
        }
    }

    @Test
    void semEstimativaEPosicaoOsCamposChegamNulos() throws Exception {
        try (ServerSocket servidor = new ServerSocket(0)) {
            CompletableFuture<List<Comunicado>> recebido = servidorQueLe(servidor, 1);

            try (AvisoDeEtapa avisos = new AvisoDeEtapa("localhost", servidor.getLocalPort(), 2000)) {
                avisos.enviar(new EtapaGravada(42, "pronto", null, null, INSTANTE));
            }

            EtapaGravada aviso = (EtapaGravada) recebido.get(10, TimeUnit.SECONDS).get(0);
            assertThat(aviso.getEtapa()).isEqualTo("pronto");
            assertThat(aviso.getEstimativaMinutos()).isNull();
            assertThat(aviso.getPosicaoNaFila()).isNull();
        }
    }

    @Test
    void aEtapaViajaNoValorQueOContratoPede() throws Exception {
        try (ServerSocket servidor = new ServerSocket(0)) {
            CompletableFuture<List<Comunicado>> recebido = servidorQueLe(servidor, 4);

            try (AvisoDeEtapa avisos = new AvisoDeEtapa("localhost", servidor.getLocalPort(), 2000)) {
                for (Pedido.Etapa etapa : Pedido.Etapa.values()) {
                    avisos.enviar(new EtapaGravada(1, etapa.valor(), null, null, INSTANTE));
                }
            }

            assertThat(recebido.get(10, TimeUnit.SECONDS))
                    .extracting(c -> ((EtapaGravada) c).getEtapa())
                    .containsExactly("recebido", "em_preparo", "pronto", "encerrado");
        }
    }

    @Test
    void comOBackend2ForaDoArOAvisoNaoEstoura() {
        try (AvisoDeEtapa avisos = new AvisoDeEtapa("localhost", 1, 300)) {
            assertThatCode(() -> avisos.enviar(new EtapaGravada(42, "em_preparo", 8, 3, INSTANTE)))
                    .doesNotThrowAnyException();
        }
    }

    private static final class ServidorDeUmaConexao implements AutoCloseable {

        private final ServerSocket servidor = new ServerSocket(0);
        private final CompletableFuture<List<Comunicado>> mensagens = new CompletableFuture<>();
        private volatile int conexoes;

        ServidorDeUmaConexao(int quantasMensagens) throws IOException {
            Thread tarefa = new Thread(() -> {
                List<Comunicado> recebidos = new ArrayList<>();
                try (Socket conexao = servidor.accept();
                        ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream())) {
                    conexoes++;
                    for (int i = 0; i < quantasMensagens; i++) {
                        recebidos.add((Comunicado) entrada.readObject());
                    }
                    mensagens.complete(recebidos);
                } catch (IOException | ClassNotFoundException erro) {
                    mensagens.completeExceptionally(erro);
                }
            });
            tarefa.setDaemon(true);
            tarefa.start();
        }

        int porta() {
            return servidor.getLocalPort();
        }

        int conexoesAceitas() {
            return conexoes;
        }

        List<Comunicado> esperarMensagens() throws Exception {
            return mensagens.get(10, TimeUnit.SECONDS);
        }

        @Override
        public void close() throws IOException {
            servidor.close();
        }
    }
}
