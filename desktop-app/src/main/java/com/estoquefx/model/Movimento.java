package com.estoquefx.model;

import com.estoquefx.util.Time;

import java.time.LocalDateTime;

public class Movimento {
    private final String codigo;
    private final String nome;
    private final LocalDateTime tempo;
    private final String tipo;
    private double valorNovo;
    private double delta;
    private double velhoValor;
    private int quantidadeNova;
    private int diff;
    private int velhaQuantidade;

    // qtdMin/qtd
    public Movimento(Produto produto, String tipo, int diff){
        this.codigo = produto.getCodigo();
        this.nome = produto.getNome();
        this.quantidadeNova = produto.getQtd();
        this.tempo = Time.getTime(true);
        this.tipo = tipo;
        this.diff = diff;
        this.velhaQuantidade = quantidadeNova - diff;
        Historico.addMovimento(this);
    }
    public Movimento(String codigo, String nome, LocalDateTime tempo, String tipo,
                     int quantidadeNova, int diff, int velhaQuantidade){
        this.codigo = codigo;
        this.nome = nome;
        this.tempo = tempo;
        this.tipo = tipo;
        this.quantidadeNova = quantidadeNova;
        this.diff = diff;
        this.velhaQuantidade = velhaQuantidade;
        Historico.addMovimento(this);
    }

    // vlrUnd
    public Movimento(Produto produto, String tipo, double delta){
        this.codigo = produto.getCodigo();
        this.nome = produto.getNome();
        this.tempo = Time.getTime(true);
        this.tipo = tipo;
        this.delta = delta;
        this.valorNovo = produto.getVlrUnd();
        this.velhoValor = valorNovo - delta;
        Historico.addMovimento(this);
    }
    public Movimento(String codigo, String nome, LocalDateTime tempo, String tipo,
                     double delta, double valorNovo, double velhoValor){
        this.codigo = codigo;
        this.nome = nome;
        this.tempo = tempo;
        this.tipo = tipo;
        this.delta = delta;
        this.valorNovo = valorNovo;
        this.velhoValor = velhoValor;
        Historico.addMovimento(this);
    }

    // produto criado
    public Movimento(Produto produto, String tipo){
        this.codigo = produto.getCodigo();
        this.nome = produto.getNome();
        this.tempo = Time.getTime(true);
        this.tipo = tipo;
    }
    public Movimento(String codigo, String nome, LocalDateTime tempo, String tipo){
        this.codigo = codigo;
        this.nome = nome;
        this.tempo = tempo;
        this.tipo = tipo;
        Historico.addMovimento(this);
    }

    public String getCodigo() {return codigo;}
    public String getNome() {return nome;}
    public LocalDateTime getTempo() {return tempo;}
    public String getTipo() {return tipo;}
    public double getValorNovo() {return valorNovo;}
    public double getDelta() {return delta;}
    public int getQuantidadeNova() {return quantidadeNova;}
    public int getDiff() {return diff;}
    public int getVelhaQuantidade() {return velhaQuantidade;}
    public double getVelhoValor() {return velhoValor;}

    public String getDiferencaFormatada() {
        if (diff > 0) {
            return "+" + diff;
        }
        return String.valueOf(diff);
    }

    public String getTipoDescricao() {
        switch (tipo.toUpperCase()) {
            case "ENTRADA": return "Entrada";
            case "SAIDA": return "Saída";
            case "AJUSTE": return "Ajuste";
            case "CRIACAO": return "Criação";
            case "ALTERACAO_VALOR": return "Alteração Valor";
            case "ALTERACAO_DADOS": return "Alteração Dados";
            default: return tipo;
        }
    }

}