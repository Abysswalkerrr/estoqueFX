package com.estoquefx;

import javafx.application.Application;

public class Launcher {
     static void main(String[] args) {
         try {
             Runtime.getRuntime().exec(
                     "java --module-path updater-app/target/updater-app-1.0.0.jar --add-modules com.estoquefx.updaterapp -m com.estoquefx.updaterapp/com.estoquefx.updater.UpdaterApp"
             );
         } catch (java.io.IOException e) {
             e.printStackTrace();
         }
         Application.launch(com.estoquefx.EstoqueAppFX.class, args);
    }
}
