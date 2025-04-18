package com.mmhachem.exchange.profile;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.UserProfile;
import com.mmhachem.exchange.api.model.ExchangeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
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
                    Platform.runLater(() -> {
                        usernameLabel.setText(profile.userName);
                        usdLabel.setText(String.format("%,.2f", profile.usdBalance));
                        lbpLabel.setText(String.format("%,.2f", profile.lbpBalance));
                        pointsLabel.setText(String.valueOf(profile.points));
                        levelLabel.setText(profile.level);
                        badgesList.getItems().setAll(profile.badges);
                    });
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable throwable) {
                System.err.println("Failed to load com.mmhachem.exchange.profile: " + throwable.getMessage());
            }
        });
    }
}
