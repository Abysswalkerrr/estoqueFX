package com.estoquefx;

import com.estoquefx.updater.core.UpdateInfo;
import com.estoquefx.updater.core.UpdateService;
import javafx.application.Application;
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
    @Override
    public void start(Stage stage) throws IOException {
        Produto.preencher(Leitor.carregarEstoque());
        Misc.carregaCategorias();
        Misc.carregaNomes();
        FXMLLoader fxmlLoader = new FXMLLoader(EstoqueAppFX.class.getResource("estoque-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("Sistema de Estoque (JavaFX)");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            if ("i".equals(Produto.getUltimaAcao()) || "s".equals(Produto.getUltimaAcao())) {
                return;
            }

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
                    Leitor.salvarEstoque(Produto.estoque);
                    Produto.setUltimaAcao("i"); // marcou como salvo
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR,
                            "Erro ao salvar antes de sair: " + e.getMessage()).showAndWait();
                    event.consume(); // mantém aberto se der erro
                }
            }
            //else não salva
        });
        verificarAtualizacaoSilenciosa();
        stage.show();
    }

    public static void verificarAtualizacaoSilenciosa() {
        UpdateService service = new UpdateService();

        try {
            UpdateInfo info = service.checkForUpdate();

            if (info.getVersaoRemota() == null || info.getUrlInstaller() == null || !info.hasUpdate()) {
                return;
            }

            Platform.runLater(() -> EstoqueController.mostrarDialogAtualizacao(service, info));

        } catch (Exception e) {
            System.err.println("Erro ao verificar update: " + e.getMessage());
        }
    }

}
