module ru.gb.fxchat {
    requires javafx.controls;
    requires javafx.fxml;

    exports ru.gb.fxchat.client;
    opens ru.gb.fxchat.client to javafx.fxml;
}