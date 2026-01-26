package com.estoquefx.util;

import com.estoquefx.service.Leitor;
import com.estoquefx.model.Categoria;
import com.estoquefx.model.Estoque;
import com.estoquefx.model.Produto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

//todo terminar misc


// imprimir
// salvar como
// bug pesquisa
// revisar botões
//todo ícones
//import
//todo calendário?
//todo apenas urgente só funciona depois de pesquisar
//classe categoria
//todo supabase
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
    private static String ultimaAtualizacao = "";
    private static boolean negouAtualizacao = false;
    private static int qtdUrgentes;



    public static void contaUrgentes(){
        int c = 0;
        for  (Produto p : Estoque.getProdutos()) {
            if (p.getCompra()){
                c++;
            }
        }
        qtdUrgentes = c;
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

    // "não lembrar/lembrar
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

}


