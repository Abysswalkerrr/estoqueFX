package br.com.SistemaEstoqueFX;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import javax.swing.filechooser.FileSystemView;

public class Leitor {
    public static String nomeArquivo = "estoque.txt";
    static String nomePasta = "Sistema estoque";
    static File pastaDocs = FileSystemView.getFileSystemView().getDefaultDirectory();


    public static ArrayList<Produto> carregarEstoque() throws IOException {
        ArrayList<Produto> estoque = new ArrayList<>();
        File pastaApp = new File(pastaDocs, nomePasta);
        File arquivo = new File(pastaApp, nomeArquivo);

        if (!arquivo.exists()) {
            System.out.println("Arquivo não encontrado. Iniciando com estoque vazio.");
            return estoque;
        }

        Scanner sc = new Scanner(arquivo);
        int linhaNum = 0;

        while (sc.hasNextLine()) {
            linhaNum++;
            String linha = sc.nextLine().trim();

            if (linha.isEmpty()) continue;

            String[] campos = linha.split("\\|");

            if (campos.length == 6) {
                try {
                    Produto p = new Produto(
                            campos[0].trim(),
                            campos[1].trim(),
                            campos[2].trim(),
                            Integer.parseInt(campos[3].trim()),
                            Double.parseDouble(campos[4].trim()),
                            Integer.parseInt(campos[5].trim())

                    );
                    estoque.add(p);
                } catch (NumberFormatException e) {
                    System.out.println("Erro ao processar linha " + linhaNum + ": " + linha);
                }
            } else {
                System.out.println("Linha " + linhaNum + " com formato inválido: " + linha);
            }
        }

        sc.close();
        System.out.println("Estoque carregado: " + estoque.size() + " produtos.");
        return estoque;
    }

    public static void salvarEstoque(ArrayList<Produto> estoque) throws IOException {
        File pastaApp = new File(pastaDocs, nomePasta);
        if (!pastaApp.exists()) {
            pastaApp.mkdirs();
        }
        File arquivo = new File(pastaApp, nomeArquivo);
        FileWriter fw = new FileWriter(arquivo);
        PrintWriter pw = new PrintWriter(fw);

        for (Produto p : estoque) {
            pw.println(p.toString());
        }

        pw.close();
    }

    public static void exportarEstoqueCSV(java.util.List<Produto> estoque) throws IOException {
        File pastaApp = new File(pastaDocs, nomePasta);
        if (!pastaApp.exists()) {
            pastaApp.mkdirs();
        }
        File arquivoCSV = new File(pastaApp, "estoque.csv");

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(arquivoCSV),
                        java.nio.charset.StandardCharsets.UTF_8))) {

            // Cabeçalho
            pw.println("Código;Nome;Categoria;Qtd Mínima;Valor Und;Qtd;Urgência;Saldo");

            double totalSaldo = 0.0;

            for (Produto p : estoque) {
                String codigo    = p.getCodigo();
                String nome      = p.getNome();
                String categoria = p.getCategoria();
                int qtdMin       = p.getVlrMin();
                double vlrUnd    = p.getVlrUnd();
                int qtd          = p.getQtd();
                String urgencia  = p.getCompra() ? "Compra urgente" : "Estoque suficiente";
                double saldo     = vlrUnd * qtd;
                totalSaldo += saldo;

                pw.println(
                        codigo + ";" +
                                nome + ";" +
                                categoria + ";" +
                                qtdMin + ";" +
                                vlrUnd + ";" +
                                qtd + ";" +
                                urgencia + ";" +
                                saldo
                );
            }

            pw.println();
            pw.println(";;;;;;;;");
            pw.println(";;;;;;;Saldo total;" + totalSaldo);
        }
    }



}
