package com.estoquefx.model;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class Estoque {
    private static double saldo;

    private static LinkedHashSet<Produto> produtos = new LinkedHashSet<>();
    private static HashSet<Categoria> categorias = new HashSet<>();
    private static HashSet<String> nomes = new HashSet<>();


    public static void addProduto(Produto produto){produtos.add(produto);}
    public static void removeProduto(Produto produto){produtos.remove(produto);}
    public static LinkedHashSet<Produto> getProdutos(){return produtos;}

    public static void addCategoria(Categoria categoria){categorias.add(categoria);}
    public static HashSet<Categoria> getCategorias(){return categorias;}

    public static void addNome(String nome) {
        nomes.add(nome);
    }
    public static void removeNome(String nome){
        nomes.remove(nome);
    }
    public static HashSet<String> getNomes(){return nomes;}

    public static double getSaldo(){return saldo;}
    public static void setSaldo(double saldo){
        Estoque.saldo = saldo;
    }


}
