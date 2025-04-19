package com.mmhachem.exchange.profile;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.ExchangeService;
import com.mmhachem.exchange.api.model.Notification;
import com.mmhachem.exchange.api.model.UserProfile;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Profile implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private Label usdLabel;
    @FXML private Label lbpLabel;
    @FXML private Label pointsLabel;
    @FXML private Label levelLabel;
    @FXML private ListView<String> badgesList;

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
                    System.out.println("RAW BADGES: " + profile.badges);

                    Platform.runLater(() -> {
                        usernameLabel.setText(profile.userName);
                        usdLabel.setText(String.format("%,.2f", profile.usdBalance));
                        lbpLabel.setText(String.format("%,.2f", profile.lbpBalance));
                        pointsLabel.setText(String.valueOf(profile.points));
                        levelLabel.setText(profile.level);

                        badgesList.getItems().clear();

                        if (profile.badges != null && !profile.badges.isEmpty()) {
                            for (String badge : profile.badges) {
                                if (badge.startsWith("[") && badge.endsWith("]")) {
                                    badge = badge.replace("[", "")
                                            .replace("]", "")
                                            .replace("\"", "")
                                            .trim();
                                    for (String b : badge.split(",")) {
                                        badgesList.getItems().add(b.trim());
                                    }
                                } else {
                                    badgesList.getItems().add(badge);
                                }
                            }
                        } else {
                            // Simulate badges based on points
                            if (profile.points > 0) {
                                badgesList.getItems().add("First Exchange");
                            }
                            if (profile.points >= 100) {
                                badgesList.getItems().add("10 Deals Done");
                            }
                            if (profile.points >= 20) {
                                badgesList.getItems().add("Used Both USD & LBP");
                            }
                        }
                    });

                    // ✅ EXTRA CALL: Get approvals count from notifications
                    ExchangeService.exchangeApi().getNotifications("Bearer " + token).enqueue(new Callback<List<Notification>>() {
                        @Override
                        public void onResponse(Call<List<Notification>> call, Response<List<Notification>> notiResponse) {
                            if (notiResponse.isSuccessful() && notiResponse.body() != null) {
                                List<Notification> notifications = notiResponse.body();

                                long approvedCount = notifications.stream()
                                        .filter(n -> "offer_request".equals(n.type) && "approved".equals(n.status))
                                        .count();

                                if (approvedCount >= 5) {
                                    Platform.runLater(() ->
                                            badgesList.getItems().add("5 Approvals Received"));
                                }
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
}
