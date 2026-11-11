package br.com.pi4g6.backend1;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

@JsonTest
class ApertoDeMaoBackend2Test {

    private static final LocalDateTime INSTANTE = LocalDateTime.of(2026, 10, 13, 19, 20, 0);
    private static final Pattern ETAPAS_ACEITAS = Pattern.compile("recebido|em_preparo|pronto|encerrado");

    record AtualizacaoPedido(
            Integer senha,
            String etapa,
            Integer estimativaMinutos,
            Integer posicaoNaFila,
            LocalDateTime atualizadoEm) {
    }

    @Autowired
    private ObjectMapper json;

    private String lerUmaLinhaComoOParceiro(BufferedReader entrada) throws IOException {
        StringBuilder linha = new StringBuilder();
        int caractere;
        while ((caractere = entrada.read()) != -1) {
            if (caractere == '\n') {
                return linha.toString();
            }
            linha.append((char) caractere);
        }
        return linha.isEmpty() ? null : linha.toString();
    }

    private String receberDoSocket(EtapaGravada aviso) throws Exception {
        try (ServerSocket servidor = new ServerSocket(0)) {
            CompletableFuture<String> recebido = CompletableFuture.supplyAsync(() -> {
                try (Socket conexao = servidor.accept();
                        BufferedReader entrada = new BufferedReader(new InputStreamReader(
                                conexao.getInputStream(), StandardCharsets.UTF_8))) {
                    return lerUmaLinhaComoOParceiro(entrada);
                } catch (IOException erro) {
                    throw new IllegalStateException(erro);
                }
            });

            new AvisoDeEtapa(json, "localhost", servidor.getLocalPort(), 2000).enviar(aviso);
            return recebido.get();
        }
    }

    @Test
    void oBackend2ConsegueLerEDesserializarOQueOBackend1Manda() throws Exception {
        EtapaGravada aviso = new EtapaGravada(42, "em_preparo", 8, 3, INSTANTE);

        String linha = receberDoSocket(aviso);
        AtualizacaoPedido lido = json.readValue(linha, AtualizacaoPedido.class);

        assertThat(lido.senha()).isEqualTo(42);
        assertThat(lido.etapa()).isEqualTo("em_preparo");
        assertThat(lido.estimativaMinutos()).isEqualTo(8);
        assertThat(lido.posicaoNaFila()).isEqualTo(3);
        assertThat(lido.atualizadoEm()).isEqualTo(INSTANTE);
    }

    @Test
    void aMensagemEUmaLinhaSoETerminaEmQuebraDeLinha() {
        String linha = new AvisoDeEtapa(json, "localhost", 1, 100)
                .linhaDe(new EtapaGravada(42, "em_preparo", 8, 3, INSTANTE));

        assertThat(linha).endsWith("\n");
        assertThat(linha.stripTrailing()).doesNotContain("\n");
        assertThat(linha.length()).isLessThan(8192);
    }

    @Test
    void osNomesDosCamposBatemComORecordDoBackend2() {
        String linha = new AvisoDeEtapa(json, "localhost", 1, 100)
                .linhaDe(new EtapaGravada(42, "em_preparo", 8, 3, INSTANTE));

        assertThat(linha.stripTrailing()).isEqualTo(
                "{\"senha\":42,\"etapa\":\"em_preparo\",\"estimativaMinutos\":8,"
                        + "\"posicaoNaFila\":3,\"atualizadoEm\":\"2026-10-13T19:20:00\"}");
    }

    @Test
    void aEtapaPassaNoPatternQueOBackend2Valida() {
        for (Pedido.Etapa etapa : Pedido.Etapa.values()) {
            assertThat(ETAPAS_ACEITAS.matcher(etapa.valor()).matches()).isTrue();
        }
    }

    @Test
    void semEstimativaEPosicaoOsCamposSaemDeFora() {
        String linha = new AvisoDeEtapa(json, "localhost", 1, 100)
                .linhaDe(new EtapaGravada(42, "pronto", null, null, INSTANTE));

        assertThat(linha.stripTrailing()).isEqualTo(
                "{\"senha\":42,\"etapa\":\"pronto\",\"atualizadoEm\":\"2026-10-13T19:20:00\"}");
    }
}
