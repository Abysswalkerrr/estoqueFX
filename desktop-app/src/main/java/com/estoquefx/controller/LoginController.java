package com.estoquefx.controller;

import com.estoquefx.EstoqueAppFX;
import com.estoquefx.service.SupabaseService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

import static com.estoquefx.controller.estoque.EstoqueController.mostrarInfoStatic;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private Button btnLogin;
    @FXML private Button btnRegistrar;
    @FXML private Label lblStatus;
    @FXML private TextField txtSenhaVisivel;
    @FXML private Button togglePasswordButton;

    private boolean showingPassword = false;
    private SupabaseService supabaseService;

    @FXML
    public void initialize() {
        System.out.println("txtEmail: " + txtEmail);
        System.out.println("txtSenha: " + txtSenha);
        System.out.println("txtSenhaVisivel: " + txtSenhaVisivel);
        System.out.println("togglePasswordButton: " + togglePasswordButton);

        supabaseService = new SupabaseService();
        txtSenha.setOnAction(_ -> onLogin());
    }

    @FXML
    private void onTogglePassword() {
        // Faz o bind na primeira vez que clicar
        if (!txtSenhaVisivel.textProperty().isBound()) {
            txtSenhaVisivel.textProperty().bindBidirectional(txtSenha.textProperty());
        }

        showingPassword = !showingPassword;

        if (showingPassword) {
            txtSenhaVisivel.setVisible(true);
            txtSenhaVisivel.setManaged(true);

            txtSenha.setVisible(false);
            txtSenha.setManaged(false);
        } else {
            txtSenhaVisivel.setVisible(false);
            txtSenhaVisivel.setManaged(false);

            txtSenha.setVisible(true);
            txtSenha.setManaged(true);
        }

        // Manter o cursor no final
        if (showingPassword) {
            txtSenhaVisivel.positionCaret(txtSenhaVisivel.getText().length());
        } else {
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
                    EstoqueAppFX.class.getResource("selecao-estoque-view.fxml")
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
