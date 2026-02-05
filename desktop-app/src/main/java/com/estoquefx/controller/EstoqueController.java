package com.estoquefx.controller;

import com.estoquefx.EstoqueAppFX;
import com.estoquefx.data.Leitor;
import com.estoquefx.model.*;
import com.estoquefx.service.*;
import com.estoquefx.updater.core.*;
import com.estoquefx.util.Misc;
import com.estoquefx.util.Time;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.*;
import java.util.function.LongConsumer;

/**
 * Controller principal do sistema de estoque
 * Coordena os sub-controllers (Tabela, Dashboard, Histórico)
 * Gerencia ações do menu e navegação
 */
public class EstoqueController {

    // ========== SUB-CONTROLLERS ==========

    @FXML private Tab tabTabela;
    private TabelaController tabelaController;

    @FXML private Tab tabHistorico;
    private HistoricoController historicoController;

    @FXML private Tab tabDashboard;
    private DashboardController dashboardController;

    // ========== VARIÁVEIS DE ESTADO ==========

    private SupabaseService supabaseService;
    private String estoqueId;
    private static Stage stage;

    // ========== INICIALIZAÇÃO ==========

    @FXML
    public void initialize() {
        System.out.println("🎬 Inicializando EstoqueController principal...");

        // Carregar sub-controllers
        carregarTabelaController();
        carregarHistoricoController();
        carregarDashboardController();

        // Conectar controllers
        conectarControllers();

        Platform.runLater(() -> {
            Produto.setUltimaAcao("s");
        });
    }

    // ========== CARREGAMENTO DE SUB-CONTROLLERS ==========

    private void carregarTabelaController() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/estoquefx/tabela-view.fxml")
            );
            VBox tabelaView = loader.load();
            tabelaController = loader.getController();
            tabTabela.setContent(tabelaView);

            System.out.println("✓ TabelaController carregado");
        } catch (Exception e) {
            System.err.println("⚠ Erro ao carregar TabelaController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarHistoricoController() {
        try {
            Object includeContent = tabHistorico.getContent();

            if (includeContent != null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/estoquefx/historico-view.fxml")
                );
                VBox historicoView = loader.load();
                historicoController = loader.getController();
                tabHistorico.setContent(historicoView);

                System.out.println("✓ HistoricoController carregado");
            }
        } catch (Exception e) {
            System.err.println("⚠ Erro ao carregar HistoricoController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarDashboardController() {
        try {
            Object includeContent = tabDashboard.getContent();
            if (includeContent != null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/estoquefx/dashboard-view.fxml")
                );
                VBox dashboardView = loader.load();
                dashboardController = loader.getController();
                tabDashboard.setContent(dashboardView);

                System.out.println("✓ DashboardController carregado");
            }
        } catch (Exception e) {
            System.err.println("⚠ Erro ao carregar DashboardController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== CONEXÃO ENTRE CONTROLLERS ==========

    private void conectarControllers() {
        // Tabela → Histórico
        if (tabelaController != null && historicoController != null) {
            tabelaController.setHistoricoController(historicoController);
        }

        // Tabela → Última alteração
        if (tabelaController != null) {
            tabelaController.setOnUltimaAlteracaoChanged(this::atualizarUltimaAlteracao);
        }

        System.out.println("✓ Controllers conectados");
    }

    // ========== CONFIGURAÇÃO DE ESTOQUE ==========

    public void setEstoqueAtual(String estoqueId, String estoqueNome, SupabaseService service) {
        this.estoqueId = estoqueId;
        this.supabaseService = service;
        Leitor.setNomeEstoque(estoqueNome);

        try {
            Leitor.carregarMisc();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inicializar sub-controllers ANTES de atualizar tabela
        if (historicoController != null) {
            historicoController.setSupabaseService(service);
            historicoController.setEstoqueAtual(estoqueId);
        }

        if (dashboardController != null) {
            dashboardController.setSupabaseService(service);
            dashboardController.setEstoqueAtual(estoqueId, estoqueNome);
        }

        // garantir que a tabela seja atualizada no Platform.runLater
        // para que todos os bindings estejam prontos
        if (tabelaController != null) {
            Platform.runLater(() -> {
                System.out.println("📊 Atualizando tabela com " + Estoque.getProdutos().size() + " produtos");
                tabelaController.refresh();
                tabelaController.carregarUltimaAlteracao();
            });
        }
    }
    // ========== MÉTODOS AUXILIARES ==========

    private void atualizarUltimaAlteracao() {
        Time.updateTime();
        if (tabelaController != null) {
            tabelaController.carregarUltimaAlteracao();
        }
    }

    // ========== AÇÕES DO MENU - ESTOQUE ==========

    @FXML
    private void onCriarProduto() {
        String nome;
        String categoria;

        TextInputDialog dialogNome = new TextInputDialog();
        dialogNome.setTitle("Criar Produto");
        dialogNome.setHeaderText(null);
        dialogNome.setContentText("Nome do produto:");
        nome = dialogNome.showAndWait().orElse(null);
        if (nome == null || nome.isBlank()) return;
        nome = nome.toUpperCase();

        if (Estoque.getNomes().contains(nome)) {
            mostrarErro("O produto " + nome + " já existe");
            return;
        }

        TextInputDialog dialogCategoria = new TextInputDialog();
        dialogCategoria.setTitle("Criar Produto");
        dialogCategoria.setHeaderText(null);
        dialogCategoria.setContentText("Categoria:");
        categoria = dialogCategoria.showAndWait().orElse(null);
        if (categoria == null || categoria.isBlank()) return;
        categoria = categoria.toUpperCase();

        int qtdMin, qtd;
        double vlrUnd;
        int r = 0;

        // Quantidade mínima
        do {
            try {
                if (r == 1) {
                    mostrarInfo("Criar Produto", "Quantidade mínima deve ser maior que zero.");
                }
                TextInputDialog dMin = new TextInputDialog();
                dMin.setTitle("Criar Produto");
                dMin.setHeaderText(null);
                dMin.setContentText("Quantidade mínima:");
                String qtdMinStr = dMin.showAndWait().orElse("").trim();
                if (qtdMinStr.isEmpty()) return;
                qtdMin = Integer.parseInt(qtdMinStr);
                r = 1;
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Valores numéricos inválidos.").showAndWait();
                return;
            }
        } while (qtdMin < 0);

        r = 0;

        // Valor unitário
        do {
            try {
                if (r == 1) {
                    mostrarInfo("Criar Produto", "Valor unitário não pode ser menor que zero.");
                }
                TextInputDialog dVlr = new TextInputDialog();
                dVlr.setTitle("Criar Produto");
                dVlr.setHeaderText(null);
                dVlr.setContentText("Valor unitário:");
                String vlrUndStr = dVlr.showAndWait().orElse("").replace(',', '.').trim();
                if (vlrUndStr.isEmpty()) return;
                vlrUnd = Double.parseDouble(vlrUndStr);
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Valores numéricos inválidos.").showAndWait();
                return;
            }
            r = 1;
        } while (vlrUnd <= 0);

        r = 0;

        // Quantidade em estoque
        do {
            try {
                if (r == 1) {
                    mostrarInfo("Criar Produto", "Estoque não pode ser menor que zero.");
                }
                TextInputDialog dQtd = new TextInputDialog();
                dQtd.setTitle("Criar Produto");
                dQtd.setHeaderText(null);
                dQtd.setContentText("Quantidade em estoque:");
                qtd = Integer.parseInt(dQtd.showAndWait().orElse("0").trim());
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Valores numéricos inválidos.").showAndWait();
                return;
            }
            r = 1;
        } while (qtd < 0);

        String tempo = Time.getTime();
        Produto novo = new Produto(nome, qtdMin, vlrUnd, qtd, categoria, tempo);

        // Registrar no histórico
        Movimento movCriacao = new Movimento(novo, "CRIACAO");
        if (historicoController != null) {
            historicoController.registrarMovimento(movCriacao);
        }

        // Adicionar produto
        Categoria c = Categoria.getCategoria(categoria);
        c.addProduto(novo);
        ProdutoService.addEstoque(novo);

        // Atualizar tabela
        if (tabelaController != null) {
            tabelaController.refresh();
        }
    }

    @FXML
    private void onEntrada() {
        if (tabelaController == null) return;

        String nome = tabelaController.pedirProduto(Estoque.getNomes(), "Entrada de produto");
        if (nome == null || nome.isEmpty()) return;
        nome = nome.trim().toUpperCase();

        TextInputDialog dialogQtd = new TextInputDialog();
        dialogQtd.setTitle("Entrada de estoque");
        dialogQtd.setHeaderText("Produto: " + nome);
        dialogQtd.setContentText("Quantidade a entrar:");

        try {
            int qtd = Integer.parseInt(dialogQtd.showAndWait().orElse("0").trim());
            if (qtd == 0) return;

            ProdutoService.entrada(qtd, nome);
            String codigo = Produto.getCodigoPorNome(nome);
            Produto p = Produto.getProdutoPorCodigo(codigo);

            if (p != null) {
                Movimento movEntrada = new Movimento(p, "ENTRADA", qtd);
                if (historicoController != null) {
                    historicoController.registrarMovimento(movEntrada);
                }

                p.setAlterHora(Time.getTime());
                p.atualizaCompra();
            }

            tabelaController.refresh();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Quantidade inválida.").showAndWait();
        }
    }

    @FXML
    private void onSaida() {
        if (tabelaController == null) return;

        String nome = tabelaController.pedirProduto(Estoque.getNomes(), "Saída de produto");
        if (nome == null || nome.isEmpty()) return;
        nome = nome.toUpperCase();

        TextInputDialog dialogQtd = new TextInputDialog();
        dialogQtd.setTitle("Saída de estoque");
        dialogQtd.setHeaderText("Produto: " + nome);
        dialogQtd.setContentText("Quantidade a retirar:");

        try {
            int qtd = Integer.parseInt(dialogQtd.showAndWait().orElse("0").trim());
            if (qtd == 0) return;

            ProdutoService.saida(qtd, nome);
            String codigo = Produto.getCodigoPorNome(nome);
            Produto p = Produto.getProdutoPorCodigo(codigo);

            if (p != null) {
                Movimento movSaida = new Movimento(p, "SAIDA", -qtd);
                if (historicoController != null) {
                    historicoController.registrarMovimento(movSaida);
                }

                p.setAlterHora(Time.getTime());
                p.atualizaCompra();
            }

            tabelaController.refresh();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Quantidade inválida.").showAndWait();
        }
    }

    // ========== AÇÕES DO MENU - ARQUIVO ==========

    @FXML
    private void onSalvar() {
        try {
            atualizarUltimaAlteracao();
            Leitor.salvarEstoque(Estoque.getProdutos());

            if (supabaseService != null && estoqueId != null) {
                salvarNoSupabase();
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Estoque salvo localmente.").showAndWait();
                Produto.setUltimaAcao("s");
            }

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao salvar: " + e.getMessage()).showAndWait();
        }
    }

    private void salvarNoSupabase() {
        Alert progresso = new Alert(Alert.AlertType.INFORMATION);
        progresso.setTitle("Salvando");
        progresso.setHeaderText("Sincronizando com servidor...");
        progresso.setContentText("Aguarde...");
        progresso.show();

        new Thread(() -> {
            try {
                supabaseService.deletarProdutosEstoque(estoqueId);

                for (Produto p : Estoque.getProdutos()) {
                    supabaseService.salvarProduto(p, estoqueId);
                    System.out.println(p.getNome() + " salvo");
                }

                Platform.runLater(() -> {
                    progresso.close();
                    new Alert(Alert.AlertType.INFORMATION,
                            "Estoque salvo localmente e sincronizado com servidor!").showAndWait();
                    Produto.setUltimaAcao("s");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progresso.close();
                    Alert erro = new Alert(Alert.AlertType.WARNING);
                    erro.setTitle("Erro na sincronização");
                    erro.setHeaderText("Salvo localmente, mas falha no servidor");
                    erro.setContentText("Erro: " + e.getMessage() +
                            "\n\nSeus dados estão salvos localmente.");
                    erro.showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void onExportarCsv() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar");
            fileChooser.setInitialFileName("EstoqueCSV.csv");
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("Arquivos CSV (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);

            File pastaInicial = new File(Leitor.pastaDocs, Leitor.nomePasta);
            if (pastaInicial.exists()) {
                fileChooser.setInitialDirectory(pastaInicial);
            }

            Window window = tabelaController.getTabela().getScene().getWindow();
            File arquivo = fileChooser.showSaveDialog(window);

            if (arquivo == null) return;

            if (!arquivo.getName().toLowerCase().endsWith(".csv")) {
                arquivo = new File(arquivo.getAbsolutePath() + ".csv");
            }

            Leitor.exportarEstoqueParaArquivo(Estoque.getProdutos(), arquivo);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportação Concluída");
            alert.setHeaderText("CSV exportado com sucesso!");
            alert.setContentText("Arquivo salvo em:\n" + arquivo.getAbsolutePath());

            ButtonType btnAbrirPasta = new ButtonType("Abrir Pasta", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnFechar = new ButtonType("Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnAbrirPasta, btnFechar);

            File finalArquivo = arquivo;
            alert.showAndWait().ifPresent(response -> {
                if (response == btnAbrirPasta) {
                    try {
                        java.awt.Desktop.getDesktop().open(finalArquivo.getParentFile());
                    } catch (Exception e) {
                        System.err.println("Erro ao abrir pasta: " + e.getMessage());
                    }
                }
            });

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro na Exportação");
            alert.setHeaderText("Falha ao exportar");
            alert.setContentText("Erro: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onImportarCSV() {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Importar");
            alert.setHeaderText("ATENÇÃO");
            alert.getDialogPane().setContent(new Label(
                    "O arquivo precisa estar em uma das seguintes" +
                            "\nordens para que as informações sejam interpretadas como esperado:" +
                            "\n\n codigo -> nome -> categoria -> vlrMin -> vlrUnd -> qtd -> desc(opcional) -> tempo(opcional) -> ..." +
                            "\n\n nome -> categoria -> vlrMin -> vlrUnd -> qtd -> desc(opcional) -> tempo(opcional) -> ..."
            ));
            alert.showAndWait();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Importar arquivo");

            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("Arquivos CSV (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);

            File pastaInicial = new File(Leitor.pastaDocs, Leitor.nomePasta);
            if (pastaInicial.exists()) {
                fileChooser.setInitialDirectory(pastaInicial);
            }

            Window window = tabelaController.getTabela().getScene().getWindow();
            File arquivo = fileChooser.showOpenDialog(window);

            if (arquivo == null) return;

            Leitor.importarCSV(arquivo);

            if (tabelaController != null) {
                tabelaController.refresh();
            }

            mostrarInfo("Importar", "Importado com sucesso!");

        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void onAbrirArquivo() {
        try {
            File pastaApp = new File(Leitor.pastaDocs, Leitor.nomePasta);

            if (!pastaApp.exists()) {
                pastaApp.mkdirs();
            }

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(pastaApp);
            } else {
                mostrarInfo("Pasta de Dados",
                        "Caminho da pasta:\n" + pastaApp.getAbsolutePath());
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível abrir a pasta");
            alert.setContentText("Caminho: " + Leitor.getPath() +
                    "\n\nErro: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // ========== AÇÕES DO MENU - NAVEGAÇÃO ==========

    @FXML
    private void onTrocarEstoque() {
        if (!"s".equals(Produto.getUltimaAcao()) && !"i".equals(Produto.getUltimaAcao())) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Trocar de Estoque");
            confirm.setHeaderText("Existem alterações não salvas");
            confirm.setContentText("Deseja salvar antes de trocar?");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    salvarAntesDeVoltarParaSelecao();
                } else if (response == ButtonType.NO) {
                    voltarParaSelecao();
                }
            });
        } else {
            voltarParaSelecao();
        }
    }

    private void salvarAntesDeVoltarParaSelecao() {
        try {
            atualizarUltimaAlteracao();
            Leitor.salvarEstoque(Estoque.getProdutos());

            if (supabaseService != null && estoqueId != null) {
                Alert progresso = new Alert(Alert.AlertType.INFORMATION);
                progresso.setTitle("Salvando");
                progresso.setHeaderText("Sincronizando com servidor...");
                progresso.setContentText("Aguarde...");
                progresso.show();

                List<Produto> produtosParaSalvar = new ArrayList<>(Estoque.getProdutos());

                new Thread(() -> {
                    try {
                        supabaseService.deletarProdutosEstoque(estoqueId);

                        for (Produto p : produtosParaSalvar) {
                            supabaseService.salvarProduto(p, estoqueId);
                        }

                        Platform.runLater(() -> {
                            progresso.close();
                            Produto.setUltimaAcao("s");
                            voltarParaSelecao();
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            progresso.close();
                            Alert erro = new Alert(Alert.AlertType.ERROR);
                            erro.setTitle("Erro ao salvar");
                            erro.setContentText("Não foi possível sincronizar: " + e.getMessage() +
                                    "\n\nVoltando sem salvar no servidor.");
                            erro.showAndWait();
                            voltarParaSelecao();
                        });
                    }
                }).start();

            } else {
                Produto.setUltimaAcao("s");
                voltarParaSelecao();
            }

        } catch (IOException e) {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Erro");
            erro.setContentText("Erro ao salvar localmente: " + e.getMessage());
            erro.showAndWait();
        }
    }

    @FXML
    private void onSair() {
        Stage stage = (Stage) tabelaController.getTabela().getScene().getWindow();
        stage.close();
    }

    private void voltarParaSelecao() {
        try {
            Estoque.getProdutos().clear();
            Categoria.categorias.clear();
            Estoque.getNomes().clear();

            Stage stage = (Stage) tabelaController.getTabela().getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    EstoqueAppFX.class.getResource("selecao-estoque-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 500, 400);

            SelecaoEstoqueController controller = loader.getController();
            controller.setSupabaseService(supabaseService);

            stage.setScene(scene);
            stage.setTitle("Selecionar Estoque");

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Erro ao voltar: " + e.getMessage()).showAndWait();
        }
    }

    // ========== AÇÕES DO MENU - IMPRIMIR ==========

    @FXML
    private void onImprimir() {
        TableView<Produto> tabela = tabelaController.getTabela();
        tabela.getSelectionModel().clearSelection();

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null || !job.showPrintDialog(tabela.getScene().getWindow())) return;

        PageLayout pageLayout = job.getPrinter().createPageLayout(
                Paper.A4,
                PageOrientation.LANDSCAPE,
                Printer.MarginType.HARDWARE_MINIMUM);

        double alturaTabela = Math.min(tabela.getItems().size() * 50, pageLayout.getPrintableHeight());
        tabela.setPrefHeight(alturaTabela);
        tabela.setFixedCellSize(50);

        double scaleX = pageLayout.getPrintableWidth() / tabela.getWidth();
        double scaleY = pageLayout.getPrintableHeight() / tabela.getHeight();
        double scale = Math.min(scaleX, scaleY);

        tabela.getTransforms().add(new Scale(scale, scale));

        boolean sucesso = job.printPage(pageLayout, tabela);
        if (sucesso) {
            job.endJob();
        }

        tabela.getTransforms().clear();
        tabela.setPrefHeight(Region.USE_COMPUTED_SIZE);
        tabela.setFixedCellSize(Region.USE_COMPUTED_SIZE);
    }

    // ========== AÇÕES DO MENU - VERSÃO ==========

    @FXML
    private void onVerificarAtualizacoes() {
        servicoUpdater();
    }

    public void servicoUpdater() {
        UpdateService service = new UpdateService();

        try {
            UpdateInfo info = service.verificarUpdate();

            if (info.getVersaoRemota() == null || info.getUrlInstaller() == null) {
                mostrarInfo("Atualização", "Não foi possível ler informações do release.");
                return;
            }

            if (!info.hasUpdate()) {
                mostrarInfo("Atualização", "Você já está na versão mais recente (" + info.getVersaoAtual() + ").");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Nova versão disponível");
            alert.setHeaderText("Versão atual: " + info.getVersaoAtual() +
                    "\nNova versão: " + info.getVersaoRemota());
            alert.setContentText("Novidades: " + info.getChangeLog() +
                    "\nDeseja baixar o novo instalador agora?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            alert.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    try {
                        mostrarDialogDownloadComProgresso(info);
                    } catch (Exception e) {
                        mostrarErro("Erro ao baixar/abrir instalador: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            mostrarErro("Erro ao verificar atualizações: " + e.getMessage());
        }
    }

    public static void verificarAtualizacaoSilenciosa() {
        UpdateService service = new UpdateService();

        try {
            UpdateInfo info = service.verificarUpdate();

            if (info.getVersaoRemota() == null || info.getUrlInstaller() == null || !info.hasUpdate()) {
                return;
            }

            Platform.runLater(() -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Atualização disponível");
                confirm.setHeaderText("Versão atual: " + info.getVersaoAtual() +
                        "\nNova versão: " + info.getVersaoRemota());
                confirm.setContentText("Novidades: " + info.getChangeLog() +
                        "\nDeseja baixar agora?");

                ButtonType BT_ATUALIZAR = new ButtonType("Atualizar agora", ButtonBar.ButtonData.YES);
                ButtonType BT_DEPOIS = new ButtonType("Lembrar depois", ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType BT_IGNORAR = new ButtonType("Não perguntar nesta versão", ButtonBar.ButtonData.NO);
                confirm.getButtonTypes().setAll(BT_ATUALIZAR, BT_DEPOIS, BT_IGNORAR);

                confirm.showAndWait().ifPresent(result -> {
                    if (result == BT_ATUALIZAR) {
                        new EstoqueController().mostrarDialogDownloadComProgresso(info);
                    } else if (result == BT_IGNORAR) {
                        mostrarInfoStatic("Ignorar atualização",
                                "O programa não irá mais avisar de novas versões. " +
                                        "\nAinda será disponível atualizar em Versão -> Verificar atualizações. " +
                                        "\nPara reverter essa mudança, vá em Versão -> Avisar atualizações.");
                        try {
                            Misc.setNegouAtualizacao(true);
                        } catch (Exception ignored) {
                        }
                    }
                });
            });

        } catch (Exception e) {
            System.err.println("Erro ao verificar update: " + e.getMessage());
        }
    }

    public void mostrarDialogDownloadComProgresso(UpdateInfo info) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Atualização disponível");
        dialog.setHeaderText("Baixando nova versão " + info.getVersaoRemota());

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        Label statusLabel = new Label("Preparando download...");

        VBox content = new VBox(10, statusLabel, progressBar);
        content.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                URL url = new URL(info.getUrlInstaller());
                URLConnection conn = url.openConnection();
                long totalBytes = conn.getContentLengthLong();

                updateMessage("Baixando " + (totalBytes / 1024 / 1024) + " MB...");

                LongConsumer progressBytes = downloaded -> {
                    if (totalBytes > 0) {
                        updateProgress(downloaded, totalBytes);
                        updateMessage(String.format("Baixando... %.1f%% (%d/%d MB)",
                                downloaded * 100.0 / totalBytes,
                                downloaded / 1024 / 1024,
                                totalBytes / 1024 / 1024));
                    } else {
                        updateMessage("Baixando...");
                    }
                };

                Path installer = UpdateService.downloadComBarraDeProgresso(
                        info.getUrlInstaller(),
                        info.getVersaoRemota(),
                        progressBytes,
                        totalBytes
                );

                updateMessage("Executando instalador...");
                UpdateService.runInstaller(installer);
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());

        dialog.setOnCloseRequest(_ -> task.cancel());

        new Thread(task).start();
        dialog.showAndWait();
    }

    @FXML
    public void onAvisarAtualizacoes(ActionEvent actionEvent) {
        try {
            Misc.setNegouAtualizacao(false);
        } catch (IOException ignored) {
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Avisar atualizações");
        alert.setContentText("Você será avisado de novas atualizações. Deseja verificar se há uma versão mais nova?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                onVerificarAtualizacoes();
            }
        });
    }

    @FXML
    private void onAlterarVersao() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Alterar versão");
        confirm.setContentText("Para qual versão você deseja alterar?");

        ButtonType BT_ESTAVEL = new ButtonType("Estável", ButtonBar.ButtonData.YES);
        ButtonType BT_BETA = new ButtonType("BETA", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(BT_ESTAVEL, BT_BETA);

        confirm.showAndWait().ifPresent(result -> {
            if (result == BT_ESTAVEL) {
                if (AppInfo.UPDATE_CHANNEL.equalsIgnoreCase("stable")) {
                    mostrarInfo("Alterar versão", "Você já está na versão estável.");
                } else {
                    AppInfo.setUpdateChannel("stable");
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Alterar Versão");
                    alert.setContentText("Novas atualizações buscarão a versão estável." +
                            "\nDeseja verificar atualizações?");
                    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            onVerificarAtualizacoes();
                        }
                    });
                }
            } else if (AppInfo.UPDATE_CHANNEL.equalsIgnoreCase("beta")) {
                mostrarInfo("Alterar versão", "Você já está na versão BETA.");
            } else {
                AppInfo.setUpdateChannel("beta");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Alterar Versão");
                alert.setContentText("Novas atualizações buscarão a versão beta." +
                        "\nDeseja verificar atualizações?");
                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        onVerificarAtualizacoes();
                    }
                });
            }
        });
    }

    @FXML
    private void onNovidades() {
        mostrarChangelog(AppInfo.novidades);
    }

    @FXML
    private void onVersoesAnteriores() {
        EstoqueAppFX.getHostServicesStatic().showDocument(AppInfo.RELEASES_URL);
    }

    // ========== AÇÕES DO MENU - AJUDA ==========

    @FXML
    private void onSobre() {
        String canal;
        if (AppInfo.VERSAO_CHANNEL.equalsIgnoreCase("stable")) {
            canal = "";
        } else {
            canal = "BETA";
        }

        String msg = String.format("""
        %s
        Versão: %s %s

        Autor: Arthur

        Pasta de dados:
        %s
        """, AppInfo.NOME_APP, AppInfo.VERSAO, canal, Leitor.getPath());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sobre SistemaEstoqueFX");
        alert.setHeaderText("Sobre o sistema");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void onReportarBug() {
        EstoqueAppFX.getHostServicesStatic().showDocument(AppInfo.BUG_REPORT_URL);
    }

    @FXML
    private void onSugestoes() {
        EstoqueAppFX.getHostServicesStatic().showDocument(AppInfo.SUGGESTIONS_URL);
    }

    // ========== MÉTODOS AUXILIARES - DIALOGS ==========

    public void mostrarInfo(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void mostrarInfoStatic(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void mostrarChangelog(String changelog) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Novidades");
        alert.setHeaderText("Novidades desta versão");

        TextArea textArea = new TextArea(changelog);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        textArea.setPrefColumnCount(60);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ========== SETTERS ==========

    public void setHistoricoController(HistoricoController historicoController) {
        this.historicoController = historicoController;
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public static void setStage(Stage stage) {
        EstoqueController.stage = stage;
    }

    public static Stage getStage() {
        return stage;
    }
}
