package com.estoquefx.controller;

import com.estoquefx.model.Movimento;
import com.estoquefx.model.Historico;
import com.estoquefx.service.MovimentoExtraService;
import com.estoquefx.service.MovimentoService;
import com.estoquefx.service.SupabaseService;
import com.estoquefx.util.SupabaseConfig;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

public class HistoricoController {

    @FXML
    private TableView<Movimento> tabelaMovimentacoes;
    @FXML
    private TableColumn<Movimento, String> colDataHora;
    @FXML
    private TableColumn<Movimento, String> colTipo;
    @FXML
    private TableColumn<Movimento, String> colCodigo;
    @FXML
    private TableColumn<Movimento, String> colProduto;
    @FXML
    private TableColumn<Movimento, Integer> colQtdAnterior;
    @FXML
    private TableColumn<Movimento, Integer> colQtdNova;
    @FXML
    private TableColumn<Movimento, String> colDiferenca;
    @FXML
    private TableColumn<Movimento, String> colObservacao;

    @FXML
    private ComboBox<String> comboTipo;
    @FXML
    private TextField txtFiltroProduto;
    @FXML
    private Button btnAtualizar;
    @FXML
    private Button btnFiltrar;
    @FXML
    private Button btnLimpar;

    @FXML
    private Label lblTotalEntradas;
    @FXML
    private Label lblTotalSaidas;
    @FXML
    private Label lblTotalMovimentacoes;
    @FXML
    private Label lblInfo;

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

        lblInfo.setText("Selecione um estoque para ver o histórico");
    }

    private void configurarTabela() {
        // Configurar colunas
        colDataHora.setCellValueFactory(new PropertyValueFactory<>("tempoFormatado"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoDescricao"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colQtdAnterior.setCellValueFactory(new PropertyValueFactory<>("quantidadeAnterior"));
        colQtdNova.setCellValueFactory(new PropertyValueFactory<>("quantidadeNova"));
        colDiferenca.setCellValueFactory(new PropertyValueFactory<>("diferencaFormatada"));
        colObservacao.setCellValueFactory(new PropertyValueFactory<>("observacao"));

        // Colorir coluna de diferença
        colDiferenca.setCellFactory(col -> new TableCell<Movimento, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    if (item.startsWith("+")) {
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else if (item.startsWith("-")) {
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #757575;");
                    }
                }
            }
        });

        // Colorir coluna de tipo
        colTipo.setCellFactory(col -> new TableCell<Movimento, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    switch (item) {
                        case "Entrada":
                            setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                            break;
                        case "Saída":
                            setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold;");
                            break;
                        case "Ajuste":
                            setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-font-weight: bold;");
                            break;
                        case "Criação":
                            setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-font-weight: bold;");
                            break;
                        case "Alteração Valor":
                            setStyle("-fx-background-color: #F3E5F5; -fx-text-fill: #7B1FA2; -fx-font-weight: bold;");
                            break;
                        case "Alteração Dados":
                            setStyle("-fx-background-color: #E0F2F1; -fx-text-fill: #00695C; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #424242;");
                    }
                }
            }
        });

        tabelaMovimentacoes.setItems(movimentacoesFiltradas);
    }

    private void configurarFiltros() {
        // Filtro automático ao digitar
        txtFiltroProduto.textProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltros();
        });
    }

    private void configurarComboTipo() {
        ObservableList<String> tipos = FXCollections.observableArrayList(
                "Todos",
                "Entrada",
                "Saída",
                "Ajuste",
                "Criação",
                "Alteração Valor",
                "Alteração Dados"
        );
        comboTipo.setItems(tipos);
        comboTipo.setValue("Todos");

        // Filtrar ao mudar tipo
        comboTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            aplicarFiltros();
        });
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
        comboTipo.setValue("Todos");
        txtFiltroProduto.clear();
        aplicarFiltros();
    }

    private void carregarMovimentacoes() {
        if (estoqueAtualId == null || movimentoService == null) {
            lblInfo.setText("Nenhum estoque selecionado");
            return;
        }

        lblInfo.setText("Carregando movimentações...");
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
                    lblInfo.setText(String.format("Total: %d movimentações", total));
                    btnAtualizar.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Erro ao carregar movimentações");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();

                    lblInfo.setText("Erro ao carregar movimentações");
                    btnAtualizar.setDisable(false);
                });
                e.printStackTrace();
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

        lblInfo.setText(String.format(
                "Mostrando %d de %d movimentações",
                filtradas.size(),
                todos.size()
        ));
    }

    private boolean filtrarPorTipo(Movimento mov) {
        String tipoSelecionado = comboTipo.getValue();
        if (tipoSelecionado == null || tipoSelecionado.equals("Todos")) {
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
        lblTotalMovimentacoes.setText(String.valueOf(todos.size()));
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
                    // quantidade
                    movimentoService.salvarMovimento(movimento, estoqueAtualId);
                } else {
                    // valor/dados
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
                e.printStackTrace();
            }
        }).start();
    }
}
