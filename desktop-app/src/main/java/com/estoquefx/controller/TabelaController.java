package com.estoquefx.controller;

import com.estoquefx.model.*;
import com.estoquefx.service.EstoqueService;
import com.estoquefx.service.ProdutoService;
import com.estoquefx.service.SupabaseService;
import com.estoquefx.util.Time;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.controlsfx.control.textfield.TextFields;

import java.util.HashSet;

public class TabelaController {

    // ========== FXML ELEMENTS ==========

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

    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> boxCategorias;
    @FXML private CheckBox boxApenasUrgente;

    @FXML private Label lblUltimaAlteracao;
    @FXML private Label lblSaldoTotal;
    @FXML private Label lblResultados;

    // ========== PROPRIEDADES ==========

    private final StringProperty ultimaAlteracao = new SimpleStringProperty("Salvo em: ");
    private StringProperty saldoTotal = new SimpleStringProperty();
    private StringProperty resultado = new SimpleStringProperty();

    private ObservableList<Produto> dados;
    private FilteredList<Produto> filtrados;

    private boolean urgente = false;
    private String busca = "";
    private HashSet<Produto> urgentes = new HashSet<>();

    // ========== CALLBACKS ==========

    private HistoricoController historicoController;
    private Runnable onDataChanged;
    private Runnable onUltimaAlteracaoChanged;


    @FXML
    public void initialize() {
        System.out.println("📋 Inicializando TabelaController...");

        // Bind labels
        lblUltimaAlteracao.textProperty().bind(ultimaAlteracao);
        lblSaldoTotal.textProperty().bind(saldoTotal);
        lblResultados.textProperty().bind(resultado);

        // Configurar tabela
        configurarColunas();
        configurarEdicao();
        configurarEstilo();
        configurarFiltros();

        // Carregar dados
        dados = FXCollections.observableArrayList(Estoque.getProdutos());
        filtrados = new FilteredList<>(dados, _ -> true);

        SortedList<Produto> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(ordenados);

        // Listeners
        dados.addListener((ListChangeListener<Produto>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    atualizarResultado();
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
            atualizarResultado();
            contaUrgentes();
            atualizarTotal();
            carregarCategorias();
            tabela.getScene().getRoot().requestFocus();
        });
    }

    // ========== CONFIGURAÇÃO DE COLUNAS ==========

    private void configurarColunas() {
        tabela.setEditable(true);

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colQtdMin.setCellValueFactory(new PropertyValueFactory<>("vlrMin"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("qtd"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("alterHora"));

        colOrientacao.setCellValueFactory(cellData -> {
            Produto p = cellData.getValue();
            return new SimpleStringProperty(p.getCompra() ? "Compra urgente" : "Estoque suficiente");
        });

        colSaldo.setCellValueFactory(cellData -> {
            Produto p = cellData.getValue();
            double saldo = p.getVlrUnd() * p.getQtd();
            return new SimpleStringProperty(String.format("R$ %.2f", saldo));
        });

        colValorUnd.setCellValueFactory(cellData -> {
            Produto p = cellData.getValue();
            return new SimpleStringProperty(String.format("R$ %.2f", p.getVlrUnd()));
        });

        // Configurar larguras
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

        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ========== CONFIGURAÇÃO DE EDIÇÃO ==========

    private void configurarEdicao() {
        configurarEdicaoNome();
        configurarEdicaoCategoria();
        configurarEdicaoQtdMin();
        configurarEdicaoQtd();
        configurarEdicaoValor();
        configurarEdicaoDescricao();
    }

    private void configurarEdicaoNome() {
        colNome.setCellFactory(TextFieldTableCell.forTableColumn());
        colNome.setOnEditCommit(event -> {
            Produto p = event.getRowValue();
            String novoNome = event.getNewValue();
            if (novoNome == null || novoNome.isBlank()) return;
            novoNome = novoNome.trim().toUpperCase();

            if (!novoNome.equals(p.getNome())) {
                p.setNome(novoNome);
                notificarMudanca();
            }

            tabela.refresh();
        });
    }

    private void configurarEdicaoCategoria() {
        colCategoria.setCellFactory(TextFieldTableCell.forTableColumn());
        colCategoria.setOnEditCommit(event -> {
            Produto p = event.getRowValue();
            String novaCat = event.getNewValue();
            if (novaCat == null || novaCat.isBlank()) return;

            String c = p.getCategoria();
            if (c.equals(novaCat)) return;

            Categoria categoria = Categoria.getCategoria(c);
            if (categoria.getProdutos().size() > 1) {
                categoria.removeProduto(p);
            }

            novaCat = novaCat.trim().toUpperCase();
            p.setCategoria(novaCat);
            Categoria.addCategoria(novaCat);
            Categoria.addProduto(novaCat, p);

            carregarCategorias();
            notificarMudanca();
            tabela.refresh();
        });
    }

    private void configurarEdicaoQtdMin() {
        colQtdMin.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQtdMin.setOnEditCommit(event -> {
            Produto p = event.getRowValue();
            try {
                int novoMin = Integer.parseInt(event.getNewValue().toString().trim());
                int velhoMin = p.getVlrMin();
                p.setVlrMin(novoMin);
                p.setAlterHora(Time.getTime());

                if (novoMin != velhoMin && historicoController != null) {
                    int diff = novoMin - velhoMin;
                    Movimento mov = new Movimento(p, "AJUSTE", diff);
                    historicoController.registrarMovimento(mov);
                }

                boolean a = p.getCompra();
                p.atualizaCompra();
                if (a != p.getCompra()) {
                    atualizaUrgentes(p);
                }

                notificarAlteracao();
                tabela.refresh();
            } catch (NumberFormatException ex) {
                // Valor inválido, ignora
            }
        });
    }

    private void configurarEdicaoQtd() {
        colQtd.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQtd.setOnEditCommit(event -> {
            Produto p = event.getRowValue();
            try {
                int novaQtd = Integer.parseInt(event.getNewValue().toString().trim());
                int qtdAntiga = p.getQtd();
                p.setQtd(novaQtd);

                if (novaQtd != qtdAntiga && historicoController != null) {
                    int diff = novaQtd - qtdAntiga;
                    String tipoMov = diff > 0 ? "ENTRADA" : "SAIDA";
                    Movimento mov = new Movimento(p, tipoMov, diff);
                    historicoController.registrarMovimento(mov);
                }

                boolean a = p.getCompra();
                p.atualizaCompra();
                if (a != p.getCompra()) {
                    atualizaUrgentes(p);
                }

                p.setAlterHora(Time.getTime());
                atualizarTotal();
                notificarAlteracao();
                tabela.refresh();
            } catch (NumberFormatException ex) {
                // Valor inválido, ignora
            }
        });
    }

    private void configurarEdicaoValor() {
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
            double oldValor = p.getVlrUnd();
            String texto = event.getNewValue();
            try {
                texto = texto.replace("R$", "");
                double novoVlr = Double.parseDouble(texto.replace(',', '.').trim());
                p.setVlrUnd(novoVlr);
                p.setAlterHora(Time.getTime());

                if (novoVlr != oldValor && historicoController != null) {
                    double delta = novoVlr - oldValor;
                    Movimento mov = new Movimento(p, "ALTERACAO_VALOR", delta);
                    historicoController.registrarMovimento(mov);
                }

                atualizarTotal();
                notificarAlteracao();
                tabela.refresh();
            } catch (NumberFormatException ex) {
                // Valor inválido, ignora
            }
        });
    }

    private void configurarEdicaoDescricao() {
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
    }

    // ========== CONFIGURAÇÃO DE ESTILO ==========

    private void configurarEstilo() {
        tabela.setRowFactory(_ -> new TableRow<>() {
            @Override
            protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    getStyleClass().remove("estoque-baixo");
                } else if (item.getCompra()) {
                    getStyleClass().add("estoque-baixo");
                } else {
                    setStyle("");
                    getStyleClass().remove("estoque-baixo");
                }
            }
        });
    }

    // ========== CONFIGURAÇÃO DE FILTROS ==========

    private void configurarFiltros() {
        boxCategorias.valueProperty().addListener((_, _, _) -> {
            atualizarFiltro();
            atualizarResultado();
        });

        boxApenasUrgente.selectedProperty().addListener((_, _, _) -> {
            urgente = boxApenasUrgente.isSelected();
            atualizarFiltro();
            atualizarResultado();
        });

        txtBusca.textProperty().addListener((_, _, newValue) -> {
            busca = newValue.toUpperCase();
            atualizarFiltro();
            atualizarResultado();
        });
    }

    private void atualizarFiltro() {
        String categoria = boxCategorias.getValue();
        if (categoria == null || categoria.isEmpty()) {
            categoria = "";
        }
        String finalCategoria = categoria.toUpperCase();

        filtrados.setPredicate(produto -> {
            if (urgente && !produto.getCompra()) return false;
            if (busca.isEmpty() && finalCategoria.isEmpty()) return true;

            String nome = produto.getNome() == null ? "" : produto.getNome().toUpperCase();
            String cat = produto.getCategoria() == null ? "" : produto.getCategoria().toUpperCase();
            String cod = produto.getCodigo() == null ? "" : produto.getCodigo().toUpperCase();

            boolean categoriaVazia = finalCategoria.isEmpty() || cat.contains(finalCategoria);

            if (busca.equalsIgnoreCase("urgente")) {
                return categoriaVazia && nome.contains(busca);
            }

            return categoriaVazia && (nome.contains(busca) || cod.contains(busca));
        });
    }

    // ========== DIALOGS ==========

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
            notificarMudanca();
            tabela.refresh();
        });
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
            alert.setHeaderText("Erro: " + ex.getMessage());
            alert.showAndWait();
            return "";
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    public void carregarCategorias() {
        ObservableList<String> categorias = FXCollections.observableArrayList();
        for (Categoria categoria : Categoria.getCategorias()) {
            categorias.add(categoria.getNome());
        }
        boxCategorias.setItems(categorias);
    }

    public void contaUrgentes() {
        urgentes.clear();
        for (Produto produto : Estoque.getProdutos()) {
            if (produto.getCompra()) {
                urgentes.add(produto);
            }
        }
    }

    public void atualizaUrgentes(Produto produto) {
        if (produto.getCompra()) {
            urgentes.add(produto);
        } else {
            urgentes.remove(produto);
        }
    }

    public void atualizarTotal() {
        EstoqueService.atualizaTotal();
        String saldo = String.format("%.2f", Estoque.getSaldo());
        saldoTotal.set("Saldo total: R$ " + saldo);
    }

    public void atualizarResultado() {
        resultado.set("Mostrando " + filtrados.size() + " de " + dados.size() + " produtos");
    }

    public void setUltimaAlteracao(String texto) {
        ultimaAlteracao.set(texto);
    }

    // ========== MÉTODOS PÚBLICOS ==========

    public void refresh() {
        dados.setAll(Estoque.getProdutos());
        atualizarTotal();
        atualizarResultado();
        carregarCategorias();
        contaUrgentes();
        tabela.refresh();
    }

    public void setHistoricoController(HistoricoController historicoController) {
        this.historicoController = historicoController;
        System.out.println("✓ HistoricoController conectado ao TabelaController");
    }

    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    public void setOnUltimaAlteracaoChanged(Runnable callback) {
        this.onUltimaAlteracaoChanged = callback;
    }

    private void notificarMudanca() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private void notificarAlteracao() {
        if (onUltimaAlteracaoChanged != null) {
            onUltimaAlteracaoChanged.run();
        }
        notificarMudanca();
    }

    public TableView<Produto> getTabela() {
        return tabela;
    }
}
