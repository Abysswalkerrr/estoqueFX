package com.estoquefx;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
            e.printStackTrace();
        }
    }

    public static String getSupabaseUrl() {
        return props.getProperty("supabase.url");
    }

    public static String getSupabaseKey() {
        return props.getProperty("supabase.key");
    }

}
