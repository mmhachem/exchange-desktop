package com.mmhachem.exchange.notifications;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.ExchangeService;
import com.mmhachem.exchange.api.model.Notification;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Notifications implements Initializable {

    @FXML
    private VBox notificationsContainer;

    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshNotifications();
    }

    @FXML
    private void refreshNotifications() {
        String token = Authentication.getInstance().getToken();
        ExchangeService.exchangeApi().getNotifications("Bearer " + token).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(retrofit2.Call<List<Notification>> call, retrofit2.Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> notifications = response.body();
                    Platform.runLater(() -> {
                        notificationsContainer.getChildren().clear();
                        for (Notification notification : notifications) {
                            notificationsContainer.getChildren().add(createNotificationCard(notification));
                        }
                    });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Notification>> call, Throwable t) {
                System.err.println("Failed to load notifications: " + t.getMessage());
            }
        });
    }

    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(5);
        card.getStyleClass().add("notification-card");

        Label typeLabel = new Label(notification.type.toUpperCase());
        typeLabel.getStyleClass().add("notification-type");

        Text messageText = new Text(notification.message);
        messageText.wrappingWidthProperty().set(500);
        TextFlow messageFlow = new TextFlow(messageText);
        messageFlow.getStyleClass().add("notification-message");

        Label timeLabel;
        try {
            Date date = inputFormat.parse(notification.timestamp);
            timeLabel = new Label(displayFormat.format(date));
        } catch (ParseException e) {
            timeLabel = new Label(notification.timestamp);
        }
        timeLabel.getStyleClass().add("notification-time");

        card.getChildren().addAll(typeLabel, messageFlow, timeLabel);
        return card;
    }
}
