package com.estoquefx.model.patrimonio;

import java.util.HashSet;

public class Patrimonio {
    int codigo;
    String nome;
    String marca;
    String dataCompra;
    String fornecedor;
    double valorCompra;
    String localizacao;
    String status;
    String lastManutencao;
    String garantia;
    String obs;

    private static HashSet<Patrimonio> patrimonios = new HashSet<>();

    public Patrimonio(String codigo, String nome) {
        this.codigo = Integer.parseInt(codigo);
        this.nome = nome;
    }
    public void addPatrimonio(Patrimonio p) {
        patrimonios.add(p);
    }
    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public void  setMarca(String marca) {
        this.marca = marca;
    }
    public void setDataCompra(String dataCompra) {
        this.dataCompra = dataCompra;
    }
    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }
    public void setValorCompra(double valorCompra) {
        this.valorCompra = valorCompra;
    }
    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setLastManutencao(String lastManutencao) {
        this.lastManutencao = lastManutencao;
    }
    public void setGarantia(String garantia) {
        this.garantia = garantia;
    }
    public void setObs(String obs) {
        this.obs = obs;
    }
    public int getCodigo() {
        return codigo;
    }
    public String getNome() {
        return nome;
    }
    public String getMarca() {
        return marca;
    }
    public String getDataCompra() {
        return dataCompra;
    }
    public String getFornecedor() {
        return fornecedor;
    }
    public double getValorCompra() {
        return valorCompra;
    }
    public String getLocalizacao() {
        return localizacao;
    }
    public String getStatus() {
        return status;
    }
    public String getLastManutencao() {
        return lastManutencao;
    }
    public String getGarantia() {
        return garantia;
    }
    public String getObs() {
        return obs;
    }

    public static HashSet<Patrimonio> getPatrimonios() {
        return patrimonios;
    }

}
