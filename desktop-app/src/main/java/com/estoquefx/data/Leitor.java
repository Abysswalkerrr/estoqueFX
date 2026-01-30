package com.estoquefx.data;

import com.estoquefx.controller.EstoqueController;
import com.estoquefx.model.Produto;
import com.estoquefx.service.ProdutoService;
import com.estoquefx.util.Misc;
import com.estoquefx.util.Time;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Scanner;
import javax.swing.filechooser.FileSystemView;

public class Leitor {
    static String nomeArquivo = "estoque.txt";
    public static String nomePasta = "Sistema estoque";
    static String nomeMisc = "misc.txt";
    public static File pastaDocs = FileSystemView.getFileSystemView().getDefaultDirectory();
    private static String nomeEstoque;

    public static void setNomeEstoque(String nomeEstoque) {
        Leitor.nomeEstoque = nomeEstoque;
    }
    public static String getNomeEstoque() {
        return nomeEstoque;
    }

    public static LinkedHashSet<Produto> carregarEstoque() throws IOException {
        LinkedHashSet<Produto> estoque = new LinkedHashSet<>();
        File arquivo = criaArquivos()[0];
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
        } else {
            System.out.println("Nenhum estoque foi encontrado. Criando novo...");
        }

        return estoque;
    }

    public static void carregarMisc() throws IOException {
        File arquivoMisc = criaArquivos()[1];

        if (arquivoMisc.exists()) {
            Scanner scm = new Scanner(arquivoMisc);
            if (scm.hasNextLine()) {
                String linha2 = scm.nextLine();
                Misc.setUltimaAtualizacaoSemSalvar(linha2);
            }
            if (scm.hasNextLine()) {
                String negou = scm.nextLine();
                if ("true".equals(negou)) {
                    Misc.setNegouAtualizacaoSemSalvar(true);
                }
            }
            scm.close();
        } else {
            // Criar arquivo misc.txt com valores padrão
            System.out.println("Arquivo misc.txt não encontrado. Criando com valores padrão...");
            Misc.setUltimaAtualizacaoSemSalvar("");
            Misc.setNegouAtualizacaoSemSalvar(false);
            criarMiscPadrao(arquivoMisc);
        }

    }

    private static void criarMiscPadrao(File misc) throws IOException {
        FileWriter fw = new FileWriter(misc);
        PrintWriter pw = new PrintWriter(fw);
        pw.println(""); // Última atualização vazia
        pw.println("false"); // Não negou atualização
        pw.close();
        fw.close();
    }

    private static Produto infoProduto(String[] partes) {
        String codigo    = String.valueOf(Produto.getProximoCodigo());
        String nome      = partes[1];
        String categoria = partes[2];
        int vlrMin       = Integer.parseInt(partes[3]);
        double vlrUnd    = Double.parseDouble(partes[4].replace(',', '.'));
        int qtd          = Integer.parseInt(partes[5]);

        String desc = (partes.length >= 7) ? partes[6] : ""; // compatível com versões antigas

        String tempo = (partes.length >= 8) ? partes[7] : "";

        return new Produto(codigo, nome, categoria, vlrMin, vlrUnd, qtd, desc,  tempo);
    }

    public static void salvarEstoque(LinkedHashSet<Produto> estoque) throws IOException {
        File[] arquivos = criaArquivos();
        File arquivo = arquivos[0];
        File misc = arquivos[1];
        File pastaApp = new File(pastaDocs, nomePasta);
        if (!pastaApp.exists()) {
            pastaApp.mkdir();
        }
        File miscGeral = new File(pastaApp, nomeMisc);

        try (PrintWriter pw = new PrintWriter(new FileWriter(arquivo))) {
            for (Produto p : estoque) {
                pw.println(p.toString());
            }
        }

        // misc do estoque
        try (PrintWriter pwm = new PrintWriter(new FileWriter(misc))) {
            pwm.println(Misc.getUltimaAtualizacao());
            pwm.println(Misc.getNegouAtualizacao());
        }

        // misc geral
        try (PrintWriter pwc = new PrintWriter(new FileWriter(miscGeral))) {
            pwc.println(Misc.getNegouAtualizacao());
        }
    }

    public static void importarCSV(File arquivo) throws IOException {
        Scanner sc = new Scanner(arquivo);
        while (sc.hasNextLine()) {
            try {
                String linha = sc.nextLine().trim();
                if (linha.isEmpty()) {
                    continue;
                }
                String[] partes = linha.split(";");

                if (partes.length >= 6) {
                    //com codigo
                    if (Misc.isNumeric(partes[0])) {
                        //codigo -> nome -> categoria -> vlrMin -> vlrUnd -> qtd -> desc opt -> tempo opt
                        if (!partes[1].isEmpty() && !partes[2].isEmpty() &&
                                Misc.isNumeric(partes[3]) && Misc.isNumeric(partes[4]) && Misc.isNumeric(partes[5])) {
                            Produto temp = infoProduto(partes);
                            ProdutoService.addEstoque(temp);
                        }
                    }
                    //sem codigo
                    else if (!partes[0].isEmpty() && !partes[1].isEmpty() && Misc.isNumeric(partes[2]) &&
                            Misc.isNumeric(partes[3]) && Misc.isNumeric(partes[4])) {
                        if (partes.length >= 7) {
                            if (partes[6].isEmpty()) {
                                partes[6] = Time.getTime();
                            }
                        }
                        Produto temp = new Produto(String.valueOf(Produto.getProximoCodigo()), partes[0],
                                partes[1], Integer.parseInt(partes[2]), Double.parseDouble(partes[3]),
                                Integer.parseInt(partes[4]), partes[5], partes[6]);
                        ProdutoService.addEstoque(temp);
                    }
                // nome -> categoria -> vlrMin -> vlrUnd -> qtd
                } else if (partes.length == 5) {
                    if (!partes[0].isEmpty() && !partes[1].isEmpty() && Misc.isNumeric(partes[2]) &&
                            Misc.isNumeric(partes[3]) && Misc.isNumeric(partes[4])) {
                        Produto temp = new Produto(String.valueOf(Produto.getProximoCodigo()), partes[0], partes[1],
                                Integer.parseInt(partes[2]), Double.parseDouble(partes[3]),
                                Integer.parseInt(partes[4]), "", Time.getTime());
                        ProdutoService.addEstoque(temp);
                    }
                }
            } catch (Exception e) {
                EstoqueController.mostrarInfoStatic("Erro", "Erro " +  e.getMessage());
            }
        }
    }

    public static String lerUltimaAtt() throws IOException {
        File pastaApp = new File(pastaDocs, nomePasta);
        File pastaEstoque =  new File(pastaApp, nomeEstoque);
        File arquivo = new File(pastaEstoque, nomeMisc);

        if (!arquivo.exists()) {
            return "";
        }

        Scanner sc = new Scanner(arquivo);
        String resultado = "";
        if (sc.hasNextLine()) {
            resultado = sc.nextLine();
        }
        sc.close();
        return resultado;
    }

    public static void salvarNA(boolean set) throws IOException {
        File pastaApp = new File(pastaDocs, nomePasta);
        if (!pastaApp.exists()) {
            pastaApp.mkdirs();
        }

        File miscGeral = new File(pastaApp, nomeMisc);
        try (PrintWriter pwm = new PrintWriter(new FileWriter(miscGeral))) {
            pwm.println(set);
        }

        if (nomeEstoque != null && !nomeEstoque.isEmpty()) {
            try {
                String temp = lerUltimaAtt();
                File arquivo = criaArquivos()[1];

                try (PrintWriter pw = new PrintWriter(new FileWriter(arquivo))) {
                    pw.println(temp);
                    pw.println(set);
                }
            } catch (Exception e) {
                System.err.println("Aviso: Não foi possível salvar no misc.txt do estoque: " + e.getMessage());
            }
        }
    }

    public static File[] criaArquivos(){
        File pastaApp = new File(pastaDocs, nomePasta);
        if (!pastaApp.exists()) {
            pastaApp.mkdirs();
        }
        File  pastaEstoque = new File(pastaApp, nomeEstoque);
        if (!pastaEstoque.exists()) {
            pastaEstoque.mkdirs();
        }
        File arquivo = new File(pastaEstoque, nomeArquivo);
        File arquivoMisc = new File(pastaEstoque, nomeMisc);
        return new File[]{arquivo, arquivoMisc};
    }

    // arquivo é escolhido antes
    public static void exportarEstoqueParaArquivo(LinkedHashSet<Produto> estoque,
                                                  File arquivoCSV) throws IOException {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(arquivoCSV),
                        java.nio.charset.StandardCharsets.UTF_8))) {

            // Cabeçalho
            pw.println("Código;Nome;Categoria;Qtd Min;Valor Und;Qtd;Descrição;Últ alt;Urgência;Saldo");

            double totalSaldo = 0.0;

            for (Produto p : estoque) {
                String codigo    = p.getCodigo();
                String nome      = p.getNome();
                String categoria = p.getCategoria();
                int qtdMin       = p.getVlrMin();
                double vlrUnd    = p.getVlrUnd();
                int qtd          = p.getQtd();
                String urgencia  = p.getCompra() ? "Compra urgente" : "Estoque suficiente";
                String tempo = p.getAlterHora();
                String descricao = p.getDescricao();
                double saldo     = vlrUnd * qtd;
                totalSaldo += saldo;

                pw.println(
                        codigo + ";" +
                                nome + ";" +
                                categoria + ";" +
                                qtdMin + ";" +
                                vlrUnd + ";" +
                                qtd + ";" +
                                descricao + ";" +
                                tempo + ";" +
                                urgencia + ";" +
                                saldo
                );
            }
            pw.println();
            pw.println(";;;;;;;;");
            pw.println(";;;;;;;Saldo total;" + totalSaldo);
        }
    }

    public static void carregarNA() throws IOException {
        File pastaApp = new File(pastaDocs, nomePasta);
        if (!pastaApp.exists()) {
            pastaApp.mkdirs();
        }
        File miscGeral = new File(pastaApp, nomeMisc);

        if (!miscGeral.exists()) {
            Misc.setNegouAtualizacaoSemSalvar(false);
            try (PrintWriter pw = new PrintWriter(new FileWriter(miscGeral))) {
                pw.println("false");
            }
            return;
        }

        try (Scanner sc = new Scanner(miscGeral)) {
            if (sc.hasNextLine()) {
                String linha = sc.nextLine().trim();
                Misc.setNegouAtualizacaoSemSalvar("true".equals(linha));
            } else {
                Misc.setNegouAtualizacaoSemSalvar(false);
            }
        }
    }

    public static String getPath() {
        File pastaApp = new File(pastaDocs, nomePasta);
        File pastaEstoque = new File(pastaApp, nomeEstoque);
        return pastaEstoque.getAbsolutePath();
    }
}
