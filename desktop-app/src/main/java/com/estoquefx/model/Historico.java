package com.estoquefx.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Historico {
    private static LinkedHashSet<Movimento> movimentos = new LinkedHashSet<>();

    public static void addMovimento(Movimento movimento) {movimentos.add(movimento);}

    public static void limpar() {
        movimentos.clear();
    }

    public static List<Movimento> getMovimentos() {
        return new ArrayList<>(movimentos);
    }

    public static List<Movimento> getMovimentosPorTipo(String tipo) {
        return movimentos.stream()
                .filter(m -> m.getTipo().equalsIgnoreCase(tipo))
                .collect(Collectors.toList());
    }

    public static List<Movimento> getMovimentosPorProduto(String codigo) {
        return movimentos.stream()
                .filter(m -> m.getCodigo().equals(codigo))
                .collect(Collectors.toList());
    }

    public static int getTotalMovimentacoes() {
        return movimentos.size();
    }

}
