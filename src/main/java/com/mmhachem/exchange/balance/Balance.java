package com.mmhachem.exchange.balance;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.ExchangeService;
import com.mmhachem.exchange.api.model.UserBalance;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Balance {
    @FXML public TextField usdField;
    @FXML public TextField lbpField;
    @FXML public Label statusLabel;

    @FXML
    private void submitUsdBalance() {
        try {
            float usdInput = Float.parseFloat(usdField.getText().trim());
            if (usdInput <= 0) {
                statusLabel.setText("Please enter a positive USD amount.");
                return;
            }

            String token = Authentication.getInstance().getToken();
            ExchangeService.exchangeApi().getBalance("Bearer " + token).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<UserBalance> call, Response<UserBalance> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        float currentUsd = response.body().usdBalance != null ? response.body().usdBalance : 0;
                        UserBalance updated = new UserBalance();
                        updated.usdBalance = currentUsd + usdInput;
                        sendBalance(updated, "USD");
                    } else {
                        updateStatus("Failed to fetch current USD balance.", false);
                    }
                }

                @Override
                public void onFailure(Call<UserBalance> call, Throwable t) {
                    updateStatus("Error fetching USD balance: " + t.getMessage(), false);
                }
            });

        } catch (NumberFormatException e) {
            updateStatus("Invalid USD value.", false);
        }
    }

    @FXML
    private void submitLbpBalance() {
        try {
            float lbpInput = Float.parseFloat(lbpField.getText().trim());
            if (lbpInput <= 0) {
                statusLabel.setText("Please enter a positive LBP amount.");
                return;
            }

            String token = Authentication.getInstance().getToken();
            ExchangeService.exchangeApi().getBalance("Bearer " + token).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<UserBalance> call, Response<UserBalance> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        float currentLbp = response.body().lbpBalance != null ? response.body().lbpBalance : 0;
                        UserBalance updated = new UserBalance();
                        updated.lbpBalance = currentLbp + lbpInput;
                        sendBalance(updated, "LBP");
                    } else {
                        updateStatus("Failed to fetch current LBP balance.", false);
                    }
                }

                @Override
                public void onFailure(Call<UserBalance> call, Throwable t) {
                    updateStatus("Error fetching LBP balance: " + t.getMessage(), false);
                }
            });

        } catch (NumberFormatException e) {
            updateStatus("Invalid LBP value.", false);
        }
    }

    private void sendBalance(UserBalance balance, String type) {
        String token = Authentication.getInstance().getToken();
        ExchangeService.exchangeApi().updateBalance(balance, "Bearer " + token)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<UserBalance> call, Response<UserBalance> response) {
                        if (response.isSuccessful()) {
                            updateStatus(type + " balance updated successfully ", true);
                        } else {
                            updateStatus("Failed: Only one currency allowed per update.", false);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserBalance> call, Throwable t) {
                        updateStatus("⚠ Error updating balance: " + t.getMessage(), false);
                    }
                });
    }

    private void updateStatus(String message, boolean success) {
        Platform.runLater(() -> {
            statusLabel.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            statusLabel.setText(message);
        });
    }
}
