package com.estoquefx.service.estoque;

import com.estoquefx.model.estoque.Categoria;

import java.util.HashMap;
import java.util.Map;

public class CategoriaService {
    public static Map<String, Double> calcularValorPorCategoria(){
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Categoria> entry : Categoria.categorias.entrySet()) {
            Categoria categoria = entry.getValue();
            result.put(categoria.getNome(), categoria.getValor());
        }
        return result;
    }

}
