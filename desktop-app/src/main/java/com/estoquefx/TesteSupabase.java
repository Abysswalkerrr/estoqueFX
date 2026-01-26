package com.estoquefx;

import com.estoquefx.service.SupabaseService;

public class TesteSupabase {
    public static void main(String[] args) {
        try {
            SupabaseService service = new SupabaseService();

            // Teste 1: Login
            System.out.println("Testando login...");
            boolean loginOk = service.login("email@teste.com", "senha123");

            if (loginOk) {
                System.out.println("✓ Login OK!");

                // Teste 2: Criar estoque
                System.out.println("\nCriando estoque...");
                String estoqueId = service.criarEstoque("Meu Primeiro Estoque");
                System.out.println("✓ Estoque criado! ID: " + estoqueId);

                // Teste 3: Listar estoques
                System.out.println("\nListando estoques...");
                service.listarEstoques().forEach(e -> {
                    System.out.println("- " + e.get("nome").getAsString() +
                            " (ID: " + e.get("id").getAsString() + ")");
                });

            } else {
                System.err.println("✗ Falha no login!");
            }

        } catch (Exception e) {
            System.err.println("✗ Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
