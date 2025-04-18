package com.mmhachem.exchange.register;

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
            System.err.println("Username and password fields cannot be empty.");
            return;
        }

        User user = new User(username, password);
        ExchangeService.exchangeApi().addUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    ExchangeService.exchangeApi().authenticate(user).enqueue(new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Authentication.getInstance().saveToken(response.body().getToken());

                                Platform.runLater(() -> {
                                    onPageCompleteListener.onPageCompleted();
                                });
                            } else {
                                System.err.println("Auto-login after registration failed: Invalid credentials or server error.");
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable throwable) {
                            System.err.println("Authentication request failed: " + throwable.getMessage());
                        }
                    });
                } else {
                    System.err.println("Registration failed: Username might already exist or server error.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {
                System.err.println("Registration request failed: " + throwable.getMessage());
            }
        });
    }
}

