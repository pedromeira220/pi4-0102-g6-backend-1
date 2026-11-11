package br.com.pi4g6.backend1;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EtapaGravada(
        int senha,
        String etapa,
        Integer estimativaMinutos,
        Integer posicaoNaFila,
        LocalDateTime atualizadoEm) {
}
