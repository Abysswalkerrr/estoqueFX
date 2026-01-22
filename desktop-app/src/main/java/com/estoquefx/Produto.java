package com.estoquefx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
    private static Map<String, Produto> mapaCodigo = new HashMap<>();

    // Construtor para novos produtos (gera código)
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

    // Construtor para produtos carregados do arquivo (código já existe)
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


    public static void entrada(int a, String nome) {
        String codigo = getCodigoPorNome(nome);
        if (codigo == null) {
            System.out.println("Produto não encontrado para entrada: " + nome);
            return;
        }
        Produto p = mapaCodigo.get(codigo);
        if (p == null) {
            System.out.println("Produto não encontrado no mapa: " + codigo);
            return;
        }
        p.qtd += a;
        atualizaCompra(p);
        setUltimaAcao("e");
    }

    public static void saida(int a, String nome) {
        String codigo = getCodigoPorNome(nome);
        if (codigo == null) {
            System.out.println("Produto não encontrado para saída: " + nome);
            return;
        }
        Produto p = mapaCodigo.get(codigo);
        if (p == null) {
            System.out.println("Produto não encontrado no mapa: " + codigo);
            return;
        }
        if (p.qtd >= a) {
            p.qtd -= a;
            atualizaCompra(p);
            setUltimaAcao("e");
        } else {
            System.out.println("Estoque insuficiente! Último estoque: " + p.qtd);
        }
    }

    public static void addEstoque(Produto prod) {
        if (verificarUnico(prod)) {
            Estoque.addProduto(prod);
            mapaCodigo.put(prod.codigo, prod);
            setUltimaAcao("c");
        } else{
            try {
                getProdutoPorCodigo(getCodigoPorNome(prod.nome)).setQtd(prod.qtd);
                getProdutoPorCodigo(getCodigoPorNome(prod.nome)).setVlrMin(prod.vlrMin);
                getProdutoPorCodigo(getCodigoPorNome(prod.nome)).setVlrUnd(prod.vlrUnd);

                atualizaCompra(getProdutoPorCodigo(getCodigoPorNome(prod.nome)));

            } catch (Exception ex) {
                EstoqueController.mostrarInfoStatic("Erro", "Erro importar " + prod.nome + ": " + ex.getMessage());
            }
            setUltimaAcao("c");
        }
    }

    public static boolean verificarUnico(Produto p){
        for (Produto produto : Estoque.getProdutos()) {
            if (produto.nome.equals(p.nome)){
                return false;
            }
        }
        return true;
    }

    public static void setVlrMin(int a, String codigo) {
        Produto p = mapaCodigo.get(codigo);
        p.vlrMin = a;
        setUltimaAcao("t");
    }

    public static void setVlrUnd(double a, String codigo) {
        Produto p = mapaCodigo.get(codigo);
        p.vlrUnd = a;
        Misc.atualizaTotal();
        setUltimaAcao("t");
    }

    public static void setQtd(int a, String codigo) {
        Produto p = mapaCodigo.get(codigo);
        p.qtd = a;
        Misc.atualizaTotal();
        setUltimaAcao("t");
    }

    public static void setNome(String nome,  String codigo) {
        Produto p = mapaCodigo.get(codigo);
        Misc.removeNome(p.nome);
        p.nome = nome;
        Misc.addNome(p.nome);
        setUltimaAcao("t");
    }

    public static void setCategoria(String categoria, String codigo) {
        Produto p = mapaCodigo.get(codigo);
        p.categoria = categoria;
        setUltimaAcao("t");
    }

    public static void setUltimaAcao(String lastAction) {ultimaAcao = lastAction;}

    public void setNome(String nome) {
        Misc.removeNome(this.nome);
        Misc.addNome(nome);
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

    public static void preencher(LinkedHashSet<Produto> lista) {
        if (!lista.isEmpty()) {
            for (Produto p : lista) {
                Estoque.addProduto(p);
                mapaCodigo.put(p.codigo, p);
            }
        }
    }

    public static void atualizaCompra(Produto p){
        p.compraUrgente = p.qtd < p.vlrMin;
    }

}