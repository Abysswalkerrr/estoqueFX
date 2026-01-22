package com.estoquefx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Categoria {
    public static Map<String, Categoria> categorias =  new HashMap<>();
    private HashSet<Produto> produtos = new  HashSet<>();
    private String nome;
    public Categoria(String nome) {
        this.nome = nome;
    }

    public HashSet<Produto> getProdutos() {return produtos;}
    public static HashSet<Produto> getProdutos(Categoria categoria) {return categoria.getProdutos();}

    public void addProduto(Produto p){
        produtos.add(p);
    }
    public static void addProduto(String nome, Produto p){
        Categoria c = categorias.get(nome);
        c.addProduto(p);
    }

    public void removeProduto(Produto p){
        produtos.remove(p);
    }
    public static void removeProduto(String nome, Produto p){
        Categoria c = categorias.get(nome);
        c.removeProduto(p);
    }

    public static void addCategoria(String nome){
        Categoria categoria = new Categoria(nome);
        categorias.putIfAbsent(nome, categoria);
    }

    public static Categoria getCategoria(String nome){
        try{
             categorias.get(nome);
        } catch(Exception e){
            addCategoria(nome);
        }
        return categorias.get(nome);
    }

    public void setNome(String nome){
        String oldNome = this.nome;
        this.nome = nome;
        categorias.put(nome, categorias.remove(oldNome));

    }
    public String getNome(){return nome;}


}
