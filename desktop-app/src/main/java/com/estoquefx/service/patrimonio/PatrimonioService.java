package com.estoquefx.service.patrimonio;

import com.estoquefx.model.patrimonio.Patrimonio;

public class PatrimonioService {
    public static void addP(int codigo, String nome) {
        Patrimonio p = new Patrimonio(String.valueOf(codigo), nome);
        p.addPatrimonio(p);
    }
}
