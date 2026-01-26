package com.estoquefx;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SelecaoEstoqueController {

    @FXML private ListView<String> listaEstoques;
    @FXML private Button btnAbrir;
    @FXML private Button btnNovo;
    @FXML private Label lblStatus;

    private SupabaseService supabaseService;
    private List<JsonObject> estoquesData;

    public void setSupabaseService(SupabaseService service) {
        this.supabaseService = service;
        carregarEstoques();
    }

    @FXML
    public void initialize() {
        // Duplo clique para abrir
        listaEstoques.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !listaEstoques.getSelectionModel().isEmpty()) {
                onAbrirEstoque();
            }
        });
    }

    private void carregarEstoques() {
        lblStatus.setText("Carregando estoques...");
        lblStatus.setStyle("-fx-text-fill: #3498db;");

        new Thread(() -> {
            try {
                estoquesData = supabaseService.listarEstoques();

                Platform.runLater(() -> {
                    listaEstoques.getItems().clear();

                    if (estoquesData.isEmpty()) {
                        lblStatus.setText("Nenhum estoque encontrado. Crie um novo!");
                        lblStatus.setStyle("-fx-text-fill: #7f8c8d;");
                    } else {
                        estoquesData.forEach(estoque -> {
                            String nome = estoque.get("nome").getAsString();
                            listaEstoques.getItems().add(nome);
                        });
                        lblStatus.setText("");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Erro ao carregar estoques: " + e.getMessage());
                    lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                });
            }
        }).start();
    }

    @FXML
    private void onNovoEstoque() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Novo Estoque");
        dialog.setHeaderText("Criar novo estoque");
        dialog.setContentText("Nome do estoque:");

        dialog.showAndWait().ifPresent(nome -> {
            if (nome.trim().isEmpty()) {
                lblStatus.setText("Nome não pode ser vazio!");
                lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            btnNovo.setDisable(true);
            lblStatus.setText("Criando estoque...");
            lblStatus.setStyle("-fx-text-fill: #3498db;");

            new Thread(() -> {
                try {
                    supabaseService.criarEstoque(nome);

                    Platform.runLater(() -> {
                        btnNovo.setDisable(false);
                        lblStatus.setText("Estoque criado com sucesso!");
                        lblStatus.setStyle("-fx-text-fill: #27ae60;");
                        carregarEstoques();
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        btnNovo.setDisable(false);
                        lblStatus.setText("Erro: " + e.getMessage());
                        lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                    });
                }
            }).start();
        });
    }

    @FXML
    private void onAbrirEstoque() {
        int selectedIndex = listaEstoques.getSelectionModel().getSelectedIndex();

        if (selectedIndex < 0) {
            lblStatus.setText("Selecione um estoque!");
            lblStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        JsonObject estoqueSelecionado = estoquesData.get(selectedIndex);
        String estoqueId = estoqueSelecionado.get("id").getAsString();
        String estoqueNome = estoqueSelecionado.get("nome").getAsString();

        btnAbrir.setDisable(true);
        lblStatus.setText("Carregando estoque...");
        lblStatus.setStyle("-fx-text-fill: #3498db;");

        new Thread(() -> {
            try {
                // Carregar produtos do estoque
                List<Produto> produtos = supabaseService.carregarProdutos(estoqueId);

                Platform.runLater(() -> {
                    try {
                        abrirTelaEstoque(estoqueId, estoqueNome, produtos);
                    } catch (IOException e) {
                        lblStatus.setText("Erro ao abrir tela: " + e.getMessage());
                        lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                        btnAbrir.setDisable(false);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Erro: " + e.getMessage());
                    lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                    btnAbrir.setDisable(false);
                });
            }
        }).start();
    }

    private void abrirTelaEstoque(String estoqueId, String estoqueNome, List<Produto> produtos) throws IOException {
        // Preencher dados locais
        Estoque.getProdutos().clear();
        produtos.forEach(p -> {
            Produto.addEstoque(p);
            Categoria.addCategoria(p.getCategoria());
            Categoria.addProduto(p.getCategoria(), p);
        });
        Misc.carregaNomes();
        Misc.atualizaTotal();

        // Abrir tela principal
        Stage stage = (Stage) btnAbrir.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(
                EstoqueAppFX.class.getResource("estoque-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 1000, 600);

        EstoqueController controller = loader.getController();
        controller.setEstoqueAtual(estoqueId, estoqueNome, supabaseService);

        stage.setScene(scene);
        stage.setTitle("EstoqueFX - " + estoqueNome);
    }
}
