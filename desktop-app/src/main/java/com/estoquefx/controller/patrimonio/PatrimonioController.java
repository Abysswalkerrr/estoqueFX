package com.estoquefx.controller.patrimonio;

import com.estoquefx.model.patrimonio.Patrimonio;
import com.estoquefx.service.SupabaseService;
import com.estoquefx.service.patrimonio.PatrimonioService;
import com.estoquefx.util.I18n;
import com.estoquefx.util.Time;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
        // Configurar colunas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colLocalizacao.setCellValueFactory(new PropertyValueFactory<>("localizacao"));

        // Estado: manter internamente BOM/REGULAR/RUIM, mas exibir traduzido.
        colEstado.setCellValueFactory(cellData -> {
            String estado = cellData.getValue() == null ? null : cellData.getValue().getEstado();
            return new SimpleStringProperty(estadoToDisplay(estado));
        });

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

        colEstado.setCellFactory(col -> new TableCell<Patrimonio, String>() {

            private final ComboBox<String> combo = new ComboBox<>(
                    FXCollections.observableArrayList("BOM", "REGULAR", "RUIM")
            );

            {
                // StringConverter: BOM → "Good"/"Bom", etc.
                combo.setConverter(new StringConverter<>() {
                    @Override public String toString(String s) { return estadoToDisplay(s); }
                    @Override public String fromString(String s) { return displayToEstado(s); }
                });

                // Forçar estilo diretamente no ComboBox e no seu ListCell interno
                combo.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: #999;" +
                                "-fx-font-size: 12px;"
                );

                // Esse é o pulo do gato: pintar o ListCell do campo fechado
                combo.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setStyle("-fx-background-color: white; -fx-text-fill: black;");
                        setText(empty || item == null ? "" : estadoToDisplay(item));
                    }
                });

                // CellFactory do dropdown aberto
                combo.setCellFactory(_ -> new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "" : estadoToDisplay(item));
                        // Não seta style fixo aqui — deixa o CSS cuidar do hover/selected
                        if (empty || item == null) {
                            setStyle("-fx-background-color: white; -fx-text-fill: black;");
                        }
                        // célula com conteúdo: só garante texto preto, fundo fica com o tema
                        // para que :hover e :selected do CSS funcionem normalmente
                    }
                });

                combo.setOnAction(_ -> {
                    if (isEditing()) {
                        commitEdit(combo.getValue());
                    }
                });
            }

            @Override
            public void startEdit() {
                super.startEdit();
                combo.setValue(getItem());
                setGraphic(combo);
                setText(null);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(estadoToDisplay(getItem()));
                setGraphic(null);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    combo.setValue(item);
                    setText(null);
                    setGraphic(combo);
                } else {
                    setText(estadoToDisplay(item));
                    setGraphic(null);
                }
            }
        });

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
                    if ("RUIM".equals(item.getEstado()))
                        getStyleClass().add("estado-ruim");
                    else if ("REGULAR".equals(item.getEstado()))
                        getStyleClass().add("estado-regular");
                }
            }
        });

        dados = FXCollections.observableArrayList(Patrimonio.getPatrimonios());
        filtrados = new FilteredList<>(dados, _ -> true);

        SortedList<Patrimonio> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(ordenados);

        // Filtro busca
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
                Patrimonio p = new Patrimonio(
                        nome.trim().toUpperCase(),
                        "",
                        "SEM LOCALIZAÇÃO",
                        "BOM",
                        Time.getTempoFormatado(Time.getTime(true))
                );
                Patrimonio.addPatrimonio(p);
                dados.add(p);
                atualizarResultado();
            }
        });
        patrimonioAlterado = true;
    }

    @FXML
    private void removerPatrimonio() {
        Patrimonio selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    MessageFormat.format(I18n.t("patrimonio.dialog.remove.confirm"), selecionado.getNome()),
                    ButtonType.YES, ButtonType.NO
            );
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

    private String estadoToDisplay(String estado) {
        if (estado == null) return "";
        return switch (estado) {
            case "BOM" -> I18n.t("patrimonio.estado.bom");
            case "REGULAR" -> I18n.t("patrimonio.estado.regular");
            case "RUIM" -> I18n.t("patrimonio.estado.ruim");
            default -> estado;
        };
    }

    private String displayToEstado(String display) {
        if (display == null) return null;
        if (display.equals(I18n.t("patrimonio.estado.bom"))) return "BOM";
        if (display.equals(I18n.t("patrimonio.estado.regular"))) return "REGULAR";
        if (display.equals(I18n.t("patrimonio.estado.ruim"))) return "RUIM";
        return display;
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
