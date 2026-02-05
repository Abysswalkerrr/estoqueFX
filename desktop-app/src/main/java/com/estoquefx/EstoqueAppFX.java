package com.estoquefx;

import com.estoquefx.controller.EstoqueController;
import com.estoquefx.model.Produto;
import com.estoquefx.data.Leitor;
import com.estoquefx.updater.core.*;

import com.estoquefx.util.Misc;
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

        /*ProdutoService.preencher(Leitor.carregarEstoque());
        EstoqueService.carregaCategorias();
        EstoqueService.carregaNomes();
        EstoqueService.atualizaTotal();
         */

        try{
            Leitor.carregarNA();
        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao carregar preferências.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }


        FXMLLoader fxmlLoader = new FXMLLoader(EstoqueAppFX.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        stage.setTitle("EstoqueFX-login");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            if ("i".equals(Produto.getUltimaAcao()) || "s".equals(Produto.getUltimaAcao())) {
                return;
            }

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sair do Sistema");
            alert.setHeaderText("Atenção!");
            alert.setContentText("Existem alterações não salvas.\n\n" +
                    "Para salvar, vá para Arquivo -> Salvar.\n" +
                    "Deseja realmente sair?");

            ButtonType btnSair = new ButtonType("Sair mesmo assim");
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnSair, btnCancelar);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() == btnCancelar) {
                event.consume(); // Não fecha
            }
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
