package com.estoquefx.util;

import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.estoquefx.controller.MainController.mostrarInfoStatic;

public class SupabaseConfig {
    private static final Properties props = new Properties();
    static {
        try (InputStream input = SupabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                System.err.println("ERRO: config.properties não encontrado!");
            }

            props.load(input);

        } catch (IOException e) {
            mostrarInfoStatic(Alert.AlertType.ERROR, "Erro",
                    "Erro ao carregar configurações.", e.getMessage());
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    public static String getSupabaseUrl() {
        return props.getProperty("supabase.url");
    }

    public static String getSupabaseKey() {
        return props.getProperty("supabase.key");
    }

}
