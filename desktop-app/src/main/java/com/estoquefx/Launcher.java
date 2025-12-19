package com.estoquefx;

import javafx.application.Application;

public class Launcher {
     static void main(String[] args) {
         try {
             Runtime.getRuntime().exec(
                     "java -cp updater-app/target/updater-app-1.0.0.jar com.estoquefx.updater.UpdaterApp"
             );
         } catch (java.io.IOException e) {
             e.printStackTrace();
         }
         Application.launch(com.estoquefx.EstoqueAppFX.class, args);
    }
}
