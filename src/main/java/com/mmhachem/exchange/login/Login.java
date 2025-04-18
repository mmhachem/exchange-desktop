package com.mmhachem.exchange.login;

import com.mmhachem.exchange.api.model.User;
import com.mmhachem.exchange.api.model.Token;
import com.mmhachem.exchange.api.model.ExchangeService;
import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.PageCompleter;
import com.mmhachem.exchange.OnPageCompleteListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.JsonParser;

import java.util.Base64;


public class Login implements PageCompleter {
    public TextField usernameTextField;
    public TextField passwordTextField;

    private OnPageCompleteListener onPageCompleteListener;

    @Override
    public void setOnPageCompleteListener(OnPageCompleteListener onPageCompleteListener) {
        this.onPageCompleteListener = onPageCompleteListener;
    }

    public void login(ActionEvent actionEvent) {
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.err.println("Username and password fields cannot be empty.");
            return;
        }

        User user = new User(username, password);
        ExchangeService.exchangeApi().authenticate(user).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    Authentication.getInstance().saveToken(token);


                    try {
                        String[] parts = token.split("\\.");
                        String payload = new String(Base64.getDecoder().decode(parts[1]));
                        com.google.gson.JsonObject json = new JsonParser().parse(payload).getAsJsonObject();

                        int id = json.get("sub").getAsInt();
                        Authentication.getInstance().saveUserId(id); // ✅ Save to Authentication
                    } catch (Exception e) {
                        System.err.println("Failed to parse token: " + e.getMessage());
                    }

                    Platform.runLater(() -> {
                        onPageCompleteListener.onPageCompleted();
                    });
                } else {
                    System.err.println("Login failed: Invalid credentials or server error.");
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable throwable) {
                System.err.println("Login request failed: " + throwable.getMessage());
            }
        });
    }

}



