package com.estoquefx.controller.patrimonio;

import com.estoquefx.model.patrimonio.Patrimonio;
import com.estoquefx.util.Time;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class PatrimonioController {

    @FXML private TableView<Patrimonio> tabela;
    @FXML private TableColumn<Patrimonio, String> colCodigo;
    @FXML private TableColumn<Patrimonio, String> colNome;
    @FXML private TableColumn<Patrimonio, String> colLocalizacao;
    @FXML private TableColumn<Patrimonio, String> colEstado;
    @FXML private TableColumn<Patrimonio, String> colDescricao;
    @FXML private TableColumn<Patrimonio, String> colHora;
    @FXML private TextField txtBusca;
    @FXML private Label lblResultados;

    private ObservableList<Patrimonio> dados;
    private FilteredList<Patrimonio> filtrados;

    @FXML
    public void initialize() {
        // Configurar colunas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colLocalizacao.setCellValueFactory(new PropertyValueFactory<>("localizacao"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("alterHora"));

        tabela.setEditable(true);
        colNome.setCellFactory(TextFieldTableCell.forTableColumn());
        colNome.setOnEditCommit(e -> {
            e.getRowValue().setNome(e.getNewValue().trim().toUpperCase());
            tabela.refresh();
        });
        colLocalizacao.setCellFactory(TextFieldTableCell.forTableColumn());
        colLocalizacao.setOnEditCommit(e -> {
            e.getRowValue().setLocalizacao(e.getNewValue().trim());
            tabela.refresh();
        });
        colEstado.setCellFactory(ComboBoxTableCell.forTableColumn(
                "BOM", "REGULAR", "RUIM"
        ));
        colEstado.setOnEditCommit(e -> {
            e.getRowValue().setEstado(e.getNewValue());
            e.getRowValue().setAlterHora(Time.getTempoFormatado(Time.getTime(true)));
            tabela.refresh();
        });

        tabela.setRowFactory(_ -> new TableRow<>() {
            @Override
            protected void updateItem(Patrimonio item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("estado-ruim", "estado-regular");
                if (!empty && item != null) {
                    if ("RUIM".equals(item.getEstado()))
                        getStyleClass().add("estado-ruim");
                    else if ("REGULAR".equals(item.getEstado()))
                        getStyleClass().add("estado-regular");
                }
            }
        });


        dados = FXCollections.observableArrayList(Patrimonio.getPatrimonios());
        filtrados = new FilteredList<>(dados, _ -> true);

        SortedList<Patrimonio> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(ordenados);

        // Filtro busca
        txtBusca.textProperty().addListener((_, _, newVal) -> {
            String busca = newVal.toUpperCase();
            filtrados.setPredicate(p -> {
                if (busca.isBlank()) return true;
                return p.getNome().toUpperCase().contains(busca)
                        || String.valueOf(p.getCodigo()).toUpperCase().contains(busca)
                        || p.getLocalizacao().toUpperCase().contains(busca);
            });

            atualizarResultado();
        });

        Platform.runLater(this::atualizarResultado);
    }

    @FXML
    private void adicionarPatrimonio() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Novo Patrimônio");
        dialog.setHeaderText("Nome do patrimônio:");
        dialog.showAndWait().ifPresent(nome -> {
            if (!nome.isBlank()) {
                Patrimonio p = new Patrimonio(
                        nome.trim().toUpperCase(), "", "SEM LOCALIZAÇÃO",
                        "BOM", Time.getTempoFormatado(Time.getTime(true))
                );
                Patrimonio.addPatrimonio(p);
                dados.add(p);
                atualizarResultado();
            }
        });
    }

    @FXML
    private void removerPatrimonio() {
        Patrimonio selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Remover \"" + selecionado.getNome() + "\"?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.YES) {
                    Patrimonio.removePatrimonio(selecionado);
                    dados.remove(selecionado);
                    atualizarResultado();
                }
            });
        }
    }

    private void atualizarResultado() {
        lblResultados.setText("Mostrando " + filtrados.size()
                + " de " + dados.size() + " patrimônios");
    }

    public void refresh() {
        dados.setAll(Patrimonio.getPatrimonios());
        atualizarResultado();
        tabela.refresh();
    }
}