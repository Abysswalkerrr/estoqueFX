package com.estoquefx.model;

import java.util.HashMap;
import java.util.Map;

public class Produto {
    private String nome;
    private final String codigo;
    private int vlrMin;
    private int qtd;
    private double vlrUnd;
    private String categoria;
    private boolean compraUrgente;
    private static String ultimaAcao = "i";
    private String descricao;
    private String alterHora;

    private static int proximoCodigo = 1;
    public static Map<String, Produto> mapaCodigo = new HashMap<>();

    // Gera código
    public Produto(String nome, int vlrMin, double vlrUnd, int qtd, String categoria, String alterHora) {
        this.nome = nome;
        this.categoria = categoria;
        this.codigo = String.valueOf(proximoCodigo++);
        this.vlrMin = vlrMin;
        this.qtd = qtd;
        this.vlrUnd = vlrUnd;
        compraUrgente = vlrMin > qtd;
        descricao = "";
        this.alterHora = alterHora;
    }

    // Construtor para produtos carregados do arquivo (código já existe) - talvez obsoleto
    public Produto(String codigo, String nome, String categoria, int vlrMin, double vlrUnd, int qtd,
                   String descricao, String alterHora) {
        this.codigo = codigo;
        this.nome = nome;
        this.categoria = categoria;
        this.vlrMin = vlrMin;
        this.qtd = qtd;
        this.vlrUnd = vlrUnd;
        compraUrgente = vlrMin > qtd;
        this.descricao = descricao;
        this.alterHora = alterHora;

        // Atualiza o próximo código se necessário
        int codigoNum = Integer.parseInt(codigo);
        if (codigoNum >= proximoCodigo) {
            proximoCodigo = codigoNum + 1;
        }
    }

    //não lembro onde é usado, mas tá azul então vai ficar loll
    public Produto(String codigo, String nome, String categoria, int vlrMin, double vlrUnd, int qtd,
                   String alterHora) {
        this.codigo = codigo;
        this.nome = nome;
        this.categoria = categoria;
        this.vlrMin = vlrMin;
        this.qtd = qtd;
        this.vlrUnd = vlrUnd;
        compraUrgente = vlrMin > qtd;
        this.descricao = "";
        this.alterHora = alterHora;


        // Atualiza o próximo código se necessário
        int codigoNum = Integer.parseInt(codigo);
        if (codigoNum >= proximoCodigo) {
            proximoCodigo = codigoNum + 1;
        }
    }



    public static void setUltimaAcao(String lastAction) {ultimaAcao = lastAction;}

    public void setNome(String nome) {
        Estoque.removeNome(this.nome);
        Estoque.addNome(nome);
        this.nome = nome;
        setUltimaAcao("t");
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
        setUltimaAcao("t");
    }

    public void setVlrMin(int vlrMin) {
        this.vlrMin = vlrMin;
        setUltimaAcao("t");
    }

    public void setVlrUnd(double vlrUnd) {
        this.vlrUnd = vlrUnd;
        setUltimaAcao("t");
    }

    public void setQtd(int qtd) {
        this.qtd = qtd;
        setUltimaAcao("t");
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
        setUltimaAcao("t");
    }

    public void setAlterHora(String alterHora) {
        this.alterHora = alterHora;
    }

    public String getAlterHora() {return alterHora;}

    public static String getUltimaAcao(){return  ultimaAcao;}

    public String getCodigo() {return codigo;}

    public String getNome() {return nome;}

    public String getCategoria() {return categoria;}

    public int getVlrMin() {return vlrMin;}

    public double getVlrUnd() {return vlrUnd;}

    public int getQtd() {return qtd;}

    public boolean getCompra(){return compraUrgente;}

    public String getDescricao() {return  descricao;}

    public static Produto getProdutoPorCodigo(String codigo) {
        for (Produto p : Estoque.getProdutos()) {
            if (p.getCodigo().equals(codigo)) {
                return p;
            }
        }
        return null;
    }

    public static int getProximoCodigo(){
        return proximoCodigo++;
    }

    @Override
    public String toString() {
        return codigo + "|" + nome + "|" + categoria + "|" + vlrMin + "|" + vlrUnd + "|" + qtd + "|" + descricao + "|" + alterHora;
    }

    public static String getCodigoPorNome(String n) {
        if (n == null) return null;
        String buscado = n.trim().toUpperCase();

        for (Produto produto : Estoque.getProdutos()) {
            String nomeProd = produto.getNome();
            if (nomeProd != null && nomeProd.trim().toUpperCase().equals(buscado)) {
                return produto.codigo;
            }
        }
        return null;
    }

    public void atualizaCompra(){
        compraUrgente = qtd < vlrMin;
    }

}