module com.progiii.mailclientserver {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.graphics;
    requires json.simple;
    requires javafx.base;

    exports com.progiii.mailclientserver.client.view;
    opens com.progiii.mailclientserver.client.view to javafx.fxml;
    exports com.progiii.mailclientserver.client.controller;
    opens com.progiii.mailclientserver.client.controller to javafx.fxml;
    exports com.progiii.mailclientserver.server.controller;
    opens com.progiii.mailclientserver.server.controller to javafx.fxml;
    exports com.progiii.mailclientserver.server.view;
    opens com.progiii.mailclientserver.server.view to javafx.fxml;
}