package br.com.pi4g6.backend1;

import java.util.Arrays;
import java.util.List;

public enum Cardapio {
    BIG_MAC(5, true),
    QUARTEIRAO_COM_QUEIJO(6, true),
    MCCHICKEN(5, true),
    CHEESEBURGER(3, true),
    MCNUGGETS(4, true),
    MCFRITAS(3, false),
    MCFLURRY(2, false),
    TORTA_DE_MACA(2, false),
    CASQUINHA(1, false),
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
