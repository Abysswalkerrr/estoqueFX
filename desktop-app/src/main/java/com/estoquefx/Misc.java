package com.estoquefx;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

// imprimir
// salvar como
// bug pesquisa
// revisar botões
//todo ícones
//todo abas?
//todo decifrar import
// todo calendário!?
//todo documentação talvez?
//todo FAQ
//todo README
//todo tasks?
//todo POO
//todo simplificar?
//todo limpar arquivos/projetos
//todo https://openjfx.io/ - olhar community
//todo https://www.reddit.com/r/JavaFX/ - tirar ideias


public class Misc {
    public static HashSet<String> categorias = new HashSet<>();
    public static HashSet<String> nomes = new HashSet<>();
    private static String ultimaAtualizacao = "";

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

    public static void setUltimaAtualizacao(String ultimaAtualizacao) {
        Misc.ultimaAtualizacao = ultimaAtualizacao;
    }

    public static String getUltimaAtualizacao(){return Misc.ultimaAtualizacao;}

    public static void updateTime(){
        LocalDateTime hora = LocalDateTime.now();
        setUltimaAtualizacao(hora.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));

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


