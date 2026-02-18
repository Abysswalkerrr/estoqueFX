package com.estoquefx.controller.patrimonio;

import com.estoquefx.model.estoque.Estoque;
import com.estoquefx.model.patrimonio.Patrimonio;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class PatrimonioController {
    @FXML private TableView<Patrimonio> tabela;
    @FXML private TableColumn<Patrimonio, Integer> colCodigo;
    @FXML private TableColumn<Patrimonio, String> colNome;

    private ObservableList<Patrimonio> dados;


    @FXML
    public void initialize(){
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        dados = FXCollections.observableArrayList(Patrimonio.getPatrimonios());
        dados.addListener((ListChangeListener<Patrimonio>) change-> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    refresh();
                }
            }
        });
    }


    public void refresh() {
        dados.setAll(Patrimonio.getPatrimonios());
    }
}
