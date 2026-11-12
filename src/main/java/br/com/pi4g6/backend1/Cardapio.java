package br.com.pi4g6.backend1;

import java.util.Arrays;
import java.util.List;

public enum Cardapio {
    HAMBURGUER_DUPLO(5, true),
    HAMBURGUER_COM_QUEIJO(6, true),
    SANDUICHE_DE_FRANGO(5, true),
    HAMBURGUER_SIMPLES(3, true),
    PORCAO_DE_FRANGO(4, true),
    BATATA_FRITA(3, false),
    SORVETE_COM_CALDA(2, false),
    TORTA(2, false),
    SORVETE_SIMPLES(1, false),
    REFRIGERANTE(1, false),
    CAFE(1, false);

    private final int minutosDePreparo;
    private final boolean principal;

    Cardapio(int minutosDePreparo, boolean principal) {
        this.minutosDePreparo = minutosDePreparo;
        this.principal = principal;
    }

    public int minutosDePreparo() {
        return minutosDePreparo;
    }

    public boolean ehPrincipal() {
        return principal;
    }

    public static List<Cardapio> principais() {
        return Arrays.stream(values()).filter(Cardapio::ehPrincipal).toList();
    }

    public static List<Cardapio> acompanhamentos() {
        return Arrays.stream(values()).filter(item -> !item.ehPrincipal()).toList();
    }
}
