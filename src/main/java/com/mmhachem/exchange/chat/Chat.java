package com.mmhachem.exchange.chat;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.ChatRequest;
import com.mmhachem.exchange.api.model.ChatResponse;
import com.mmhachem.exchange.api.model.ExchangeService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chat {

    @FXML private VBox chatBox;
    @FXML private TextField inputField;
    @FXML private ScrollPane chatScroll;

    @FXML
    public void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        addMessage("You: " + message, "user-message", Pos.CENTER_RIGHT);

        inputField.clear();

        // Call backend
        String token = Authentication.getInstance().getToken();
        Call<ChatResponse> call = ExchangeService.exchangeApi().chat(new ChatRequest(message), "Bearer " + token);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().response;
                    Platform.runLater(() ->
                            addMessage("ExchangeBot: " + reply, "bot-message", Pos.CENTER_LEFT)
                    );
                } else {
                    Platform.runLater(() ->
                            addMessage("ExchangeBot: Failed to get a response.", "bot-message", Pos.CENTER_LEFT)
                    );
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Platform.runLater(() ->
                        addMessage("Error: " + t.getMessage(), "bot-message", Pos.CENTER_LEFT)
                );
            }
        });
    }

    private void addMessage(String text, String styleClass, Pos alignment) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMaxWidth(400);

        HBox container = new HBox(label);
        container.setAlignment(alignment);
        container.setPadding(new Insets(5, 0, 5, 0));

        chatBox.getChildren().add(container);

        // Scroll to bottom after adding message
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }
}
