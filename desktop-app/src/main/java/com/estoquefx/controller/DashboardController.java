package com.estoquefx.controller;

import com.estoquefx.model.Categoria;
import com.estoquefx.model.Estoque;
import com.estoquefx.service.CategoriaService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Map;

public class DashboardController {
    @FXML
    private Label lblQtdProdutosR;
    private final StringProperty qtdProdutos = new SimpleStringProperty();

    @FXML private Label lblValorTotalR;
    private String vlr = String.format("%.2f", Estoque.getSaldo());
    private StringProperty vlrTotal = new SimpleStringProperty("R$ " + vlr);


    @FXML private Label lblQtdCategoriasR;
    private StringProperty qtdCategorias = new SimpleStringProperty();

    @FXML private Label lblQtdUrgentesR;
    private StringProperty qtdUrgentes = new SimpleStringProperty();

    @FXML private PieChart pieCategoriasR;

    @FXML private TableView<Categoria> tabelaCategoriasR;

    @FXML private TableColumn<Categoria, String> colCategoriaR;
    @FXML private TableColumn<Categoria, String> colValorR;
    ObservableList<Categoria> dadosRelatorio;

    @FXML
    private void initialize() {
        lblValorTotalR.textProperty().bind(vlrTotal);
        lblQtdProdutosR.textProperty().bind(qtdProdutos);
        lblQtdUrgentesR.textProperty().bind(qtdUrgentes);
        lblQtdCategoriasR.textProperty().bind(qtdCategorias);

        Platform.runLater(() -> {
            atualizarRelatorio();
            atualizarDashboard();
        });

        colCategoriaR.setCellValueFactory(new PropertyValueFactory<>("nome"));

        colValorR.setCellValueFactory(cellData -> {
            Categoria cat = cellData.getValue();
            String valorFormatado = String.format("R$ %.2f", cat.getValor());
            return new SimpleStringProperty(valorFormatado);
        });
        dadosRelatorio = FXCollections.observableArrayList(Categoria.getCategorias());

        tabelaCategoriasR.setItems(dadosRelatorio);



    }

    @FXML
    private void atualizarDashboard() {
        qtdProdutos.set(String.valueOf(Estoque.getProdutos().size()));

        Map<String, Double> mapa = CategoriaService.calcularValorPorCategoria();


        ObservableList<PieChart.Data> dadosChart = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> e : mapa.entrySet()) {
            dadosChart.add(new PieChart.Data(
                    e.getKey() + " (R$ " + String.format("%.2f", e.getValue()) + ")",
                    e.getValue()
            ));
        }
        pieCategoriasR.setData(dadosChart);
    }

    private void atualizarRelatorio() {
        dadosRelatorio.setAll(Categoria.getCategorias());
        tabelaCategoriasR.refresh();

        ObservableList<PieChart.Data> dadosChart = FXCollections.observableArrayList();
        for (Categoria cat : Categoria.getCategorias()) {
            dadosChart.add(new PieChart.Data(
                    cat.getNome() + " (R$ " + String.format("%.2f", cat.getValor()) + ")", cat.getValor()
            ));
        }
        pieCategoriasR.setData(dadosChart);

        qtdCategorias.set(String.valueOf(Categoria.getCategorias().size()));
    }


}
