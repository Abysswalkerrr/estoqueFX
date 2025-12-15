module br.com.estoquefx.estoquefx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens br.com.SistemaEstoqueFX to javafx.fxml;
    exports br.com.SistemaEstoqueFX;
}