package com.mmhachem.exchange.notifications;

import com.mmhachem.exchange.api.model.Notification;
import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.ExchangeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Notifications implements Initializable {

    @FXML private TableView<Notification> notificationTable;
    @FXML private TableColumn<Notification, String> typeColumn;
    @FXML private TableColumn<Notification, String> messageColumn;
    @FXML private TableColumn<Notification, String> timestampColumn;

    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().type));
        messageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().message));
        timestampColumn.setCellValueFactory(cellData -> {
            try {
                Date date = inputFormat.parse(cellData.getValue().timestamp);
                return new SimpleStringProperty(displayFormat.format(date));
            } catch (ParseException e) {
                return new SimpleStringProperty(cellData.getValue().timestamp);
            }
        });

        refreshNotifications();
    }

    @FXML
    private void refreshNotifications() {
        String token = Authentication.getInstance().getToken();
        ExchangeService.exchangeApi().getNotifications("Bearer " + token).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(retrofit2.Call<List<Notification>> call, retrofit2.Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    javafx.application.Platform.runLater(() -> notificationTable.getItems().setAll(response.body()));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Notification>> call, Throwable t) {
                System.err.println("Failed to load notifications: " + t.getMessage());
            }
        });
    }
}
