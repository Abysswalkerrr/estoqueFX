package com.estoquefx.service;

import com.estoquefx.controller.EstoqueController;
import com.estoquefx.model.Categoria;
import com.estoquefx.model.Produto;
import com.estoquefx.model.Estoque;
import javafx.scene.control.Alert;

import java.util.*;

public class ProdutoService {

    public static void entrada(int a, String nome) {
        String codigo = Produto.getCodigoPorNome(nome);
        if (codigo != null) {
            Produto p = Produto.mapaCodigo.get(codigo);
            if (p != null) {
                p.setQtd(p.getQtd() + a);
                p.atualizaCompra();
                Produto.setUltimaAcao("e");
            }
        }
    }

    public static void saida(int a, String nome) {
        String codigo = Produto.getCodigoPorNome(nome);
        if (codigo != null) {
            Produto p = Produto.mapaCodigo.get(codigo);
            if (p != null) {
                if (p.getQtd() >= a) {
                    p.setQtd(p.getQtd() - a);
                    p.atualizaCompra();
                    Produto.setUltimaAcao("e");
                }
            }
        }
    }

    public static void addEstoque(Produto prod) {

        if (verificarUnico(prod)) {
            // adiciona normal
            Estoque.addProduto(prod);
            Produto.mapaCodigo.put(prod.getCodigo(), prod);
            Estoque.addNome(prod.getNome());
            Produto.setUltimaAcao("c");
        } else {
            try {
                // sobrescreve
                Produto existente = Produto.getProdutoPorCodigo(Produto.getCodigoPorNome(prod.getNome()));
                assert existente != null;
                existente.setQtd(prod.getQtd());
                existente.setVlrMin(prod.getVlrMin());
                existente.setVlrUnd(prod.getVlrUnd());
                existente.atualizaCompra();
            } catch (Exception ex) {
                EstoqueController.mostrarInfoStatic(Alert.AlertType.ERROR, "Erro", null, "Erro importar " + prod.getNome() + ": " + ex.getMessage());
            }
            Produto.setUltimaAcao("c");
        }
    }

    public static boolean verificarUnico(Produto p){
        for (Produto produto : Estoque.getProdutos()) {
            if (produto.getNome().equals(p.getNome())){
                return false;
            }
        }
        return true;
    }

    public static void preencher(LinkedHashSet<Produto> lista) {
        if (!lista.isEmpty()) {
            for (Produto p : lista) {
                Estoque.addProduto(p);
                Produto.mapaCodigo.put(p.getCodigo(), p);

                String cat = p.getCategoria();
                Categoria.addCategoria(cat);
                Categoria.addProduto(cat, p);
            }

        }
    }





}
