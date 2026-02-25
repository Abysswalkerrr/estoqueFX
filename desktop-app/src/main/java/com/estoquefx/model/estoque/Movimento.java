package com.estoquefx.model.estoque;

import com.estoquefx.util.I18n;
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
    private String qtdVelhaMostrar;
    private String qtdNovaMostrar;

    // qtdMin/qtd
    public Movimento(Produto produto, String tipo, int diff){
        this.codigo = produto.getCodigo();
        this.nome = produto.getNome();
        this.quantidadeNova = tipo.equals("AJUSTE") ? produto.getVlrMin() : produto.getQtd();
        this.tempo = Time.getTime(true);
        this.tipo = tipo;
        this.diff = diff;
        this.velhaQuantidade = quantidadeNova - diff;
        this.qtdVelhaMostrar = String.valueOf(velhaQuantidade);
        this.qtdNovaMostrar = String.valueOf(quantidadeNova);
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
        this.qtdVelhaMostrar = String.valueOf(velhaQuantidade);
        this.qtdNovaMostrar = String.valueOf(quantidadeNova);
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
        this.quantidadeNova = produto.getQtd();
        this.velhaQuantidade = produto.getQtd();
        this.diff = 0;
        this.qtdVelhaMostrar = String.format("R$ %.2f", velhoValor);
        this.qtdNovaMostrar = String.format("R$ %.2f", valorNovo);
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
        this.qtdVelhaMostrar = String.format("R$ %.2f", velhoValor);
        this.qtdNovaMostrar = String.format("R$ %.2f", valorNovo);

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
    public int getQuantidadeAnterior() {return velhaQuantidade;}
    public double getVelhoValor() {return velhoValor;}

    public String getTempoFormatado(){
        return Time.getTempoFormatado(tempo);
    }

    public String getDiferencaFormatada() {
        String toReturn = "";
        if (tipo.equals("ENTRADA") || tipo.equals("SAIDA") || tipo.equals("AJUSTE")) {
            if (diff > 0) {
                toReturn = "+" + diff;
            } else{
                toReturn = String.valueOf(diff);
            }
        } else if (tipo.equals("ALTERACAO_VALOR")) {
            if (delta > 0) {
                toReturn = ("R$ +" + delta).replace(".", ",");
            } else{
                toReturn = ("R$ " + delta).replace(".", ",");
            }
        }
        return toReturn;
    }

    public String getQtdVelhaMostrar() {return qtdVelhaMostrar;}
    public String getQtdNovaMostrar() {return qtdNovaMostrar;}

    public String getTipoDescricao() {
        return switch (tipo.toUpperCase()) {
            case "ENTRADA"          -> I18n.t("historico.tipo.entrada");
            case "SAIDA"            -> I18n.t("historico.tipo.saida");
            case "AJUSTE"           -> I18n.t("historico.tipo.ajuste");
            case "CRIACAO"          -> I18n.t("historico.tipo.criacao");
            case "ALTERACAO_VALOR"  -> I18n.t("historico.tipo.alteracao_valor");
            case "ALTERACAO_DADOS"  -> I18n.t("historico.tipo.alteracao_dados");
            case "REMOCAO"          -> I18n.t("historico.tipo.remocao");
            default                 -> tipo;
        };
    }
}
