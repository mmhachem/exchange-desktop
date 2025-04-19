package com.mmhachem.exchange.chat;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.ChatRequest;
import com.mmhachem.exchange.api.model.ChatResponse;
import com.mmhachem.exchange.api.model.ExchangeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chat {
    @FXML private TextArea chatArea;
    @FXML private TextField inputField;

    @FXML
    public void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        chatArea.appendText("You: " + message + "\n");
        inputField.clear();

        String token = Authentication.getInstance().getToken();
        Call<ChatResponse> call = ExchangeService.exchangeApi().chat(new ChatRequest(message), "Bearer " + token);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().response;
                    Platform.runLater(() -> chatArea.appendText("ExchangeBot: " + reply + "\n"));
                } else {
                    Platform.runLater(() -> chatArea.appendText("ExchangeBot: Failed to get a response.\n"));
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Platform.runLater(() -> chatArea.appendText("Error: " + t.getMessage() + "\n"));
            }
        });
    }
}
