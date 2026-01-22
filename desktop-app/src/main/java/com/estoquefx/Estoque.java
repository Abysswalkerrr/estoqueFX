package com.estoquefx;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class Estoque {
    private static LinkedHashSet<Produto> produtos;
    private static HashSet<Categoria> categorias;

    public static void addProduto(Produto produto){produtos.add(produto);}
    public static void removeProduto(Produto produto){produtos.remove(produto);}
    public static LinkedHashSet<Produto> getProdutos(){return produtos;}

    public static void addCategoria(Categoria categoria){categorias.add(categoria);}
    public static HashSet<Categoria> getCategorias(){return categorias;}

}
