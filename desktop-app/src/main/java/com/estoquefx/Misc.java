package com.estoquefx;

import java.util.HashSet;

//todo ícones
//todo revisar botões
//todo abas?
//todo decifrar import
//todo copiar excel lol
//todo calendário!?
//todo documentação talvez?
//todo FAQ
//todo README
//todo tasks?
//todo verificar POO? simplificar?
//todo https://openjfx.io/ - olhar community
//todo https://www.reddit.com/r/JavaFX/ - tirar ideias


public class Misc {
    public static HashSet<String> categorias = new HashSet<>();
    public static HashSet<String> nomes = new HashSet<>();

    private static double total;

    public static void addCategoria(String categoria) {
        categorias.add(categoria);
    }

    public static void carregaCategorias(){
        for (Produto p : Produto.estoque) {
            addCategoria(p.getCategoria());
        }
    }

    public static void carregaNomes(){
        for (Produto p : Produto.estoque) {
            addNome(p.getNome());
        }
    }

    public static void addNome(String nome) {
        nomes.add(nome);
    }

    public static void removeNome(String nome){
        nomes.remove(nome);
    }

    //muito provavelmente não vai ser mais usado
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


