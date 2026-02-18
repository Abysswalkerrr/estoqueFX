package com.estoquefx.service.estoque;

import com.estoquefx.model.estoque.Categoria;
import com.estoquefx.model.estoque.Estoque;
import com.estoquefx.model.estoque.Produto;

public class EstoqueService {

    public static void carregaCategorias(){
        for (Produto p : Estoque.getProdutos()) {
            Categoria.addCategoria(p.getCategoria());
        }
    }

    public static void carregaNomes(){
        for (Produto p : Estoque.getProdutos()) {
            Estoque.addNome(p.getNome());
        }
    }

    public static void atualizaTotal() {
        double total = 0;
        for (Produto p : Estoque.getProdutos()) {
            total += (p.getQtd() * p.getVlrUnd());
        }
        Estoque.setSaldo(total);
    }

}
