package br.com.pi4g6.backend1;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
public class Pedido {

    public enum Etapa {
        RECEBIDO(15),
        EM_PREPARO(10),
        PRONTO(null),
        ENCERRADO(null);

        private final Integer minutosNominais;

        Etapa(Integer minutosNominais) {
            this.minutosNominais = minutosNominais;
        }

        @JsonValue
        public String valor() {
            return name().toLowerCase();
        }

        public static Etapa deValor(String valor) {
            return Etapa.valueOf(valor.toUpperCase());
        }

        public boolean estaNaFila() {
            return minutosNominais != null;
        }

        public Etapa proxima() {
            return switch (this) {
                case RECEBIDO -> EM_PREPARO;
                case EM_PREPARO -> PRONTO;
                case PRONTO -> ENCERRADO;
                case ENCERRADO -> null;
            };
        }
    }

    @Converter(autoApply = true)
    public static class EtapaConverter implements AttributeConverter<Etapa, String> {

        @Override
        public String convertToDatabaseColumn(Etapa etapa) {
            return etapa == null ? null : etapa.valor();
        }

        @Override
        public Etapa convertToEntityAttribute(String coluna) {
            return coluna == null ? null : Etapa.deValor(coluna);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer senha;

    private Etapa etapa;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    protected Pedido() {
    }

    public Pedido(int senha, LocalDateTime agora) {
        this.senha = senha;
        this.etapa = Etapa.RECEBIDO;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public Integer getId() {
        return id;
    }

    public int getSenha() {
        return senha;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public boolean avancar(LocalDateTime agora) {
        Etapa proxima = etapa.proxima();
        if (proxima == null) {
            return false;
        }
        this.etapa = proxima;
        this.atualizadoEm = agora;
        return true;
    }

    public Integer estimativaMinutos(LocalDateTime agora) {
        if (!etapa.estaNaFila()) {
            return null;
        }
        long decorridos = Duration.between(atualizadoEm, agora).toMinutes();
        long restantes = etapa.minutosNominais - decorridos;
        return (int) Math.max(1, Math.min(etapa.minutosNominais, restantes));
    }
}
