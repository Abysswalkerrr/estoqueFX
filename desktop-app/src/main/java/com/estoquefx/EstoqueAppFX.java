package com.estoquefx;

import com.estoquefx.controller.MainController;
import com.estoquefx.controller.patrimonio.PatrimonioController;
import com.estoquefx.model.estoque.Produto;
import com.estoquefx.data.Leitor;
import com.estoquefx.updater.core.*;

import com.estoquefx.util.Misc;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class EstoqueAppFX extends Application {
    private static HostServices hostServicesRef;
    private static MainController controller;

    @Override
    public void start(Stage stage) throws IOException {
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/estoquefx/icons/i16.png"))),
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/estoquefx/icons/i32.png"))),
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/estoquefx/icons/i256.png")))
        );

        hostServicesRef = getHostServices();

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
            if ((!"i".equals(Produto.getUltimaAcao()) && !"s".equals(Produto.getUltimaAcao())
                    || PatrimonioController.isPatrimonioAlterado())) {
                System.out.println("🔄 Salvando antes de fechar...");
                controller.salvarSilencioso();
            }
            System.exit(0);
        });

        if (!Misc.getNegouAtualizacao()) {
            MainController.verificarAtualizacaoSilenciosa();
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

        MainController.setStage(stage);

        stage.show();

    }

    public static HostServices getHostServicesStatic() {return hostServicesRef;}

    public static void setController(MainController controller) {
        EstoqueAppFX.controller = controller;
    }

}
