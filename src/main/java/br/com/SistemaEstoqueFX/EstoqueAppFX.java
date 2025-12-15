package br.com.SistemaEstoqueFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class EstoqueAppFX extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Produto.preencher(Leitor.carregarEstoque());
        FXMLLoader fxmlLoader = new FXMLLoader(EstoqueAppFX.class.getResource("estoque-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("Sistema de Estoque (JavaFX)");
        stage.setScene(scene);
        stage.show();
    }

}
