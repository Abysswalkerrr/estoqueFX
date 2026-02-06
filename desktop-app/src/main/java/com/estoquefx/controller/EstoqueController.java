package com.estoquefx.controller;

import com.estoquefx.data.Leitor;
import com.estoquefx.model.*;
import com.estoquefx.service.*;
import com.estoquefx.updater.core.*;
import com.estoquefx.util.Misc;
import com.estoquefx.util.Time;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

/**
 * Controller principal do sistema de estoque
 * Coordena os subcontrollers (Tabela, Dashboard, Histórico)
 * Gerencia ações do menu e navegação
 */
public class EstoqueController {

    // ========== SUB-CONTROLLERS ==========

    @FXML private Tab tabTabela;

    private TabelaController tabelaController;
    private MenuController menuController;

    @FXML private Tab tabHistorico;
    private HistoricoController historicoController;

    @FXML private Tab tabDashboard;
    private DashboardController dashboardController;

    // ========== VARIÁVEIS DE ESTADO ==========

    private SupabaseService supabaseService;
    private String estoqueId;
    private static Stage stage;

    // ========== INICIALIZAÇÃO ==========

    @FXML
    public void initialize() {
        System.out.println("🎬 Inicializando EstoqueController principal...");

        // Carregar sub-controllers
        carregarTabelaController();
        carregarMenuController();
        carregarHistoricoController();
        carregarDashboardController();

        // Conectar controllers
        conectarControllers();

        Platform.runLater(() -> {
            Produto.setUltimaAcao("s");
        });
    }

    // ========== CARREGAMENTO DE SUB-CONTROLLERS ==========

    private void carregarMenuController(){
        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/MenuController.fxml")
            );
            AnchorPane menuView = loader.load();
            menuController = loader.getController();
            tabTabela.setContent(menuView);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarTabelaController() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/estoquefx/tabela-view.fxml")
            );
            VBox tabelaView = loader.load();
            tabelaController = loader.getController();
            tabTabela.setContent(tabelaView);

            System.out.println("✓ TabelaController carregado");
        } catch (Exception e) {
            System.err.println("⚠ Erro ao carregar TabelaController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarHistoricoController() {
        try {
            Object includeContent = tabHistorico.getContent();

            if (includeContent != null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/estoquefx/historico-view.fxml")
                );
                VBox historicoView = loader.load();
                historicoController = loader.getController();
                tabHistorico.setContent(historicoView);

                System.out.println("✓ HistoricoController carregado");
            }
        } catch (Exception e) {
            System.err.println("⚠ Erro ao carregar HistoricoController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarDashboardController() {
        try {
            Object includeContent = tabDashboard.getContent();
            if (includeContent != null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/estoquefx/dashboard-view.fxml")
                );
                VBox dashboardView = loader.load();
                dashboardController = loader.getController();
                tabDashboard.setContent(dashboardView);

                System.out.println("✓ DashboardController carregado");
            }
        } catch (Exception e) {
            System.err.println("⚠ Erro ao carregar DashboardController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== CONEXÃO ENTRE CONTROLLERS ==========

    private void conectarControllers() {
        // Tabela → Histórico
        if (tabelaController != null && historicoController != null) {
            tabelaController.setHistoricoController(historicoController);
        }

        // Tabela → Última alteração
        if (tabelaController != null) {
            tabelaController.setOnUltimaAlteracaoChanged(this::atualizarUltimaAlteracao);
        }

        System.out.println("✓ Controllers conectados");
    }

    // ========== CONFIGURAÇÃO DE ESTOQUE ==========

    public void setEstoqueAtual(String estoqueId, String estoqueNome, SupabaseService service) {
        this.estoqueId = estoqueId;
        this.supabaseService = service;
        Leitor.setNomeEstoque(estoqueNome);

        try {
            Leitor.carregarMisc();
        } catch (Exception e) {
            e.printStackTrace();
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

        // garantir que a tabela seja atualizada no Platform.runLater
        // para que todos os bindings estejam prontos
        if (tabelaController != null) {
            Platform.runLater(() -> {
                System.out.println("📊 Atualizando tabela com " + Estoque.getProdutos().size() + " produtos");
                tabelaController.refresh();
                tabelaController.carregarUltimaAlteracao();
            });
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void atualizarUltimaAlteracao() {
        Time.updateTime();
        if (tabelaController != null) {
            tabelaController.carregarUltimaAlteracao();
        }
    }

    // ========== AÇÕES DO MENU - VERSÃO ==========

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

    // ========== MÉTODOS AUXILIARES - DIALOGS ==========

    public static void mostrarInfoStatic(Alert.AlertType tipo, String title, String header, String message) {
        Alert alert = new Alert(tipo);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========== SETTERS ==========

    public void setHistoricoController(HistoricoController historicoController) {
        this.historicoController = historicoController;
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public static void setStage(Stage stage) {
        EstoqueController.stage = stage;
    }

    public static Stage getStage() {
        return stage;
    }
}
