package br.com.pi4g6.backend1;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Pedido {

    public enum Etapa {
        RECEBIDO,
        EM_PREPARO,
        PRONTO,
        ENCERRADO;

        @JsonValue
        public String valor() {
            return name().toLowerCase();
        }

        public static Etapa deValor(String valor) {
            return Etapa.valueOf(valor.toUpperCase());
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

    @Column(nullable = false, unique = true)
    private Integer senha;

    @Column(nullable = false, length = 12)
    private Etapa etapa;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
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
}
