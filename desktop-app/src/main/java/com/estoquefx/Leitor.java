package com.estoquefx;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import javax.swing.filechooser.FileSystemView;

public class Leitor {
    static String nomeArquivo = "estoque.txt";
    static String nomePasta = "Sistema estoque";
    static String nomeMisc = "misc.txt";
    static File pastaDocs = FileSystemView.getFileSystemView().getDefaultDirectory();

    public static ArrayList<Produto> carregarEstoque() throws IOException {
        ArrayList<Produto> estoque = new ArrayList<>();
        File pastaApp = new File(pastaDocs, nomePasta);
        File arquivo = new File(pastaApp, nomeArquivo);
        File misc = new File(pastaApp, nomeMisc);

        if (arquivo.exists()) {
            Scanner sc = new Scanner(arquivo);

            while (sc.hasNextLine()) {
                String linha = sc.nextLine().trim();

                if (linha.isEmpty()) continue;

                String[] partes = linha.split("\\|");
                if (partes.length >= 6) {
                    Produto p = infoProduto(partes);
                    estoque.add(p);
                }

            }

            sc.close();
            System.out.println("Estoque carregado: " + estoque.size() + " produtos.");
        } else{
            System.out.println("Nenhum estoque foi encontrado.");
        }
        if (misc.exists()) {
            Scanner scm = new Scanner(misc);
            if (scm.hasNextLine()) {
                String linha2 = scm.nextLine();
                Misc.setUltimaAtualizacao(linha2);
            }
            if (scm.hasNextLine()) {
                if (scm.nextLine().equals("true")) {
                    Misc.setNegouAtualizacao(true);
                }
            }
        } else{
            Misc.setUltimaAtualizacao("");
            Misc.setNegouAtualizacao(false);
        }
        return estoque;
    }

    private static Produto infoProduto(String[] partes) {
        String codigo    = partes[0];
        String nome      = partes[1];
        String categoria = partes[2];
        int vlrMin       = Integer.parseInt(partes[3]);
        double vlrUnd    = Double.parseDouble(partes[4].replace(',', '.'));
        int qtd          = Integer.parseInt(partes[5]);

        String desc = (partes.length >= 7) ? partes[6] : ""; // compatível com versões antigas

        return new Produto(codigo, nome, categoria, vlrMin, vlrUnd, qtd, desc);
    }

    public static void salvarEstoque(ArrayList<Produto> estoque) throws IOException {
        File pastaApp = new File(pastaDocs, nomePasta);
        if (!pastaApp.exists()) {
            pastaApp.mkdirs();
        }
        File arquivo = new File(pastaApp, nomeArquivo);
        File misc = new File(pastaApp, nomeMisc);

        FileWriter fw = new FileWriter(arquivo);
        PrintWriter pw = new PrintWriter(fw);


        for (Produto p : estoque) {
            pw.println(p.toString());
        }

        pw.close();
        fw.close();

        FileWriter fwm = new FileWriter(misc);
        PrintWriter pwm = new PrintWriter(fwm);

        pwm.println(Misc.getUltimaAtualizacao());
        pwm.println(Misc.getNegouAtualizacao());
        pwm.close();
        fwm.close();

    }

    public static String lerUltimaAtt() throws IOException {
        File pastaApp = new File(pastaDocs, nomePasta);
        File arquivo = new File(pastaApp, nomeMisc);
        Scanner sc = new Scanner(arquivo);
        if (sc.hasNextLine()) {
            return sc.nextLine();
        } else {
            return "";
        }
    }

    public static void salvarNA(boolean set) throws IOException{
        String temp = lerUltimaAtt();
        File pastaApp = new File(pastaDocs, nomePasta);
        File arquivo =  new File(pastaApp, nomeMisc);
        FileWriter fw = new FileWriter(arquivo);
        PrintWriter pw = new PrintWriter(fw);
        pw.println(temp);
        pw.println(set);
        pw.close();
    }

    public static void exportarEstoqueCSV(List<Produto> estoque) throws IOException {
        File pastaApp = new File(pastaDocs, nomePasta);
        if (!pastaApp.exists()) {
            pastaApp.mkdirs();
        }
        File arquivoCSV = new File(pastaApp, "estoqueCSV.csv");

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(arquivoCSV),
                        java.nio.charset.StandardCharsets.UTF_8))) {

            // Cabeçalho
            pw.println("Código;Nome;Categoria;Qtd Mínima;Valor Und;Qtd;Urgência;Saldo;Descrição");

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
                String descricao = p.getDescricao();
                totalSaldo += saldo;

                pw.println(
                                codigo + ";" +
                                nome + ";" +
                                categoria + ";" +
                                qtdMin + ";" +
                                vlrUnd + ";" +
                                qtd + ";" +
                                urgencia + ";" +
                                saldo + ";" +
                                descricao
                );
            }

            pw.println();
            pw.println(";;;;;;;;");
            pw.println(";;;;;;;Saldo total;" + totalSaldo);
        }
    }

    public static String getPath(){
        File pastaApp = new File(pastaDocs, nomePasta);
        return pastaApp.getAbsolutePath();
    }



}
