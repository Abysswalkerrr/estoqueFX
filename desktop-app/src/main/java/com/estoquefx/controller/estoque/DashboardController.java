package com.estoquefx.controller.estoque;

import com.estoquefx.model.estoque.Categoria;
import com.estoquefx.model.estoque.Estoque;
import com.estoquefx.model.estoque.Produto;
import com.estoquefx.service.SupabaseService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

import static com.estoquefx.controller.MainController.mostrarInfoStatic;

public class DashboardController {

    @FXML private Label lblValorTotalR;
    @FXML private Label lblQtdProdutosR;
    @FXML private Label lblQtdCategoriasR;
    @FXML private Label lblQtdUrgentesR;

    @FXML private PieChart pieCategoriasR;
    @FXML private TableView<Categoria> tabelaCategoriasR;
    @FXML private TableColumn<Categoria, String> colCategoriaR;
    @FXML private TableColumn<Categoria, String> colValorR;

    @FXML private Button btnAtualizar;

    private SupabaseService supabaseService;
    private String estoqueAtualId;
    private String estoqueNome;

    @FXML
    public void initialize() {
        System.out.println("🎨 Inicializando DashboardController...");
        configurarTabelaCategorias();
        configurarGrafico();
    }

    private void configurarTabelaCategorias() {
        colCategoriaR.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNome())
        );

        colValorR.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("R$ %.2f", data.getValue().getValor())
                )
        );
    }

    private void configurarGrafico() {
        pieCategoriasR.setLegendVisible(true);
        pieCategoriasR.setLabelsVisible(true);
    }

    public void setSupabaseService(SupabaseService service) {
        this.supabaseService = service;
        System.out.println("✓ SupabaseService injetado no DashboardController");
    }

    public void setEstoqueAtual(String estoqueId, String estoqueNome) {
        this.estoqueAtualId = estoqueId;
        this.estoqueNome = estoqueNome;
        System.out.println("✓ Estoque definido no DashboardController: " + estoqueNome);
        atualizarDashboard();
    }

    /**
     * ✅ ADICIONAR: Método chamado pelo botão Atualizar no FXML
     */
    @FXML
    private void onAtualizar() {
        System.out.println("🔄 Atualizando dashboard manualmente...");
        btnAtualizar.setDisable(true);  // Desabilita durante atualização

        atualizarDashboard();

        // Reabilita o botão após 500ms (feedback visual)
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> btnAtualizar.setDisable(false));
        }).start();
    }

    public void atualizarDashboard() {
        Platform.runLater(() -> {
            try {
                atualizarCards();
                atualizarGrafico();
                atualizarTabelaCategorias();
                System.out.println("✓ Dashboard atualizado");
            } catch (Exception e) {
                System.err.println("⚠ Erro ao atualizar dashboard: " + e.getMessage());
                mostrarInfoStatic(Alert.AlertType.ERROR, "Erro",
                        "Erro ao atualizar dashboard", e.getMessage());
            }
        });
    }

    private void atualizarCards() {
        // Valor Total
        double saldo = Estoque.getSaldo();
        lblValorTotalR.setText(String.format("R$ %.2f", saldo));

        // Quantidade de Produtos
        int qtdProdutos = Estoque.getProdutos().size();
        lblQtdProdutosR.setText(String.valueOf(qtdProdutos));

        // Quantidade de Categorias
        int qtdCategorias = Categoria.getCategorias().size();
        lblQtdCategoriasR.setText(String.valueOf(qtdCategorias));

        // Produtos Urgentes
        long urgentes = Estoque.getProdutos().stream()
                .filter(Produto::getCompra)
                .count();
        lblQtdUrgentesR.setText(String.valueOf(urgentes));
    }

    private void atualizarGrafico() {
        ObservableList<PieChart.Data> dados = FXCollections.observableArrayList();

        for (Categoria cat : Categoria.getCategorias()) {
            if (cat.getValor() > 0) {
                String label = String.format("%s (R$ %.2f)",
                        cat.getNome(), cat.getValor());
                dados.add(new PieChart.Data(label, cat.getValor()));
            }
        }

        pieCategoriasR.setData(dados);
    }

    private void atualizarTabelaCategorias() {
        ObservableList<Categoria> lista = FXCollections.observableArrayList(
                Categoria.getCategorias()
        );
        tabelaCategoriasR.setItems(lista);
    }
}
