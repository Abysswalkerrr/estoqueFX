module com.estoquefx {
    requires com.estoquefx.updater.core;

    requires javafx.fxml;

    requires javafx.controls;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.estoquefx to javafx.fxml;
    exports com.estoquefx;
}