package com.estoquefx.controller.estoque;

import com.estoquefx.EstoqueAppFX;
import com.estoquefx.controller.SelecaoEstoqueController;
import com.estoquefx.data.Leitor;
import com.estoquefx.model.estoque.Categoria;
import com.estoquefx.model.estoque.Estoque;
import com.estoquefx.model.estoque.Movimento;
import com.estoquefx.model.estoque.Produto;
import com.estoquefx.service.*;
import com.estoquefx.service.estoque.ProdutoService;
import com.estoquefx.updater.core.*;
import com.estoquefx.util.I18n;
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
import java.text.MessageFormat;
import java.util.*;
import java.util.function.LongConsumer;

import static com.estoquefx.controller.MainController.mostrarInfoStatic;

public class MenuController {

    private TabelaController tabelaController;
    private HistoricoController historicoController;
    private SupabaseService supabaseService;
    private String estoqueId;

    @FXML
    public void initialize() {
        System.out.println("📋 Inicializando MenuController...");
    }

    // SETTERS PARA DEPENDÊNCIAS

    public void setTabelaController(TabelaController tabelaController) {
        this.tabelaController = tabelaController;
    }

    public void setHistoricoController(HistoricoController historicoController) {
        this.historicoController = historicoController;
    }

    public void setSupabaseService(SupabaseService service, String estoqueId) {
        this.supabaseService = service;
        this.estoqueId = estoqueId;
    }

    // AÇÕES DO MENU - ESTOQUE

    @FXML
    void onCriarProduto() {
        String nome;
        String categoria;

        TextInputDialog dialogNome = new TextInputDialog();
        dialogNome.setTitle(I18n.t("produto.dialog.title"));
        dialogNome.setHeaderText(null);
        dialogNome.setContentText(I18n.t("produto.dialog.name"));
        nome = dialogNome.showAndWait().orElse(null);
        if (nome == null || nome.isBlank()) return;
        nome = nome.toUpperCase();

        if (Estoque.getNomes().contains(nome)) {
            mostrarInfo(Alert.AlertType.ERROR, I18n.t("error"), null,
                    MessageFormat.format(I18n.t("produto.dialog.already_exists"), nome));
            return;
        }

        TextInputDialog dialogCategoria = new TextInputDialog();
        dialogCategoria.setTitle(I18n.t("produto.dialog.title"));
        dialogCategoria.setHeaderText(null);
        dialogCategoria.setContentText(I18n.t("produto.dialog.category"));
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
                    mostrarInfo(Alert.AlertType.NONE, I18n.t("produto.dialog.title"), null,
                            I18n.t("produto.dialog.min_qty_invalid"));
                }
                TextInputDialog dMin = new TextInputDialog();
                dMin.setTitle(I18n.t("produto.dialog.title"));
                dMin.setHeaderText(null);
                dMin.setContentText(I18n.t("produto.dialog.min_qty"));
                String qtdMinStr = dMin.showAndWait().orElse("").trim();
                if (qtdMinStr.isEmpty()) return;
                qtdMin = Integer.parseInt(qtdMinStr);
                r = 1;
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, I18n.t("produto.dialog.invalid_number")).showAndWait();
                return;
            }
        } while (qtdMin < 0);

        r = 0;

        // Valor unitário
        do {
            try {
                if (r == 1) {
                    mostrarInfo(Alert.AlertType.NONE, I18n.t("produto.dialog.title"), null,
                            I18n.t("produto.dialog.unit_value_invalid"));
                }
                TextInputDialog dVlr = new TextInputDialog();
                dVlr.setTitle(I18n.t("produto.dialog.title"));
                dVlr.setHeaderText(null);
                dVlr.setContentText(I18n.t("produto.dialog.unit_value"));
                String vlrUndStr = dVlr.showAndWait().orElse("").replace(',', '.').trim();
                if (vlrUndStr.isEmpty()) return;
                vlrUnd = Double.parseDouble(vlrUndStr);
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, I18n.t("produto.dialog.invalid_number")).showAndWait();
                return;
            }
            r = 1;
        } while (vlrUnd <= 0);

        r = 0;

        // Quantidade em estoque
        do {
            try {
                if (r == 1) {
                    mostrarInfo(Alert.AlertType.NONE, I18n.t("produto.dialog.title"),
                            null, I18n.t("produto.dialog.qty_invalid"));
                }
                TextInputDialog dQtd = new TextInputDialog();
                dQtd.setTitle(I18n.t("produto.dialog.title"));
                dQtd.setHeaderText(null);
                dQtd.setContentText(I18n.t("produto.dialog.qty"));
                qtd = Integer.parseInt(dQtd.showAndWait().orElse("0").trim());
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, I18n.t("produto.dialog.invalid_number")).showAndWait();
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
    void onEntrada() {
        if (tabelaController == null) return;

        String nome = tabelaController.pedirProduto(Estoque.getNomes(), I18n.t("produto.entry.title"));
        if (nome == null || nome.isEmpty()) return;
        nome = nome.trim().toUpperCase();

        TextInputDialog dialogQtd = new TextInputDialog();
        dialogQtd.setTitle(I18n.t("produto.entry.title"));
        dialogQtd.setHeaderText(MessageFormat.format(I18n.t("produto.entry.header"), nome));
        dialogQtd.setContentText(I18n.t("produto.entry.qty"));

        try {
            int qtd = Integer.parseInt(dialogQtd.showAndWait().orElse("0").trim());
            if (qtd <= 0) return;

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
            new Alert(Alert.AlertType.ERROR, I18n.t("produto.entry.invalid_qty")).showAndWait();
        }
    }

    @FXML
    void onSaida() {
        if (tabelaController == null) return;

        String nome = tabelaController.pedirProduto(Estoque.getNomes(), I18n.t("produto.exit.title"));
        if (nome == null || nome.isEmpty()) return;
        nome = nome.toUpperCase();

        TextInputDialog dialogQtd = new TextInputDialog();
        dialogQtd.setTitle(I18n.t("produto.exit.title"));
        dialogQtd.setHeaderText(MessageFormat.format(I18n.t("produto.exit.header"), nome));
        dialogQtd.setContentText(I18n.t("produto.exit.qty"));

        try {
            int qtd = Integer.parseInt(dialogQtd.showAndWait().orElse("0").trim());
            if (qtd <= 0) return;

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
            new Alert(Alert.AlertType.ERROR, I18n.t("produto.exit.invalid_qty")).showAndWait();
        }
    }

    //AÇÕES DO MENU

    @FXML
    private void onSalvar() {
        try {
            atualizarUltimaAlteracao();
            Leitor.salvarEstoque(Estoque.getProdutos());

            if (supabaseService != null && estoqueId != null) {
                salvarNoSupabase();
            } else {
                new Alert(Alert.AlertType.INFORMATION, I18n.t("salvar.local_only")).showAndWait();
                Produto.setUltimaAcao("s");
            }

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    MessageFormat.format(I18n.t("salvar.local.error"), e.getMessage())).showAndWait();
        }
    }

    private void salvarNoSupabase() {
        Alert progresso = new Alert(Alert.AlertType.INFORMATION);
        progresso.setTitle(I18n.t("salvar.dialog.title"));
        progresso.setHeaderText(I18n.t("salvar.dialog.header"));
        progresso.setContentText(I18n.t("salvar.dialog.content"));
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
                    new Alert(Alert.AlertType.INFORMATION, I18n.t("salvar.success")).showAndWait();
                    Produto.setUltimaAcao("s");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progresso.close();
                    Alert erro = new Alert(Alert.AlertType.WARNING);
                    erro.setTitle(I18n.t("salvar.error.title"));
                    erro.setHeaderText(I18n.t("salvar.error.header"));
                    erro.setContentText(MessageFormat.format(I18n.t("error.generic"), e.getMessage()) +
                            "\n\n" + I18n.t("salvar.error.body"));
                    erro.showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void onExportarCsv() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(I18n.t("csv.export.chooser.title"));
            fileChooser.setInitialFileName("EstoqueCSV.csv");
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter(I18n.t("csv.export.filter"), "*.csv");
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
            alert.setTitle(I18n.t("csv.export.done.title"));
            alert.setHeaderText(I18n.t("csv.export.done.header"));
            alert.setContentText(MessageFormat.format(I18n.t("csv.export.done.body"), arquivo.getAbsolutePath()));

            ButtonType btnAbrirPasta = new ButtonType(I18n.t("csv.export.btn.open_folder"), ButtonBar.ButtonData.OK_DONE);
            ButtonType btnFechar = new ButtonType(I18n.t("csv.export.btn.close"), ButtonBar.ButtonData.CANCEL_CLOSE);
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
            alert.setTitle(I18n.t("csv.export.error.title"));
            alert.setHeaderText(I18n.t("csv.export.error.header"));
            alert.setContentText(MessageFormat.format(I18n.t("error.generic"), e.getMessage()));
            alert.showAndWait();
        }
    }

    @FXML
    private void onImportarCSV() {
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(I18n.t("csv.import.warning.title"));
            alert.setHeaderText(I18n.t("csv.import.warning.header"));
            alert.getDialogPane().setContent(new Label(I18n.t("csv.import.warning.body")));
            alert.showAndWait();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(I18n.t("csv.import.chooser.title"));

            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter(I18n.t("csv.export.filter"), "*.csv");
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

            mostrarInfo(Alert.AlertType.INFORMATION, I18n.t("csv.import.warning.title"),
                    null, I18n.t("csv.import.success"));

        } catch (Exception e) {
            mostrarInfo(Alert.AlertType.ERROR, I18n.t("error"), null, e.getMessage());
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
                mostrarInfo(Alert.AlertType.INFORMATION, I18n.t("pasta.title"), null,
                        MessageFormat.format(I18n.t("pasta.path"), pastaApp.getAbsolutePath()));
            }

        } catch (Exception e) {
            mostrarInfo(Alert.AlertType.ERROR, I18n.t("error"), I18n.t("pasta.error"),
                    "Caminho: " + Leitor.getPath() + "\n\nErro: " + e.getMessage());
        }
    }

    @FXML
    private void onTrocarEstoque() {
        salvarSilenciosamente();
        voltarParaSelecao();
    }

    private void salvarAntesDeVoltarParaSelecao() {
        try {
            atualizarUltimaAlteracao();
            Leitor.salvarEstoque(Estoque.getProdutos());

            if (supabaseService != null && estoqueId != null) {
                Alert progresso = new Alert(Alert.AlertType.INFORMATION);
                progresso.setTitle(I18n.t("salvar.dialog.title"));
                progresso.setHeaderText(I18n.t("salvar.dialog.header"));
                progresso.setContentText(I18n.t("salvar.dialog.content"));
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
                            erro.setTitle(I18n.t("salvar.back.error.title"));
                            erro.setContentText(MessageFormat.format(I18n.t("salvar.back.error.body"), e.getMessage()));
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
            erro.setTitle(I18n.t("error"));
            erro.setContentText(MessageFormat.format(I18n.t("salvar.local.error"), e.getMessage()));
            erro.showAndWait();
        }
    }

    @FXML
    private void onSair() {
        salvarSilenciosamente();
        Stage stage = (Stage) tabelaController.getTabela().getScene().getWindow();
        stage.close();
    }

    private void voltarParaSelecao() {
        try {
            Estoque.getProdutos().clear();
            Categoria.categorias.clear();
            Estoque.getNomes().clear();

            if (tabelaController != null) {

                Stage stage = (Stage) tabelaController.getTabela().getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(
                        EstoqueAppFX.class.getResource("selecao-estoque-view.fxml"),
                        I18n.getBundle()
                );
                Scene scene = new Scene(loader.load(), 500, 400);

                SelecaoEstoqueController controller = loader.getController();
                controller.setSupabaseService(supabaseService);

                stage.setScene(scene);
                stage.setTitle(I18n.t("selecao.title"));
            }

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    MessageFormat.format(I18n.t("error.generic"), e.getMessage())).showAndWait();
        }
    }

    // AÇÕES DO MENU - IMPRIMIR

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

    @FXML
    private void onVerificarAtualizacoes() {
        servicoUpdater();
    }

    public void servicoUpdater() {
        UpdateService service = new UpdateService();

        try {
            UpdateInfo info = service.verificarUpdate();

            if (info.getVersaoRemota() == null || info.getUrlInstaller() == null) {
                mostrarInfo(Alert.AlertType.INFORMATION, I18n.t("update.available.title"),
                        null, I18n.t("update.read_error"));
                return;
            }

            if (!info.hasUpdate()) {
                mostrarInfo(Alert.AlertType.INFORMATION, I18n.t("update.available.title"),
                        null, MessageFormat.format(I18n.t("update.up_to_date"), info.getVersaoAtual()));
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(I18n.t("update.available.title"));
            alert.setHeaderText(MessageFormat.format(I18n.t("update.available.header"),
                    info.getVersaoAtual(), info.getVersaoRemota()));
            alert.setContentText(MessageFormat.format(I18n.t("update.available.body"),
                    info.getChangeLog()));
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            alert.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    try {
                        mostrarDialogDownloadComProgresso(info);
                    } catch (Exception e) {
                        mostrarInfo(Alert.AlertType.ERROR, I18n.t("error"), null,
                                MessageFormat.format(I18n.t("update.error.download"), e.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            mostrarInfo(Alert.AlertType.ERROR, I18n.t("error"), null,
                    MessageFormat.format(I18n.t("update.error.check"), e.getMessage()));
        }
    }

    public void mostrarDialogDownloadComProgresso(UpdateInfo info) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(I18n.t("update.download.title"));
        dialog.setHeaderText(MessageFormat.format(I18n.t("update.download.header"), info.getVersaoRemota()));

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        Label statusLabel = new Label(I18n.t("update.download.preparing"));

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
                        progressBytes
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
        alert.setTitle(I18n.t("update.notify.title"));
        alert.setContentText(I18n.t("update.notify.body"));
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                onVerificarAtualizacoes();
            }
        });
    }

    @FXML
    private void onNovidades() {
        mostrarChangelog(I18n.t("novidades.changelog"));
    }

    @FXML
    private void onVersoesAnteriores() {
        EstoqueAppFX.getHostServicesStatic().showDocument(AppInfo.RELEASES_URL);
    }

    @FXML
    private void onSobre() {
        String msg = String.format(I18n.t("sobre.body"),
                AppInfo.NOME_APP, AppInfo.VERSAO, Leitor.getPath());
        mostrarInfo(Alert.AlertType.INFORMATION, I18n.t("sobre.title"), I18n.t("sobre.header"), msg);
    }

    @FXML
    private void onReportarBug() {
        EstoqueAppFX.getHostServicesStatic().showDocument(AppInfo.BUG_REPORT_URL);
    }

    @FXML
    private void onSugestoes() {
        EstoqueAppFX.getHostServicesStatic().showDocument(AppInfo.SUGGESTIONS_URL);
    }

    // MÉTODOS AUXILIARES

    private void atualizarUltimaAlteracao() {
        Time.updateTime();
        if (tabelaController != null) {
            tabelaController.carregarUltimaAlteracao();
        }
    }

    public void mostrarInfo(Alert.AlertType tipo, String titulo, String header, String msg) {
        mostrarInfoStatic(tipo, titulo, header, msg);
    }


    public void mostrarChangelog(String changelog) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.t("novidades.title"));
        alert.setHeaderText(I18n.t("novidades.header"));

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

    public void salvarSilenciosamente() {
        try {
            atualizarUltimaAlteracao();
            Leitor.salvarEstoque(Estoque.getProdutos());

            if (supabaseService != null && estoqueId != null) {
                try {
                    supabaseService.deletarProdutosEstoque(estoqueId);
                    for (Produto p : Estoque.getProdutos()) {
                        supabaseService.salvarProduto(p, estoqueId);
                    }
                    System.out.println("✓ Sincronizado ao trocar estoque");
                } catch (Exception e) {
                    System.err.println("⚠ Erro ao sincronizar: " + e.getMessage());
                }
            }

            Produto.setUltimaAcao("s");
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar: " + e.getMessage());
        }
    }
}
