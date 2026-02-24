package com.estoquefx.controller;

import com.estoquefx.EstoqueAppFX;
import com.estoquefx.service.SupabaseService;
import com.estoquefx.util.I18n;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

import static com.estoquefx.controller.MainController.mostrarInfoStatic;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private Button btnLogin;
    @FXML private Button btnRegistrar;
    @FXML private Label lblStatus;
    @FXML private TextField txtSenhaVisivel;
    @FXML private HBox hboxSenhaOculta;
    @FXML private HBox hboxSenhaVisivel;
    SupabaseService supabaseService;

    private boolean showingPassword = false;

    @FXML
    public void initialize() {
        supabaseService = new SupabaseService();

        // Sincroniza os textos
        txtSenhaVisivel.textProperty().bindBidirectional(txtSenha.textProperty());

        // Enter no campo senha = fazer login
        txtSenha.setOnAction(_ -> onLogin());
        txtSenhaVisivel.setOnAction(_ -> onLogin()); // também no visível
    }

    @FXML
    private void onTogglePassword() {
        showingPassword = !showingPassword;

        if (showingPassword) {
            // Mostrar HBox com senha visível
            hboxSenhaVisivel.setVisible(true);
            hboxSenhaVisivel.setManaged(true);

            // Esconder HBox com senha oculta
            hboxSenhaOculta.setVisible(false);
            hboxSenhaOculta.setManaged(false);

            // Foco no campo visível
            txtSenhaVisivel.requestFocus();
            txtSenhaVisivel.positionCaret(txtSenhaVisivel.getText().length());

        } else {
            // Mostrar HBox com senha oculta
            hboxSenhaOculta.setVisible(true);
            hboxSenhaOculta.setManaged(true);

            // Esconder HBox com senha visível
            hboxSenhaVisivel.setVisible(false);
            hboxSenhaVisivel.setManaged(false);

            // Foco no campo senha
            txtSenha.requestFocus();
            txtSenha.positionCaret(txtSenha.getText().length());
        }
    }

    @FXML
    private void onLogin() {
        String email = txtEmail.getText().trim();
        String senha = txtSenha.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            lblStatus.setText("Preencha todos os campos!");
            return;
        }

        btnLogin.setDisable(true);
        lblStatus.setText("Entrando...");
        lblStatus.setStyle("-fx-text-fill: #3498db;");

        // Feito em outra thread pra n travar a ui
        new Thread(() -> {
            try {
                boolean loginOk = supabaseService.login(email, senha);

                Platform.runLater(() -> {
                    if (loginOk) {
                        abrirSelecaoEstoque();
                    } else {
                        lblStatus.setText("Email ou senha incorretos!");
                        lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                        btnLogin.setDisable(false);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Erro: " + e.getMessage());
                    lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                    btnLogin.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void onRegistrar() {
        String email = txtEmail.getText().trim();
        String senha = txtSenha.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            lblStatus.setText("Preencha todos os campos!");
            return;
        }

        if (senha.length() < 6) {
            lblStatus.setText("Senha deve ter no mínimo 6 caracteres!");
            return;
        }

        btnRegistrar.setDisable(true);
        lblStatus.setText("Criando conta...");
        lblStatus.setStyle("-fx-text-fill: #3498db;");

        // em outra thread pra n travar
        new Thread(() -> {
            try {
                boolean registroOk = supabaseService.registrar(email, senha);

                Platform.runLater(() -> {
                    if (registroOk) {
                        lblStatus.setText("Conta criada! Fazendo login...");
                        // Fazer login automaticamente após registro
                        onLogin();
                    } else {
                        lblStatus.setText("Erro ao criar conta.");
                        lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                        btnRegistrar.setDisable(false);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Erro: " + e.getMessage());
                    lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                    btnRegistrar.setDisable(false);
                });
            }
        }).start();
    }

    private void abrirSelecaoEstoque() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    EstoqueAppFX.class.getResource("selecao-estoque-view.fxml"),
                    I18n.getBundle()
            );
            Scene scene = new Scene(loader.load(), 600, 600);

            SelecaoEstoqueController controller = loader.getController();
            controller.setSupabaseService(supabaseService);

            stage.setScene(scene);
            stage.setTitle("Selecionar Estoque");

        } catch (IOException e) {
            e.printStackTrace();
            mostrarInfoStatic(Alert.AlertType.ERROR, "Erro",
                    "Erro ao abrir tela de seleção de estoque.", e.getMessage());
            lblStatus.setText("Erro ao abrir seleção de estoque. \n" + e.getMessage());
        }
    }

}
