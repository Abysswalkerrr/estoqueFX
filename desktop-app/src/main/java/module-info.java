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

    opens com.estoquefx to javafx.fxml;
    exports com.estoquefx;
    exports com.estoquefx.model;
    opens com.estoquefx.model to javafx.fxml;
    exports com.estoquefx.controller;
    opens com.estoquefx.controller to javafx.fxml;
    exports com.estoquefx.service;
    opens com.estoquefx.service to javafx.fxml;
    exports com.estoquefx.util;
    opens com.estoquefx.util to javafx.fxml;
    exports com.estoquefx.data;
    opens com.estoquefx.data to javafx.fxml;
    exports com.estoquefx.controller.estoque;
    opens com.estoquefx.controller.estoque to javafx.fxml;
}