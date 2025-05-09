package com.mmhachem.exchange;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        if (Authentication.getToken() != null) {
            Authentication.getInstance().parseUserIdFromToken();
        }
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/mmhachem/exchange/parent.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 475);
        stage.setTitle("Currency Exchange");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}