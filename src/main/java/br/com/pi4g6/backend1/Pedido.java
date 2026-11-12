package br.com.pi4g6.backend1;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

        public boolean estaNaFila() {
            return this == RECEBIDO || this == EM_PREPARO;
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

    private static final int MAXIMO_DE_ACOMPANHAMENTOS = 3;

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

    public List<Cardapio> comanda() {
        Random sorteio = new Random(semente());
        List<Cardapio> principais = Cardapio.principais();
        List<Cardapio> acompanhamentos = Cardapio.acompanhamentos();

        List<Cardapio> comanda = new ArrayList<>();
        comanda.add(principais.get(sorteio.nextInt(principais.size())));

        int quantosExtras = sorteio.nextInt(MAXIMO_DE_ACOMPANHAMENTOS + 1);
        for (int posicao = 0; posicao < quantosExtras; posicao++) {
            comanda.add(acompanhamentos.get(sorteio.nextInt(acompanhamentos.size())));
        }
        return comanda;
    }

    public int minutosDePreparo() {
        int total = 0;
        for (Cardapio item : comanda()) {
            total += item.minutosDePreparo();
        }
        return total;
    }

    private long semente() {
        return senha * 31L + criadoEm.toEpochSecond(ZoneOffset.UTC);
    }

}
