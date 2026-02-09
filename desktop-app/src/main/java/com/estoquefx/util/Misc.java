package com.estoquefx.util;

import com.estoquefx.data.Leitor;
import java.io.IOException;

// revisar botões
//todo reabrir app
//todo erro é entrada de qtd e não movimentações
//todo btn load from estq, salvar .txt com nome - talvez pasta nomeada?,
//todo ícones
//todo calendário?
//todo supabase *
//todo documentação
//todo FAQ
//todo README
//todo relatórios *
//todo https://openjfx.io/ - olhar community


public class Misc {
    private static String ultimaAtualizacao = "";
    private static boolean negouAtualizacao = false;


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


