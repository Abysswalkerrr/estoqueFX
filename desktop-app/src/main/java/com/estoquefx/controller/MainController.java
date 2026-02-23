package com.estoquefx.controller;

import com.estoquefx.EstoqueAppFX;
import com.estoquefx.controller.estoque.DashboardController;
import com.estoquefx.controller.estoque.HistoricoController;
import com.estoquefx.controller.estoque.MenuController;
import com.estoquefx.controller.estoque.TabelaController;
import com.estoquefx.controller.patrimonio.PatrimonioController;
import com.estoquefx.data.Leitor;
import com.estoquefx.model.estoque.Estoque;
import com.estoquefx.model.estoque.Produto;
import com.estoquefx.model.patrimonio.Patrimonio;
import com.estoquefx.service.*;
import com.estoquefx.service.patrimonio.PatrimonioService;
import com.estoquefx.updater.core.*;
import com.estoquefx.util.Misc;
import com.estoquefx.util.Time;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;


public class MainController {

    // SUB-CONTROLLERS

    @FXML private Tab tabTabela;

    @FXML private TabelaController tabelaViewController;
    @FXML private MenuController menuViewController;

    @FXML private Tab tabHistorico;
    private HistoricoController historicoController;

    @FXML private Tab tabDashboard;
    private DashboardController dashboardController;

    @FXML private Tab tabPatrimonio;
    private PatrimonioController patrimonioController;

    private SupabaseService supabaseService;
    private String estoqueId;
    private static Stage stage;

    @FXML
    public void initialize() {
        System.out.println("🎬 Inicializando EstoqueController principal...");


        carregarHistoricoController();
        carregarDashboardController();
        carregarPatrimonioController();

        Platform.runLater(() -> {
            conectarControllers();
            Produto.setUltimaAcao("s");
            setEstoqueAppController();
        });
    }

    // SUB-CONTROLLERS

    private void carregarHistoricoController() {
        try {
            Object includeContent = tabHistorico.getContent();

            if (includeContent != null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/estoquefx/estoque/historico-view.fxml")
                );
                VBox historicoView = loader.load();
                historicoController = loader.getController();
                tabHistorico.setContent(historicoView);

                System.out.println("✓ HistoricoController carregado");
            }
        } catch (Exception e) {
            System.err.println("⚠ Erro ao carregar HistoricoController: " + e.getMessage());
            mostrarInfoStatic(Alert.AlertType.ERROR, "Erro",
                    "Erro ao carregar histórico.", e.getMessage());
        }
    }

    private void carregarDashboardController() {
        try {
            Object includeContent = tabDashboard.getContent();

            if (includeContent != null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/estoquefx/estoque/dashboard-view.fxml")
                );
                AnchorPane dashboardView = loader.load();
                dashboardController = loader.getController();
                tabDashboard.setContent(dashboardView);

                System.out.println("✓ DashboardController carregado");
            }
        } catch (Exception e) {
            System.err.println("⚠ Erro ao carregar DashboardController: " + e.getMessage());
            mostrarInfoStatic(Alert.AlertType.ERROR, "Erro",
                    "Erro ao carregar dashboard.", e.getMessage());
        }
    }

    private void carregarPatrimonioController() {
        try {
            Object includeContent = tabPatrimonio.getContent();
            if (includeContent != null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/estoquefx/patrimonio/patrimonio-view.fxml")
                );
                BorderPane patrimonioView = loader.load();
                patrimonioController = loader.getController();
                tabPatrimonio.setContent(patrimonioView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // CONEXÃO ENTRE CONTROLLERS

    private void conectarControllers() {

        if (tabelaViewController != null && historicoController != null) {
            tabelaViewController.setHistoricoController(historicoController);
        }
        if (tabelaViewController != null){
            tabelaViewController.setOnUltimaAlteracaoChanged(this::atualizarUltimaAlteracao);
        }
        if (menuViewController != null && historicoController != null) {
            menuViewController.setHistoricoController(historicoController);
        }
        if  (menuViewController != null && tabelaViewController != null) {
            menuViewController.setTabelaController(tabelaViewController);
            tabelaViewController.setMenuController(menuViewController);
        }

        System.out.println("✓ Controllers conectados");
    }

    // CONFIGURAÇÃO DE ESTOQUE

    public void setEstoqueAtual(String estoqueId, String estoqueNome, SupabaseService service) {
        this.estoqueId = estoqueId;
        this.supabaseService = service;
        Leitor.setNomeEstoque(estoqueNome);

        try {
            Leitor.carregarMisc();
        } catch (Exception e) {
            mostrarInfoStatic(Alert.AlertType.ERROR, "Erro",
                    "Erro ao carregar preferências.", e.getMessage());
        }

        // Inicializar sub-controllers ANTES de atualizar tabela
        if (historicoController != null) {
            historicoController.setSupabaseService(service);
            historicoController.setEstoqueAtual(estoqueId);
        }

        if (dashboardController != null) {
            dashboardController.setSupabaseService(service);
            dashboardController.setEstoqueAtual(estoqueId, estoqueNome);
        }

        if (menuViewController != null) {
            menuViewController.setSupabaseService(service, estoqueId);
        }

        if (tabelaViewController != null) {
            Platform.runLater(() -> {
                System.out.println("📊 Atualizando tabela com " + Estoque.getProdutos().size() + " produtos");
                tabelaViewController.refresh();
                tabelaViewController.carregarUltimaAlteracao();
            });
        }

        if (patrimonioController != null) {
            patrimonioController.setSupabaseService(service,  estoqueId);
            new Thread(() -> {
                try {
                    PatrimonioService ps = new PatrimonioService(service);
                    List<Patrimonio> lista = ps.carregarPatrimonios(estoqueId);
                    lista.forEach(Patrimonio::addPatrimonio);
                    Platform.runLater(() -> patrimonioController.refresh());
                    System.out.println("✓ Patrimônios carregados: " + lista.size());
                } catch (Exception e) {
                    System.err.println("Erro ao carregar patrimônios: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // MÉTODOS AUXILIARES

    private void atualizarUltimaAlteracao() {
        Time.updateTime();
        if (tabelaViewController != null) {
            tabelaViewController.carregarUltimaAlteracao();
        }
    }

    // VERIFICAR ATT SILENCIOSA

    public static void verificarAtualizacaoSilenciosa() {
        UpdateService service = new UpdateService();

        try {
            UpdateInfo info = service.verificarUpdate();

            if (info.getVersaoRemota() == null || info.getUrlInstaller() == null || !info.hasUpdate()) {
                return;
            }

            Platform.runLater(() -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Atualização disponível");
                confirm.setHeaderText("Versão atual: " + info.getVersaoAtual() +
                        "\nNova versão: " + info.getVersaoRemota());
                confirm.setContentText("Novidades: " + info.getChangeLog() +
                        "\nDeseja baixar agora?");

                ButtonType BT_ATUALIZAR = new ButtonType("Atualizar agora", ButtonBar.ButtonData.YES);
                ButtonType BT_DEPOIS = new ButtonType("Lembrar depois", ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType BT_IGNORAR = new ButtonType("Não perguntar nesta versão", ButtonBar.ButtonData.NO);
                confirm.getButtonTypes().setAll(BT_ATUALIZAR, BT_DEPOIS, BT_IGNORAR);

                confirm.showAndWait().ifPresent(result -> {
                    if (result == BT_ATUALIZAR) {
                        new MenuController().mostrarDialogDownloadComProgresso(info);
                    } else if (result == BT_IGNORAR) {
                        mostrarInfoStatic(Alert.AlertType.INFORMATION, "Ignorar atualização", null,
                                """
                                        O programa não irá mais avisar de novas versões. \
                                        
                                        Ainda será disponível atualizar em Versão -> Verificar atualizações. \
                                        
                                        Para reverter essa mudança, vá em Versão -> Avisar atualizações.""");
                        try {
                            Misc.setNegouAtualizacao(true);
                        } catch (Exception ignored) {
                        }
                    }
                });
            });

        } catch (Exception e) {
            System.err.println("Erro ao verificar update: " + e.getMessage());
        }
    }

    // DIALOGS(top 10 funções de todo o código)
    public static void mostrarInfoStatic(Alert.AlertType tipo, String title, String header, String message) {
        Alert alert = new Alert(tipo);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // SALVAR SHHHHHH
    public void salvarSilencioso(){
        menuViewController.salvarSilenciosamente();
        if (patrimonioController != null) {
            patrimonioController.salvarNuvem();
        } else {
            System.out.println("Patrimonio controller é null, não salvo");
        }
    }

    // SETTERS
    public void setHistoricoController(HistoricoController historicoController) {
        this.historicoController = historicoController;
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public static void setStage(Stage stage) {
        MainController.stage = stage;
    }

    public void setEstoqueAppController(){
        EstoqueAppFX.setController(this);
    }
}
