package com.estoquefx;

import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

// imprimir
// salvar como
// bug pesquisa
// revisar botões
//todo pensar ícones
//todo abas
//todo import
// todo calendário?
//todo documentação talvez?
//todo FAQ
//todo README
//todo tarefas
//todo POO
//todo simplificar
//todo limpar arquivos/projetos
//todo relatórios
//todo nuvem
//todo Multi-usuário
//todo https://openjfx.io/ - olhar community
//todo https://www.reddit.com/r/JavaFX/ - tirar ideias


public class Misc {
    public static HashSet<String> categorias = new HashSet<>();
    public static HashSet<String> nomes = new HashSet<>();
    private static String ultimaAtualizacao = "";
    private static boolean negouAtualizacao = false;

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

    // Versão sem salvar, usada apenas na inicialização
    public static void setUltimaAtualizacaoSemSalvar(String ultimaAtualizacao) {
        Misc.ultimaAtualizacao = ultimaAtualizacao;
    }

    // Versão sem salvar, usada apenas na inicialização
    public static void setNegouAtualizacaoSemSalvar(boolean negouAtualizacao) {
        Misc.negouAtualizacao = negouAtualizacao;
    }


    public static void setNegouAtualizacao(boolean negouAtualizacao) throws IOException {
        Misc.negouAtualizacao = negouAtualizacao;
        Leitor.salvarNA(negouAtualizacao);
    }

    public static boolean getNegouAtualizacao(){return negouAtualizacao;}

    public static void updateTime(){
        LocalDateTime hora = LocalDateTime.now();
        setUltimaAtualizacao(hora.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
    }

    public static String getTime(){
        LocalDateTime tempo = LocalDateTime.now();
        return tempo.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
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


