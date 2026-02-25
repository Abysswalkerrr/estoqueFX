package com.estoquefx.controller.estoque;

import com.estoquefx.model.estoque.Movimento;
import com.estoquefx.model.estoque.Historico;
import com.estoquefx.service.estoque.MovimentoExtraService;
import com.estoquefx.service.estoque.MovimentoService;
import com.estoquefx.service.SupabaseService;
import com.estoquefx.util.I18n;
import com.estoquefx.util.SupabaseConfig;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import static com.estoquefx.controller.MainController.mostrarInfoStatic;

public class HistoricoController {

    @FXML private TableView<Movimento> tabelaMovimentacoes;
    @FXML private TableColumn<Movimento, String> colDataHora;
    @FXML private TableColumn<Movimento, String> colTipo;
    @FXML private TableColumn<Movimento, String> colCodigo;
    @FXML private TableColumn<Movimento, String> colProduto;
    @FXML private TableColumn<Movimento, String> colQtdAnterior;
    @FXML private TableColumn<Movimento, String> colQtdNova;
    @FXML private TableColumn<Movimento, String> colDiferenca;

    @FXML private ComboBox<String> comboTipo;
    @FXML private TextField txtFiltroProduto;
    @FXML private Button btnAtualizar;
    @FXML private Button btnFiltrar;
    @FXML private Button btnLimpar;

    @FXML private Label lblTotalEntradas;
    @FXML private Label lblTotalSaidas;
    @FXML private Label lblTotalMovimentacoes;
    @FXML private Label lblInfo;

    private SupabaseService supabaseService;
    private MovimentoService movimentoService;
    private MovimentoExtraService movimentoExtraService;
    private String estoqueAtualId;

    private ObservableList<Movimento> movimentacoesFiltradas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabela();
        configurarFiltros();
        configurarComboTipo();

        lblInfo.setText(I18n.t("historico.info.select"));
    }

    private void configurarTabela() {
        colDataHora.setCellValueFactory(new PropertyValueFactory<>("tempoFormatado"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoDescricao"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colQtdAnterior.setCellValueFactory(new PropertyValueFactory<>("qtdVelhaMostrar"));
        colQtdNova.setCellValueFactory(new PropertyValueFactory<>("qtdNovaMostrar"));
        colDiferenca.setCellValueFactory(new PropertyValueFactory<>("diferencaFormatada"));

        // Colorir coluna de diferença
        colDiferenca.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    if (item.startsWith("+") || item.startsWith("R$ +")) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else if (item.startsWith("-") || item.startsWith("R$ -")) {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #757575;");
                    }
                }
            }
        });

        // Colorir coluna de tipo — compara com as chaves traduzidas
        colTipo.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    if (item.equals(I18n.t("historico.tipo.entrada"))) {
                        setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    } else if (item.equals(I18n.t("historico.tipo.saida"))) {
                        setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold;");
                    } else if (item.equals(I18n.t("historico.tipo.ajuste"))) {
                        setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-font-weight: bold;");
                    } else if (item.equals(I18n.t("historico.tipo.criacao"))) {
                        setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-font-weight: bold;");
                    } else if (item.equals(I18n.t("historico.tipo.alteracao_valor"))) {
                        setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-font-weight: bold;");
                    } else if (item.equals(I18n.t("historico.tipo.alteracao_dados"))) {
                        setStyle("-fx-background-color: #E0F2F1; -fx-text-fill: #00695C; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #424242;");
                    }
                }
            }
        });

        tabelaMovimentacoes.setItems(movimentacoesFiltradas);
    }

    private void configurarFiltros() {
        txtFiltroProduto.textProperty().addListener((_, _, _) ->
                aplicarFiltros());
    }

    private void configurarComboTipo() {
        ObservableList<String> tipos = FXCollections.observableArrayList(
                I18n.t("historico.tipo.todos"),
                I18n.t("historico.tipo.entrada"),
                I18n.t("historico.tipo.saida"),
                I18n.t("historico.tipo.ajuste"),
                I18n.t("historico.tipo.criacao"),
                I18n.t("historico.tipo.alteracao_valor"),
                I18n.t("historico.tipo.alteracao_dados")
        );
        comboTipo.setItems(tipos);
        comboTipo.setValue(I18n.t("historico.tipo.todos"));

        comboTipo.valueProperty().addListener((_, _, _) ->
                aplicarFiltros());
    }

    public void setSupabaseService(SupabaseService service) {
        this.supabaseService = service;
        this.movimentoService = new MovimentoService(
                SupabaseConfig.getSupabaseUrl(),
                SupabaseConfig.getSupabaseKey()
        );
        this.movimentoService.setAuthToken(service.getAuthToken());

        this.movimentoExtraService = new MovimentoExtraService(
                SupabaseConfig.getSupabaseUrl(),
                SupabaseConfig.getSupabaseKey()
        );
        this.movimentoExtraService.setAuthToken(service.getAuthToken());
    }

    public void setEstoqueAtual(String estoqueId) {
        this.estoqueAtualId = estoqueId;
        carregarMovimentacoes();
    }

    @FXML
    private void onAtualizar() {
        carregarMovimentacoes();
    }

    @FXML
    private void onFiltrar() {
        aplicarFiltros();
    }

    @FXML
    private void onLimparFiltros() {
        comboTipo.setValue(I18n.t("historico.tipo.todos"));
        txtFiltroProduto.clear();
        aplicarFiltros();
    }

    public void carregarMovimentacoes() {
        if (estoqueAtualId == null || movimentoService == null) {
            lblInfo.setText(I18n.t("historico.info.none"));
            return;
        }

        lblInfo.setText(I18n.t("historico.loading"));
        btnAtualizar.setDisable(true);

        new Thread(() -> {
            try {
                Historico.limpar();

                movimentoService.carregarMovimentos(estoqueAtualId);
                movimentoExtraService.carregarAlteracoes(estoqueAtualId);

                Platform.runLater(() -> {
                    aplicarFiltros();
                    atualizarEstatisticas();

                    int total = Historico.getTotalMovimentacoes();
                    lblInfo.setText(MessageFormat.format(I18n.t("historico.info.total"), total));
                    btnAtualizar.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarInfoStatic(Alert.AlertType.ERROR, I18n.t("error"),
                            I18n.t("error.load.history"), e.getMessage());

                    lblInfo.setText(I18n.t("historico.info.error"));
                    btnAtualizar.setDisable(false);
                });
            }
        }).start();
    }

    private void aplicarFiltros() {
        List<Movimento> todos = Historico.getMovimentos();

        List<Movimento> filtradas = todos.stream()
                .filter(this::filtrarPorTipo)
                .filter(this::filtrarPorProduto)
                .collect(Collectors.toList());

        movimentacoesFiltradas.setAll(filtradas);

        lblInfo.setText(MessageFormat.format(
                I18n.t("historico.info.showing"),
                filtradas.size(),
                todos.size()
        ));
    }

    private boolean filtrarPorTipo(Movimento mov) {
        String tipoSelecionado = comboTipo.getValue();
        if (tipoSelecionado == null || tipoSelecionado.equals(I18n.t("historico.tipo.todos"))) {
            return true;
        }
        return mov.getTipoDescricao().equals(tipoSelecionado);
    }

    private boolean filtrarPorProduto(Movimento mov) {
        String filtro = txtFiltroProduto.getText();
        if (filtro == null || filtro.trim().isEmpty()) {
            return true;
        }

        String filtroLower = filtro.toLowerCase();
        return mov.getNome().toLowerCase().contains(filtroLower) ||
                mov.getCodigo().toLowerCase().contains(filtroLower);
    }

    private void atualizarEstatisticas() {
        List<Movimento> todos = Historico.getMovimentos();

        int totalEntradas = 0;
        int totalSaidas = 0;

        for (Movimento mov : todos) {
            String tipo = mov.getTipo().toUpperCase();

            if (tipo.equals("ENTRADA")) {
                totalEntradas++;
            } else if (tipo.equals("SAIDA")) {
                totalSaidas++;
            }
        }

        lblTotalEntradas.setText(String.valueOf(totalEntradas));
        lblTotalSaidas.setText(String.valueOf(totalSaidas));
        lblTotalMovimentacoes.setText(String.valueOf(todos.size() - (totalEntradas + totalSaidas)));
    }

    public void registrarMovimento(Movimento movimento) {
        if (estoqueAtualId == null) {
            System.err.println("⚠ estoqueId não configurado");
            return;
        }

        String tipo = movimento.getTipo().toUpperCase();

        new Thread(() -> {
            try {
                if (tipo.equals("ENTRADA") || tipo.equals("SAIDA") ||
                        tipo.equals("AJUSTE") || tipo.equals("CRIACAO")) {
                    movimentoService.salvarMovimento(movimento, estoqueAtualId);
                } else {
                    if (movimentoExtraService != null) {
                        movimentoExtraService.salvarMovimento(movimento, estoqueAtualId);
                    }
                }

                Platform.runLater(() -> {
                    aplicarFiltros();
                    atualizarEstatisticas();
                });

            } catch (Exception e) {
                System.err.println("⚠ Erro ao salvar movimento: " + e.getMessage());
                mostrarInfoStatic(Alert.AlertType.ERROR, I18n.t("error"),
                        I18n.t("error.load.history"), e.getMessage());
            }
        }).start();
    }
}
