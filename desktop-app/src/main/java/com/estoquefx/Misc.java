package com.estoquefx;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

// imprimir
// salvar como
// bug pesquisa
// revisar botões
//todo ícones
//import
//todo calendário?
//todo classe categoria
//todo documentação
//todo FAQ
//todo README
//todo POO
//todo limpar arquivos/projetos
//todo relatórios
//todo sync via drive
//todo Multi-usuário
//todo https://openjfx.io/ - olhar community


public class Misc {
    public static HashSet<String> nomes = new HashSet<>();
    private static String ultimaAtualizacao = "";
    private static boolean negouAtualizacao = false;
    private static int qtdUrgentes;

    private static double total;

    public static void carregaCategorias(){
        for (Produto p : Estoque.getProdutos()) {
            Categoria.addCategoria(p.getCategoria());
        }
    }

    public static void contaUrgentes(){
        int c = 0;
        for  (Produto p : Estoque.getProdutos()) {
            if (p.getCompra()){
                c++;
            }
        }
        qtdUrgentes = c;
    }

    public static void carregaNomes(){
        for (Produto p : Estoque.getProdutos()) {
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
        for (Produto p : Estoque.getProdutos()) {
            total += p.getQtd() * p.getVlrUnd();
        }
    }

    public static double getTotal(){
        return total;
    }



}


