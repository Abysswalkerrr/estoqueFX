package com.estoquefx;

import javafx.application.Application;

public class Launcher {
    static void main(String[] args) {
        try{
        Application.launch(EstoqueAppFX.class, args);
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
