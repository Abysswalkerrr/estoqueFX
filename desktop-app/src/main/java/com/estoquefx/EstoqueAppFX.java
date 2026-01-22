package com.estoquefx;

import com.estoquefx.updater.core.*;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class EstoqueAppFX extends Application {
    private static HostServices hostServicesRef;

    @Override
    public void start(Stage stage) throws IOException {
        hostServicesRef = getHostServices();

        Produto.preencher(Leitor.carregarEstoque());
        Misc.carregaCategorias();
        Misc.carregaNomes();
        Misc.atualizaTotal();

        FXMLLoader fxmlLoader = new FXMLLoader(EstoqueAppFX.class.getResource("estoque-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("EstoqueFX");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            if ("i".equals(Produto.getUltimaAcao()) || "s".equals(Produto.getUltimaAcao())) {return;}

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Sair do Sistema");
            alert.setHeaderText("Existem alterações não salvas.");
            alert.setContentText("Deseja salvar o estoque antes de sair?");

            ButtonType btnSalvar = new ButtonType("Salvar");
            ButtonType btnNaoSalvar = new ButtonType("Não salvar");
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnSalvar, btnNaoSalvar, btnCancelar);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isEmpty() || result.get() == btnCancelar) {
                event.consume(); // não fecha
            } else if (result.get() == btnSalvar) {
                try {
                    Misc.updateTime();
                    Leitor.salvarEstoque(Estoque.getProdutos());
                    Produto.setUltimaAcao("i"); // salvo
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR,
                            "Erro ao salvar antes de sair: " + e.getMessage()).showAndWait();
                    event.consume(); // mantém aberto se der erro
                }
            }
            //else não salva
        });

        if (!Misc.getNegouAtualizacao()) {
            EstoqueController.verificarAtualizacaoSilenciosa();
        }

        if (UpdateService.deveReabrir()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Atualização Concluída");
                alert.setHeaderText("Sistema atualizado com sucesso!");
                alert.setContentText("O SistemaEstoqueFX foi atualizado para a versão " + AppInfo.VERSAO);
                alert.showAndWait();
            });
        }

        EstoqueController.setStage(stage);

        stage.show();

    }

    public static HostServices getHostServicesStatic() {return hostServicesRef;}

}
