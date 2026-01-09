package com.estoquefx;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // ✅ Pega raiz do MSI INSTALADO
        String appRoot = new java.io.File(".").getAbsoluteFile().getParent();

        String runtimePath = appRoot + "\\runtime";
        String appPath = appRoot + "\\app";

        System.out.println("App root: " + appRoot);
        System.out.println("Runtime: " + runtimePath);
        System.out.println("App jars: " + appPath);

        // ✅ Valida pastas
        if (!new java.io.File(runtimePath).exists()) {
            System.err.println("❌ runtime não encontrado!");
            System.exit(1);
        }
        if (!new java.io.File(appPath).exists()) {
            System.err.println("❌ app não encontrado!");
            System.exit(1);
        }

        System.setProperty("java.home", runtimePath);
        System.setProperty("java.class.path", appPath + "/*");

        Application.launch(EstoqueAppFX.class, args);
    }
}
