package com.estoquefx;


import com.estoquefx.updater.core.*;


import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.converter.IntegerStringConverter;
import javafx.stage.DirectoryChooser;

import org.controlsfx.control.textfield.*;

import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.function.LongConsumer;


public class EstoqueController {

    @FXML private Label lblUltimaAlteracao;
    private final StringProperty ultimaAlteracao = new SimpleStringProperty("Salvo em: ");

    @FXML private Label lblSaldoTotal;
    private String saldo = String.format("%.2f", Misc.getTotal());
    private StringProperty saldoTotal = new SimpleStringProperty("Saldo total: " + "R$ " + saldo);

    @FXML private TableView<Produto> tabela;
    @FXML private TableColumn<Produto, String> colCodigo;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colCategoria;
    @FXML private TableColumn<Produto, Integer> colQtdMin;
    @FXML private TableColumn<Produto, String> colValorUnd;
    @FXML private TableColumn<Produto, Integer> colQtd;
    @FXML private TableColumn<Produto, String> colOrientacao;
    @FXML private TableColumn<Produto, String> colSaldo;
    @FXML private TableColumn<Produto, String> colDescricao;
    @FXML private TableColumn<Produto, String> colHora;

    @FXML private Button btnCriar;
    @FXML private Button btnEntrada;
    @FXML private Button btnSaida;
    @FXML private Button btnSalvar;
    @FXML private Button btnExportar;

    private ObservableList<Produto> dados;
    private FilteredList<Produto> filtrados;

    @FXML private TextField txtBusca;

    private static Stage stage;


    @FXML
    public void initialize() {
        lblUltimaAlteracao.textProperty().bind(ultimaAlteracao);
        Platform.runLater(this::carregarUltimaAlteracao);

        lblSaldoTotal.textProperty().bind(saldoTotal);

        tabela.setEditable(true);
        // liga colunas aos getters de Produto
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colQtdMin.setCellValueFactory(new PropertyValueFactory<>("vlrMin"));
        colValorUnd.setCellValueFactory(new PropertyValueFactory<>("vlrUnd"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("qtd"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("alterHora"));

        colOrientacao.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        Misc.isUrgente(cellData.getValue())
                )
        );
        colSaldo.setCellValueFactory(cellData -> {
            Produto p = cellData.getValue();
            double saldo = p.getVlrUnd() * p.getQtd();
            String saldoStr = String.format("%.2f", saldo);
            return new SimpleStringProperty("R$ " + saldoStr);
        });

        colValorUnd.setCellValueFactory(cellData -> {
            Produto p = cellData.getValue();
            double vlrUnd = p.getVlrUnd();
            String vlrUndStr = String.format("%.2f", vlrUnd);
            return new SimpleStringProperty("R$ " + vlrUndStr);
        });

        colNome.setCellFactory(TextFieldTableCell.forTableColumn());
        colNome.setOnEditCommit(event -> {
            Produto p = event.getRowValue();
            String novoNome = event.getNewValue();
            if (novoNome == null || novoNome.isBlank()) return;
            novoNome = novoNome.trim().toUpperCase();
            p.setNome(novoNome);

            tabela.refresh();
        });

        colCategoria.setCellFactory(TextFieldTableCell.forTableColumn());
        colCategoria.setOnEditCommit(event -> {
            Produto p = event.getRowValue();
            String novaCat = event.getNewValue();
            if (novaCat == null || novaCat.isBlank()) return;
            novaCat = novaCat.trim().toUpperCase();
            p.setCategoria(novaCat);
            Misc.addCategoria(novaCat);
            tabela.refresh();
        });

        colQtdMin.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQtdMin.setOnEditCommit(event -> {
            Produto p = event.getRowValue();
            try {
                int novoMin = Integer.parseInt(event.getNewValue().toString().trim());
                p.setVlrMin(novoMin);
                p.setAlterHora(Misc.getTime());
                Produto.atualizaCompra(p);
                tabela.refresh();
            } catch (NumberFormatException ex) {
                // erro inválido
            }
        });

        colQtd.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQtd.setOnEditCommit(event -> {
            Produto p = event.getRowValue();
            try {
                int novaQtd = Integer.parseInt(event.getNewValue().toString().trim());
                p.setQtd(novaQtd);
                Produto.atualizaCompra(p);
                p.setAlterHora(Misc.getTime());
                atualizarTotal();
                tabela.refresh();
            } catch (NumberFormatException ex) {
                // erro inválido
            }
        });

        colValorUnd.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(String value) {
                return value;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        }));

        colValorUnd.setOnEditCommit(event -> {
            Produto p = event.getRowValue();
            String texto = event.getNewValue();
            try {
                texto = texto.replace("R$", "");
                double novoVlr = Double.parseDouble(texto.replace(',', '.').trim());
                p.setVlrUnd(novoVlr);
                p.setAlterHora(Misc.getTime());
                atualizarTotal();
                tabela.refresh();
            } catch (NumberFormatException ex) {
                // erro inválido
            }
        });

        colHora.setCellFactory(TextFieldTableCell.forTableColumn());




        colDescricao.setCellFactory(col -> new TableCell<>() {
            private final Text text = new Text();
            {
                setGraphic(text);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
                text.wrappingWidthProperty().bind(col.widthProperty().subtract(10));

                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !isEmpty()) {
                        Produto p = getTableView().getItems().get(getIndex());
                        abrirDialogoDescricao(p);
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (text.fillProperty().isBound()) {
                    text.fillProperty().unbind();
                }
                if (empty || item == null) {
                    text.setText("");
                    return;
                }
                text.setText(item);

                TableRow<?> row = getTableRow();
                if (row != null) {
                    ChangeListener<String> styleListener = (obs, oldClass, newClass) -> {
                        atualizarCor(row);
                    };
                    row.getStyleClass().addListener((ListChangeListener<String>) c -> {
                        while (c.next()) {
                            if (c.wasAdded() || c.wasRemoved()) {
                                atualizarCor(row);
                            }
                        }
                    });
                    atualizarCor(row);
                }
            }

            private void atualizarCor(TableRow<?> row) {
                if (text.fillProperty().isBound()) {
                    text.fillProperty().unbind();
                }
                boolean isEstoqueBaixo = row.getStyleClass().contains("estoque-baixo");
                if (isEstoqueBaixo) {
                    text.setFill(Color.BLACK);
                } else {
                    text.fillProperty().bind(
                            row.selectedProperty().map(selected ->
                                    selected ? Color.WHITE : Color.BLACK
                            )
                    );
                }
            }
        });



        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colNome.setMinWidth(90);
        colNome.setMaxWidth(200);
        colCodigo.setMinWidth(50);
        colCodigo.setMaxWidth(50);
        colQtdMin.setMinWidth(60);
        colQtdMin.setMaxWidth(100);
        colQtd.setMinWidth(75);
        colQtd.setMaxWidth(100);
        colValorUnd.setMinWidth(100);
        colValorUnd.setMaxWidth(130);
        colSaldo.setMinWidth(120);
        colSaldo.setMaxWidth(150);
        colOrientacao.setMinWidth(105);
        colOrientacao.setMaxWidth(105);
        colHora.setMinWidth(75);
        colHora.setMaxWidth(95);
        colDescricao.setMinWidth(150);
        colDescricao.setPrefWidth(300);

        dados = FXCollections.observableArrayList(Produto.estoque);
        filtrados = new FilteredList<>(dados, _ -> true);

        txtBusca.textProperty().addListener((_, _, newValue) -> {
            String filtro = (newValue == null) ? "" : newValue.trim().toUpperCase();

            filtrados.setPredicate(produto -> {
                if (filtro.isEmpty()) return true;

                String nome = produto.getNome() == null ? "" : produto.getNome().toUpperCase();
                String cat  = produto.getCategoria() == null ? "" : produto.getCategoria().toUpperCase();
                String cod  = produto.getCodigo() == null ? "" : produto.getCodigo().toUpperCase();

                if (filtro.equalsIgnoreCase("urgente")){
                    return produto.getCompra()
                            || cat.contains(filtro)
                            || nome.contains(filtro);
                }

                return nome.contains(filtro)
                        || cat.contains(filtro)
                        || cod.contains(filtro);
            });
        });

        SortedList<Produto> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabela.comparatorProperty());

        tabela.setItems(ordenados);


        tabela.setRowFactory(_ -> new TableRow<>() {
            @Override
            protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getCompra()) {
                    getStyleClass().add("estoque-baixo");
                } else {
                    setStyle("");
                }
            }
        });

        filtrados.addListener((ListChangeListener<Produto>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    tabela.refresh();
                }
            }
        });

        Platform.runLater(() -> {
            tabela.getScene().getRoot().requestFocus();
        });
    }

    //substituída pela versão com autocomplete
    private String perguntarNomeProduto(String titulo) {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        String sugestaoNome = selecionado != null ? selecionado.getNome() : "";

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText("Informe o produto:");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextField txtNome = new TextField(sugestaoNome);
        txtNome.setPromptText("Nome do produto");

        dialog.getDialogPane().setContent(txtNome);

        dialog.setResultConverter(button -> {
            if (button == okButtonType) {
                String n = txtNome.getText();
                return (n == null || n.isBlank()) ? null : n;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    public String pedirProduto(HashSet<String> produtos, String title) {

        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText("Informe o nome do produto");

            TextField editor = dialog.getEditor();
            editor.setPromptText("Nome do produto");

            TextFields.bindAutoCompletion(editor, produtos);

            return dialog.showAndWait().orElse("");
        } catch (RuntimeException ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro" + ex.getMessage());
            alert.showAndWait();
            return "";
        }
    }

    public void servicoUpdater(){
        UpdateService service = new UpdateService();

        try {
            UpdateInfo info;
            info = service.verificarUpdate();

            if (info.getVersaoRemota() == null || info.getUrlInstaller() == null) {
                mostrarInfo("Atualização", "Não foi possível ler informações do release.");
                return;
            }

            if (!info.hasUpdate()) {
                mostrarInfo("Atualização", "Você já está na versão mais recente(" + info.getVersaoAtual() + ").");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Nova versão disponível");
            alert.setHeaderText("Versão atual: " + info.getVersaoAtual()
                    + "\nNova versão: " + info.getVersaoRemota());
            alert.setContentText("Novidades: " + info.getChangeLog()
                    + "\nDeseja baixar o novo instalador agora?");
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

    private void abrirDialogoDescricao(Produto p) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Editar descrição");

        ButtonType okButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextArea area = new TextArea(p.getDescricao());
        area.setWrapText(true);
        area.setPrefRowCount(6);
        area.setPrefColumnCount(40);

        area.setStyle(
                "-fx-control-inner-background: #ffffff;" +
                        "-fx-background-color: #f0f0f0;" +
                        "-fx-text-fill: black;"
        );

        dialog.getDialogPane().setContent(area);

        dialog.getDialogPane().setStyle(
                "-fx-background-color: #e0e0e0;" +
                        "-fx-text-fill: black;"
        );

        dialog.setResultConverter(button -> {
            if (button == okButtonType) {
                return area.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(novaDesc -> {
            novaDesc = novaDesc.trim();
            p.setDescricao(novaDesc);
            tabela.refresh();
        });
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
                confirm.setHeaderText("Versão atual: " + info.getVersaoAtual() + "\nNova versão: " + info.getVersaoRemota());
                confirm.setContentText("Novidades: " + info.getChangeLog()
                                        + "\nDeseja baixar agora?");
                ButtonType BT_ATUALIZAR = new ButtonType("Atualizar agora", ButtonBar.ButtonData.YES);
                ButtonType BT_DEPOIS   = new ButtonType("Lembrar depois", ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType BT_IGNORAR  = new ButtonType("Não perguntar nesta versão", ButtonBar.ButtonData.NO);
                confirm.getButtonTypes().setAll(BT_ATUALIZAR, BT_DEPOIS, BT_IGNORAR);

                confirm.showAndWait().ifPresent(result -> {
                    if (result == BT_ATUALIZAR) {
                        new EstoqueController().mostrarDialogDownloadComProgresso(info);
                    } else if (result ==  BT_IGNORAR) {
                        mostrarInfoStatic("Ignorar atualização", "O programa não irá mais avisar de novas versões. " +
                                "\nAinda será disponível atualizar em Versão -> Verificar atualizações. " +
                                "\nPara reverter essa mudança, vá em Versão -> Avisar atualizações.");
                        try {
                            Misc.setNegouAtualizacao(true);
                        } catch (Exception _) {
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
                // Descobre o tamanho total do arquivo
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

        // Liga UI às propriedades da Task
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());

        dialog.setOnCloseRequest(e -> task.cancel());

        new Thread(task).start();
        dialog.showAndWait();
    }

    public void atualizarUltimaAlteracao() {
        Misc.updateTime();
        carregarUltimaAlteracao();
    }

    public void atualizarTabela(){dados.setAll(Produto.estoque);}

    public void carregarUltimaAlteracao(){ultimaAlteracao.set("Salvo em: " + Misc.getUltimaAtualizacao());}

    public void atualizarTotal(){
        Misc.atualizaTotal();
        String saldo = String.format("%.2f", Misc.getTotal());
        saldoTotal.set("Saldo total: " + "R$ " + saldo);

    }

    @FXML
    private void onCriarProduto() {
        String nome = "";
        String categoria = "";
        while (nome.isBlank()) {
            TextInputDialog dialogNome = new TextInputDialog();
            dialogNome.setTitle("Criar Produto");
            dialogNome.setHeaderText(null);
            dialogNome.setContentText("Nome do produto:");
            nome = dialogNome.showAndWait().orElse(null);
            if (nome == null) return;
            nome = nome.toUpperCase();
            if (Misc.nomes.contains(nome)) {
                String temp = "O produto " + nome + " já existe";
                mostrarErro(temp);
                return;
            }
        }

        while (categoria.isBlank()) {
            TextInputDialog dialogCategoria = new TextInputDialog();
            dialogCategoria.setTitle("Criar Produto");
            dialogCategoria.setHeaderText(null);
            dialogCategoria.setContentText("Categoria:");
            categoria = dialogCategoria.showAndWait().orElse(null);
            if (categoria == null) return;
            categoria = categoria.toUpperCase();
            Misc.addCategoria(categoria);
        }


        int qtdMin, qtd;
        double vlrUnd;
        int r = 0;

        do {
            try {
                if (r == 1){
                    mostrarInfo("Criar Produto", "Quantidade mínima deve ser maior que zero.");
                }
                TextInputDialog dMin = new TextInputDialog();
                dMin.setTitle("Criar Produto");
                dMin.setHeaderText(null);
                dMin.setContentText("Quantidade mínima:");
                String qtdMinStr;
                qtdMinStr = dMin.showAndWait().orElse("").trim();
                if (qtdMinStr.isEmpty()) return;
                qtdMin = Integer.parseInt(qtdMinStr);
                r = 1;

            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Valores numéricos inválidos.").showAndWait();
                return;
            }
        } while (qtdMin < 0);

        r = 0;

        do {
            try {
                if (r == 1){
                    mostrarInfo("Criar Produto", "Valor unitário não pode ser menor que zero.");
                }
                TextInputDialog dVlr = new TextInputDialog();
                dVlr.setTitle("Criar Produto");
                dVlr.setHeaderText(null);
                dVlr.setContentText("Valor unitário:");
                String vlrUndStr;
                vlrUndStr = dVlr.showAndWait().orElse("").replace(',', '.').trim();
                if (vlrUndStr.isEmpty()) return;
                vlrUnd = Double.parseDouble(vlrUndStr);

            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Valores numéricos inválidos.").showAndWait();
                return;
            }
            r = 1;
        } while (vlrUnd <= 0);

        r = 0;

        do {
            try {
                if (r == 1){
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
        } while  (qtd < 0);

        String tempo = Misc.getTime();

        Produto novo = new Produto(nome, qtdMin, vlrUnd, qtd, categoria, tempo);
        Produto.addEstoque(novo);
        dados.setAll(Produto.estoque);
        atualizarTotal();
    }

    @FXML
    private void onEntrada() {
        String nome = pedirProduto(Misc.nomes, "Entrada de produto");
        if (nome == null) return;
        nome = nome.trim().toUpperCase();

        TextInputDialog dialogQtd = new TextInputDialog();
        dialogQtd.setTitle("Entrada de estoque");
        dialogQtd.setHeaderText("Produto: " + nome);
        dialogQtd.setContentText("Quantidade a entrar:");
        try {
            int qtd = Integer.parseInt(dialogQtd.showAndWait().orElse("0").trim());
            Produto.entrada(qtd, nome);
            String codigo = Produto.getCodigoPorNome(nome);
            Produto p = Produto.getProdutoPorCodigo(codigo);
            if (p != null) {
                p.setAlterHora(Misc.getTime());
            }

            dados.setAll(Produto.estoque);
            atualizarTotal();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Quantidade inválida.").showAndWait();
        }
    }

    @FXML
    private void onSaida() {

        String nome = pedirProduto(Misc.nomes, "Saida de produto");
        if (nome.isEmpty()) return;
        nome = nome.toUpperCase();

        TextInputDialog dialogQtd = new TextInputDialog();
        dialogQtd.setTitle("Saída de estoque");
        dialogQtd.setHeaderText("Produto: " + nome);
        dialogQtd.setContentText("Quantidade a retirar:");
        try {
            int qtd = Integer.parseInt(dialogQtd.showAndWait().orElse("0").trim());
            Produto.saida(qtd, nome);
            String codigo = Produto.getCodigoPorNome(nome);
            Produto p = Produto.getProdutoPorCodigo(codigo);
            if (p != null) {
                p.setAlterHora(Misc.getTime());
            }
            dados.setAll(Produto.estoque);
            atualizarTotal();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Quantidade inválida.").showAndWait();
        }
    }

    @FXML
    private void onSalvar() {
        try {
            atualizarUltimaAlteracao();
            Leitor.salvarEstoque(Produto.estoque);
            Produto.setUltimaAcao("s");
            new Alert(Alert.AlertType.INFORMATION, "Estoque salvo com sucesso.").showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao salvar: " + e.getMessage()).showAndWait();
        }
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
            Window stage = tabela.getScene().getWindow();
            File arquivo = fileChooser.showSaveDialog(stage);

            if (arquivo == null) {return;}

            if (!arquivo.getName().toLowerCase().endsWith(".csv")) {
                arquivo = new File(arquivo.getAbsolutePath() + ".csv");
            }

            Leitor.exportarEstoqueParaArquivo(Produto.estoque, arquivo);

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
            alert.setContentText("Erro " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onImportarCSV(){
        try {
            Alert alert  = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Importar");
            alert.setHeaderText("ATENÇÃO");
            alert.getDialogPane().setContent( new Label("O arquivo precisa estar em uma das seguintes" +
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

            Window stage = tabela.getScene().getWindow();
            File arquivo = fileChooser.showOpenDialog(stage);

            if (arquivo == null) {return;}

            Leitor.importarCSV(arquivo);

            dados.setAll(Produto.estoque);
            tabela.refresh();

            mostrarInfo("Importar", "Importado com sucesso!");

        } catch (Exception e){
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

    @FXML
    private void onSobre(){
        String canal;
        if (AppInfo.VERSAO_CHANNEL.equalsIgnoreCase("stable")) {
            canal = "";
        } else{
            canal = "BETA";
        }
        String msg = """
        %s
        Versão: %s %s

        Autor: Arthur

        Pasta de dados:
        %s
        """.formatted(AppInfo.NOME_APP, AppInfo.VERSAO, canal, Leitor.getPath());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sobre SistemaEstoqueFX");
        alert.setHeaderText("Sobre o sistema");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void onVerificarAtualizacoes() {servicoUpdater();}

    @FXML
    private void onNovidades(){mostrarChangelog(AppInfo.novidades);}

    @FXML
    private void onVersoesAnteriores(){EstoqueAppFX.getHostServicesStatic().showDocument(AppInfo.RELEASES_URL);}

    @FXML
    private void onReportarBug() {EstoqueAppFX.getHostServicesStatic().showDocument(AppInfo.BUG_REPORT_URL);}

    @FXML
    private void onImprimir() {
        tabela.getSelectionModel().clearSelection();

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null || !job.showPrintDialog(tabela.getScene().getWindow())) {return;}

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
        if (sucesso) {job.endJob();}

        tabela.getTransforms().clear();
        tabela.setPrefHeight(Region.USE_COMPUTED_SIZE);
        tabela.setFixedCellSize(Region.USE_COMPUTED_SIZE);    }

    public void onAvisarAtualizacoes(ActionEvent actionEvent) {
        try {
            Misc.setNegouAtualizacao(false);
        } catch (IOException _) {}
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
    private void onAlterarVersao(){
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Alterar versão");
        confirm.setContentText("Para qual versão você deseja alterar?");
        ButtonType BT_ESTAVEL = new ButtonType("Estável", ButtonBar.ButtonData.YES);
        ButtonType BT_BETA  = new ButtonType("BETA", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(BT_ESTAVEL, BT_BETA);
        confirm.showAndWait().ifPresent(result -> {
            if (result == BT_ESTAVEL) {
                if (AppInfo.UPDATE_CHANNEL.equalsIgnoreCase("stable")) {
                    mostrarInfo("Alterar versão", "Você já está na versão estável.");
                } else{
                    AppInfo.setUpdateChannel("stable");
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Alterar Versão");
                    alert.setContentText("Novas atualizações buscarão a verão estável." +
                            "\nDeseja verificar atualizações?");
                    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES){onVerificarAtualizacoes();}
                    });
                }
            } else if (AppInfo.UPDATE_CHANNEL.equalsIgnoreCase("beta")) {
                mostrarInfo("Alterar versão", "Você já está na versão BETA.");
            } else{
                AppInfo.setUpdateChannel("beta");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Alterar Versão");
                alert.setContentText("Novas atualizações buscarão a verão beta." +
                        "\nDeseja verificar atualizações");
                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES){
                        onVerificarAtualizacoes();
                    }
                });
            }
        });
    }

    // onImprimir é melhor
    public void imprimir(Node node, Window owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            mostrarErro("Erro ao imprimir");
            return;
        }

        boolean ok = job.showPrintDialog(owner); // abre diálogo de impressão
        if (!ok) {
            return;
        }

        boolean sucesso = job.printPage(node);
        if (sucesso) {
            job.endJob();
        }
    }

    public void mostrarInfo(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    //Não entendo porque tem que ser static por vezes, mas é útil.
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

    public static void setStage(Stage stage) {
        EstoqueController.stage = stage;
    }
    public static Stage getStage() {
        return stage;
    }

}