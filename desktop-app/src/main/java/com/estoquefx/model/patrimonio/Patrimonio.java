package com.estoquefx.model.patrimonio;


import java.util.ArrayList;
import java.util.List;

public class Patrimonio {
    private String codigo;
    private String nome;
    private String descricao;
    private String localizacao;
    private String estado; // BOM, REGULAR, RUIM
    private String alterHora;

    private static int proximoCodigo = 1;
    private static final List<Patrimonio> patrimonios = new ArrayList<>();

    public Patrimonio(String nome, String descricao, String localizacao, String estado, String alterHora) {
        this.codigo = String.valueOf(proximoCodigo++);
        this.nome = nome;
        this.descricao = descricao;
        this.localizacao = localizacao;
        this.estado = estado;
        this.alterHora = alterHora;
    }

    // Construtor para carga de arquivo
    public Patrimonio(String codigo, String nome, String descricao,
                      String localizacao, String estado, String alterHora) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.localizacao = localizacao;
        this.estado = estado;
        this.alterHora = alterHora;

        int num = Integer.parseInt(codigo);
        if (num >= proximoCodigo) proximoCodigo = num + 1;
    }

    public static void addPatrimonio(Patrimonio p) { patrimonios.add(p); }
    public static List<Patrimonio> getPatrimonios() { return patrimonios; }
    public static void removePatrimonio(Patrimonio p) { patrimonios.remove(p); }

    public String getCodigo()      { return codigo; }
    public String getNome()        { return nome; }
    public String getDescricao()   { return descricao; }
    public String getLocalizacao() { return localizacao; }
    public String getEstado()      { return estado; }
    public String getAlterHora()   { return alterHora; }

    public void setNome(String nome)               { this.nome = nome; }
    public void setDescricao(String descricao)     { this.descricao = descricao; }
    public void setLocalizacao(String loc)         { this.localizacao = loc; }
    public void setEstado(String estado)           { this.estado = estado; }
    public void setAlterHora(String alterHora)     { this.alterHora = alterHora; }

    @Override
    public String toString() {
        return codigo + "|" + nome + "|" + descricao + "|"
                + localizacao + "|" + estado + "|" + alterHora;
    }
}