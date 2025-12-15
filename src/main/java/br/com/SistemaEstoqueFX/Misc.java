package br.com.SistemaEstoqueFX;

import java.util.HashSet;

public class Misc {
    /* todo funcionalidades
     *   todo remover nomes conflitantes
     *    */
    public static HashSet<String> categorias = new HashSet<>();

    private static double total;

    public static void addCategoria(String categoria) {
        categorias.add(categoria);
    }

    public static void carregaCategorias(){
        for (Produto p : Produto.estoque) {
            addCategoria(p.getCategoria());
        }
    }

    public static boolean isNumeric(String n) {
        try{
            Double.parseDouble(n);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static String isUrgente(Produto produto){
        if (produto.getCompra()){
            return "Compra urgente";
        } else {
            return "Estoque suficiente";
        }
    }

    public static void atualizaTotal() {
        total = 0;
        for (Produto p : Produto.estoque) {
            total += p.getQtd() * p.getVlrUnd();
        }
    }

    public static double getTotal(){
        return total;
    }

}

