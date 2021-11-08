module com.progiii.mailclientserver {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.progiii.mailclientserver to javafx.fxml;
    exports com.progiii.mailclientserver.client.view;
    opens com.progiii.mailclientserver.client.view to javafx.fxml;
}