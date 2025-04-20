package com.mmhachem.exchange.profile;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.ExchangeService;
import com.mmhachem.exchange.api.model.Notification;
import com.mmhachem.exchange.api.model.UserProfile;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Profile implements Initializable {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label usdLabel;
    @FXML
    private Label lbpLabel;
    @FXML
    private Label pointsLabel;
    @FXML
    private Label levelLabel;
    @FXML
    private FlowPane badgesList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshProfile();
    }

    @FXML
    public void refreshProfile() {
        String token = Authentication.getInstance().getToken();
        if (token == null) return;

        ExchangeService.exchangeApi().getProfile("Bearer " + token).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();

                    Platform.runLater(() -> {
                        usernameLabel.setText(profile.userName);
                        usdLabel.setText(String.format("%,.2f", profile.usdBalance));
                        lbpLabel.setText(String.format("%,.2f", profile.lbpBalance));
                        pointsLabel.setText(String.valueOf(profile.points));
                        levelLabel.setText(profile.level);

                        badgesList.getChildren().clear();

                        if (profile.badges != null && !profile.badges.isEmpty()) {
                            for (String badge : profile.badges) {
                                if (badge.startsWith("[") && badge.endsWith("]")) {
                                    badge = badge.replace("[", "")
                                            .replace("]", "")
                                            .replace("\"", "")
                                            .trim();
                                    for (String b : badge.split(",")) {
                                        addBadge(b.trim());
                                    }
                                } else {
                                    addBadge(badge);
                                }
                            }
                        } else {
                            if (profile.points > 0) {
                                addBadge("First Exchange");
                            }
                            if (profile.points >= 100) {
                                addBadge("10 Deals Done");
                            }
                        }
                    });

                    ExchangeService.exchangeApi().getNotifications("Bearer " + token).enqueue(new Callback<List<Notification>>() {
                        @Override
                        public void onResponse(Call<List<Notification>> call, Response<List<Notification>> notiResponse) {
                            if (notiResponse.isSuccessful() && notiResponse.body() != null) {
                                List<Notification> notifications = notiResponse.body();

                                long approvedCount = notifications.stream()
                                        .filter(n -> "offer_request".equals(n.type) && "approved".equals(n.status))
                                        .count();

                                boolean didUSDToLBP = notifications.stream().anyMatch(n ->
                                        "offer_request".equals(n.type) &&
                                                "approved".equals(n.status) &&
                                                n.message.contains("received ") &&
                                                n.message.contains("USD") &&
                                                n.message.contains("paid") &&
                                                n.message.contains("LBP") &&
                                                n.message.indexOf("USD") < n.message.indexOf("LBP")
                                );

                                boolean didLBPToUSD = notifications.stream().anyMatch(n ->
                                        "offer_request".equals(n.type) &&
                                                "approved".equals(n.status) &&
                                                n.message.contains("received ") &&
                                                n.message.contains("LBP") &&
                                                n.message.contains("paid") &&
                                                n.message.contains("USD") &&
                                                n.message.indexOf("LBP") < n.message.indexOf("USD")
                                );

                                Platform.runLater(() -> {
                                    if (approvedCount >= 5) {
                                        addBadge("5 Approvals Received");
                                    }
                                    if (didUSDToLBP && didLBPToUSD) {
                                        addBadge("Used Both USD & LBP");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Notification>> call, Throwable t) {
                            System.err.println("Failed to load notifications: " + t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable throwable) {
                System.err.println("Failed to load profile: " + throwable.getMessage());
            }
        });
    }

    private void addBadge(String text) {
        Label badgeLabel = new Label(text);
        badgeLabel.getStyleClass().add("badge");
        badgeLabel.setTooltip(new Tooltip(text)); // Optional: tooltip for full text
        badgesList.getChildren().add(badgeLabel);
    }
}
