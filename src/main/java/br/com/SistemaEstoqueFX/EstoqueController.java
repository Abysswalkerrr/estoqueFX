package br.com.SistemaEstoqueFX;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Text;
import javafx.util.converter.IntegerStringConverter;

import javafx.util.StringConverter;
import java.io.IOException;


public class EstoqueController {

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

    @FXML private Button btnCriar;
    @FXML private Button btnEntrada;
    @FXML private Button btnSaida;
    @FXML private Button btnSalvar;
    @FXML private Button btnExportar;

    private ObservableList<Produto> dados;
    private FilteredList<Produto> filtrados;

    @FXML private TextField txtBusca;


    @FXML
    public void initialize() {
        tabela.setEditable(true);
        // liga colunas aos getters de Produto
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colQtdMin.setCellValueFactory(new PropertyValueFactory<>("vlrMin"));
        colValorUnd.setCellValueFactory(new PropertyValueFactory<>("vlrUnd"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("qtd"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        colOrientacao.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        Misc.isUrgente(cellData.getValue())
                )
        );
        colSaldo.setCellValueFactory(cellData -> {
            Produto p = cellData.getValue();
            double saldo = p.getVlrUnd() * p.getQtd();
            String saldoStr = String.format("%.2f", saldo);
            return new javafx.beans.property.SimpleStringProperty("R$ " + saldoStr);
        });

        colValorUnd.setCellValueFactory(cellData -> {
            Produto p = cellData.getValue();
            double vlrUnd = p.getVlrUnd();
            String vlrUndStr = String.format("%.2f", vlrUnd);
            return new javafx.beans.property.SimpleStringProperty("R$ " + vlrUndStr);
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
                Misc.atualizaTotal();
                tabela.refresh();
            } catch (NumberFormatException ex) {
                // erro inválido
            }
        });

        colValorUnd.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<String>() {
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
                double novoVlr = Double.parseDouble(texto.replace(',', '.').trim());
                p.setVlrUnd(novoVlr);
                Misc.atualizaTotal();
                tabela.refresh();
            } catch (NumberFormatException ex) {
                // erro inválido
            }
        });



        colDescricao.setCellFactory(col -> new TableCell<Produto, String>() {
            private final Text text = new Text();

            {
                setGraphic(text);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
                text.wrappingWidthProperty().bind(col.widthProperty().subtract(10));

                // Observa a seleção desta célula
                selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        text.setFill(javafx.scene.paint.Color.WHITE);
                    } else {
                        text.setFill(javafx.scene.paint.Color.BLACK);
                    }
                });

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
                if (empty || item == null) {
                    text.setText("");
                } else {
                    text.setText(item);
                }
                // garante cor inicial correta
                if (isSelected()) {
                    text.setFill(javafx.scene.paint.Color.WHITE);
                } else {
                    text.setFill(javafx.scene.paint.Color.BLACK);
                }
            }
        });

        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
        colOrientacao.setMinWidth(135);
        colOrientacao.setMaxWidth(135);
        colDescricao.setMinWidth(150);
        colDescricao.setPrefWidth(300);




        dados = FXCollections.observableArrayList(Produto.estoque);
        filtrados = new FilteredList<>(dados, p -> true);

        txtBusca.textProperty().addListener((obs, oldValue, newValue) -> {
            String filtro = (newValue == null) ? "" : newValue.trim().toUpperCase();

            filtrados.setPredicate(produto -> {
                if (filtro.isEmpty()) return true;

                String nome = produto.getNome() == null ? "" : produto.getNome().toUpperCase();
                String cat  = produto.getCategoria() == null ? "" : produto.getCategoria().toUpperCase();
                String cod  = produto.getCodigo() == null ? "" : produto.getCodigo().toUpperCase();

                return nome.contains(filtro)
                        || cat.contains(filtro)
                        || cod.contains(filtro);
            });
        });

        SortedList<Produto> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabela.comparatorProperty());

        tabela.setItems(ordenados);

        // colorir linhas urgentes
        tabela.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getCompra()) {
                    setStyle("-fx-background-color: yellow;");
                } else {
                    setStyle("");
                }
            }
        });
    }

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
            if (novaDesc == null) novaDesc = "";
            novaDesc = novaDesc.trim();
            p.setDescricao(novaDesc);
            tabela.refresh();
        });
    }

    @FXML
    private void onCriarProduto() {
        String nome = "";
        String categoria = "";
        String descricao = "";

        while (nome.isBlank()) {
            TextInputDialog dialogNome = new TextInputDialog();
            dialogNome.setTitle("Criar Produto");
            dialogNome.setHeaderText(null);
            dialogNome.setContentText("Nome do produto:");
            nome = dialogNome.showAndWait().orElse(null);
            if (nome == null) return;
            nome = nome.toUpperCase();
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

        try {
            TextInputDialog dMin = new TextInputDialog();
            dMin.setTitle("Criar Produto");
            dMin.setHeaderText(null);
            dMin.setContentText("Quantidade mínima:");
            qtdMin = Integer.parseInt(dMin.showAndWait().orElse("0").trim());

            TextInputDialog dVlr = new TextInputDialog();
            dVlr.setTitle("Criar Produto");
            dVlr.setHeaderText(null);
            dVlr.setContentText("Valor unitário:");
            vlrUnd = Double.parseDouble(dVlr.showAndWait().orElse("0").replace(',', '.').trim());

            TextInputDialog dQtd = new TextInputDialog();
            dQtd.setTitle("Criar Produto");
            dQtd.setHeaderText(null);
            dQtd.setContentText("Quantidade em estoque:");
            qtd = Integer.parseInt(dQtd.showAndWait().orElse("0").trim());
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Valores numéricos inválidos.").showAndWait();
            return;
        }

        Produto novo = new Produto(nome, qtdMin, vlrUnd, qtd, categoria);
        Produto.addEstoque(novo);
        dados.setAll(Produto.estoque); // atualiza TableView
    }

    @FXML
    private void onEntrada() {
        String nome = perguntarNomeProduto("Entrada de estoque");
        if (nome == null) return;
        nome = nome.trim().toUpperCase();

        TextInputDialog dialogQtd = new TextInputDialog();
        dialogQtd.setTitle("Entrada de estoque");
        dialogQtd.setHeaderText("Produto: " + nome);
        dialogQtd.setContentText("Quantidade a entrar:");
        try {
            int qtd = Integer.parseInt(dialogQtd.showAndWait().orElse("0").trim());
            Produto.entrada(qtd, nome);
            dados.setAll(Produto.estoque); // atualiza TableView
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Quantidade inválida.").showAndWait();
        }
    }

    @FXML
    private void onSaida() {
        String nome = perguntarNomeProduto("Saída de estoque");
        if (nome == null) return;
        nome = nome.toUpperCase();

        TextInputDialog dialogQtd = new TextInputDialog();
        dialogQtd.setTitle("Saída de estoque");
        dialogQtd.setHeaderText("Produto: " + nome);
        dialogQtd.setContentText("Quantidade a retirar:");
        try {
            int qtd = Integer.parseInt(dialogQtd.showAndWait().orElse("0").trim());
            Produto.saida(qtd, nome);
            dados.setAll(Produto.estoque); // atualiza TableView
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Quantidade inválida.").showAndWait();
        }
    }

    @FXML
    private void onSalvar() {
        try {
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
            Leitor.exportarEstoqueCSV(Produto.estoque);

            new Alert(Alert.AlertType.INFORMATION,
                    "CSV exportado com sucesso em:\nDocumentos/" + Leitor.nomePasta + "/estoque.csv")
                    .showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Erro ao exportar CSV: " + e.getMessage())
                    .showAndWait();
        }
    }

    @FXML
    private void onSobre(){
        String msg = """
            SistemaEstoqueFX
            Versão: 1.0.0

            Autor: Arthur

            Pasta de dados:
            %s
            """.formatted(Leitor.getPath()); // ou texto fixo

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sobre SistemaEstoqueFX");
        alert.setHeaderText("Sobre o sistema");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
