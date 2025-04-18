package com.mmhachem.exchange.register;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mmhachem.exchange.api.model.User;
import com.mmhachem.exchange.api.model.Token;
import com.mmhachem.exchange.api.model.ExchangeService;
import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.PageCompleter;
import com.mmhachem.exchange.OnPageCompleteListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class Register implements PageCompleter {
    public TextField usernameTextField;
    public TextField passwordTextField;

    private OnPageCompleteListener onPageCompleteListener;

    @Override
    public void setOnPageCompleteListener(OnPageCompleteListener onPageCompleteListener) {
        this.onPageCompleteListener = onPageCompleteListener;
    }

    public void register(ActionEvent actionEvent) {
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields", "Username and password fields cannot be empty.");
            return;
        }

        User user = new User(username, password);
        ExchangeService.exchangeApi().addUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    // Auto-login after successful registration
                    ExchangeService.exchangeApi().authenticate(user).enqueue(new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Authentication.getInstance().saveToken(response.body().getToken());
                                Platform.runLater(() -> onPageCompleteListener.onPageCompleted());
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Login Failed", "Auto-login failed. Please try logging in manually.");
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable throwable) {
                            showAlert(Alert.AlertType.ERROR, "Network Error", "Authentication failed: " + throwable.getMessage());
                        }
                    });

                } else {
                    // 🔍 Parse error from backend JSON
                    try {
                        String errorBody = response.errorBody().string();
                        if (errorBody.trim().startsWith("{")) {
                            JsonObject json = new JsonParser().parse(errorBody).getAsJsonObject();
                            String errorMessage = json.has("error") ? json.get("error").getAsString() : "Unknown server error.";
                            showAlert(Alert.AlertType.ERROR, "Registration Failed", errorMessage);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Registration Failed", errorBody);
                        }
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Registration Failed", "Error reading response.");
                    }

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {
                showAlert(Alert.AlertType.ERROR, "Network Error", "Registration request failed: " + throwable.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}

