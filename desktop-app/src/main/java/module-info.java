module com.estoquefx {
    requires com.estoquefx.updater.core;

    requires javafx.fxml;

    requires javafx.controls;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires okhttp3;
    requires com.google.gson;
    requires resend.java;
    requires annotations;

    opens com.estoquefx to javafx.fxml;
    exports com.estoquefx;
    exports com.estoquefx.model.estoque;
    opens com.estoquefx.model.estoque to javafx.fxml;
    exports com.estoquefx.controller;
    exports com.estoquefx.model.patrimonio;
    opens com.estoquefx.model.patrimonio to javafx.fxml;
    opens com.estoquefx.controller to javafx.fxml;
    exports com.estoquefx.service;
    opens com.estoquefx.service to javafx.fxml;
    exports com.estoquefx.util;
    opens com.estoquefx.util to javafx.fxml;
    exports com.estoquefx.data;
    opens com.estoquefx.data to javafx.fxml;
    exports com.estoquefx.controller.estoque;
    opens com.estoquefx.controller.estoque to javafx.fxml;
    exports com.estoquefx.controller.patrimonio;
    opens com.estoquefx.controller.patrimonio to javafx.fxml;
    exports com.estoquefx.service.estoque;
    opens com.estoquefx.service.estoque to javafx.fxml;
    exports com.estoquefx.service.patrimonio;
    opens com.estoquefx.service.patrimonio to javafx.fxml;
}