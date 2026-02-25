package com.estoquefx.controller.patrimonio;

import com.estoquefx.model.patrimonio.Patrimonio;
import com.estoquefx.service.SupabaseService;
import com.estoquefx.service.patrimonio.PatrimonioService;
import com.estoquefx.util.I18n;
import com.estoquefx.util.Time;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.text.MessageFormat;

public class PatrimonioController {

    @FXML private TableView<Patrimonio> tabela;
    @FXML private TableColumn<Patrimonio, String> colCodigo;
    @FXML private TableColumn<Patrimonio, String> colNome;
    @FXML private TableColumn<Patrimonio, String> colLocalizacao;
    @FXML private TableColumn<Patrimonio, String> colEstado;
    @FXML private TableColumn<Patrimonio, String> colDescricao;
    @FXML private TableColumn<Patrimonio, String> colHora;
    @FXML private TextField txtBusca;
    @FXML private Label lblResultados;

    private ObservableList<Patrimonio> dados;
    private FilteredList<Patrimonio> filtrados;

    private PatrimonioService patrimonioService;
    private String estoqueId;

    private static boolean patrimonioAlterado = false;

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colLocalizacao.setCellValueFactory(new PropertyValueFactory<>("localizacao"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("alterHora"));

        tabela.setEditable(true);
        colNome.setCellFactory(TextFieldTableCell.forTableColumn());
        colNome.setOnEditCommit(e -> {
            e.getRowValue().setNome(e.getNewValue().trim().toUpperCase());
            tabela.refresh();
            patrimonioAlterado = true;
        });
        colLocalizacao.setCellFactory(TextFieldTableCell.forTableColumn());
        colLocalizacao.setOnEditCommit(e -> {
            e.getRowValue().setLocalizacao(e.getNewValue().trim());
            tabela.refresh();
            patrimonioAlterado = true;
        });
        colEstado.setCellFactory(ComboBoxTableCell.forTableColumn(
                colEstado.setCellFactory(ComboBoxTableCell.forTableColumn(
                        new StringConverter<>() {
                            @Override
                            public String toString(String object) {
                                if (object == null) return "";
                                return switch (object) {
                                    case "BOM"     -> I18n.t("patrimonio.estado.bom");
                                    case "REGULAR" -> I18n.t("patrimonio.estado.regular");
                                    case "RUIM"    -> I18n.t("patrimonio.estado.ruim");
                                    default        -> object;
                                };
                            }
                            @Override
                            public String fromString(String string) {
                                // converte label traduzido de volta para constante interna
                                if (string.equals(I18n.t("patrimonio.estado.bom")))     return "BOM";
                                if (string.equals(I18n.t("patrimonio.estado.regular"))) return "REGULAR";
                                if (string.equals(I18n.t("patrimonio.estado.ruim")))    return "RUIM";
                                return string;
                            }
                        },
                        "BOM", "REGULAR", "RUIM"   // ← valores internos reais
                ));
        colEstado.setOnEditCommit(e -> {
            e.getRowValue().setEstado(e.getNewValue()); // e.getNewValue() já é "BOM"/"REGULAR"/"RUIM"
            e.getRowValue().setAlterHora(Time.getTempoFormatado(Time.getTime(true)));
            tabela.refresh();
            patrimonioAlterado = true;
        });        ));
        colEstado.setOnEditCommit(e -> {
            e.getRowValue().setEstado(e.getNewValue());
            e.getRowValue().setAlterHora(Time.getTempoFormatado(Time.getTime(true)));
            tabela.refresh();
            patrimonioAlterado = true;
        });
        colDescricao.setCellFactory(col -> new TableCell<>() {
            private final Text text = new Text();

            {
                setGraphic(text);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
                text.wrappingWidthProperty().bind(col.widthProperty().subtract(10));

                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !isEmpty()) {
                        Patrimonio p = getTableView().getItems().get(getIndex());
                        abrirDialogoDescricao(p);
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (text.fillProperty().isBound()) {
                    text.fillProperty().unbind();
                }
                if (empty || item == null) {
                    text.setText("");
                    return;
                }
                text.setText(item);
            }
        });

        tabela.setRowFactory(_ -> new TableRow<>() {
            @Override
            protected void updateItem(Patrimonio item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("estado-ruim", "estado-regular");
                if (!empty && item != null) {
                    if ("RUIM".equals(item.getEstado()) || I18n.t("patrimonio.estado.ruim").equals(item.getEstado()))
                        getStyleClass().add("estado-ruim");
                    else if ("REGULAR".equals(item.getEstado()) || I18n.t("patrimonio.estado.regular").equals(item.getEstado()))
                        getStyleClass().add("estado-regular");
                }
            }
        });

        dados = FXCollections.observableArrayList(Patrimonio.getPatrimonios());
        filtrados = new FilteredList<>(dados, _ -> true);

        SortedList<Patrimonio> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(ordenados);

        txtBusca.textProperty().addListener((_, _, newVal) -> {
            String busca = newVal.toUpperCase();
            filtrados.setPredicate(p -> {
                if (busca.isBlank()) return true;
                return p.getNome().toUpperCase().contains(busca)
                        || String.valueOf(p.getCodigo()).toUpperCase().contains(busca)
                        || p.getLocalizacao().toUpperCase().contains(busca);
            });

            atualizarResultado();
        });

        Platform.runLater(this::atualizarResultado);
    }

    @FXML
    private void adicionarPatrimonio() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(I18n.t("patrimonio.dialog.add.title"));
        dialog.setHeaderText(I18n.t("patrimonio.dialog.add.header"));
        dialog.showAndWait().ifPresent(nome -> {
            if (!nome.isBlank()) {
                new Patrimonio(nome.trim().toUpperCase(), "",
                        "SEM LOCALIZAÇÃO",   // localização é texto livre, não constante — pode ficar hardcoded ou traduzido
                        // Patrimonio.addPatrimonio(p);
                dados.add(p));
                atualizarResultado();
            }
        });
        patrimonioAlterado = true;
    }

    @FXML
    private void removerPatrimonio() {
        Patrimonio selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    MessageFormat.format(I18n.t("patrimonio.dialog.remove.confirm"), selecionado.getNome()),
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.YES) {
                    Patrimonio.removePatrimonio(selecionado);
                    dados.remove(selecionado);
                    atualizarResultado();
                }
            });
        }
        patrimonioAlterado = true;
    }

    private void atualizarResultado() {
        lblResultados.setText(MessageFormat.format(
                I18n.t("patrimonio.status.showing"),
                filtrados.size(),
                dados.size()
        ));
    }

    public void setSupabaseService(SupabaseService service, String estoqueId) {
        this.patrimonioService = new PatrimonioService(service);
        this.estoqueId = estoqueId;
    }

    public void salvarNuvem() {
        if (patrimonioService == null || estoqueId == null) return;
        try {
            patrimonioService.salvarTodos(Patrimonio.getPatrimonios(), estoqueId);
        } catch (Exception e) {
            System.err.println("Erro ao salvar patrimônios: " + e.getMessage());
        }
    }

    private void abrirDialogoDescricao(Patrimonio patrimonio) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(I18n.t("tabela.edit_description.title"));

        ButtonType okButtonType = new ButtonType(I18n.t("tabela.edit_description.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextArea area = new TextArea(patrimonio.getDescricao());
        area.setWrapText(true);
        area.setPrefRowCount(6);
        area.setPrefColumnCount(40);

        area.setStyle(
                "-fx-control-inner-background: #ffffff;" +
                        "-fx-background-color: #f0f0f0;" +
                        "-fx-text-fill: black;"
        );

        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #e0e0e0;" +
                        "-fx-text-fill: black;"
        );

        dialog.setResultConverter(button -> {
            if (button == okButtonType) {
                return area.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(novaDesc -> {
            novaDesc = novaDesc.trim();
            patrimonio.setDescricao(novaDesc);
            refresh();
        });
    }

    public static boolean isPatrimonioAlterado() {
        return patrimonioAlterado;
    }

    public void refresh() {
        dados.setAll(Patrimonio.getPatrimonios());
        atualizarResultado();
        tabela.refresh();
    }
}
