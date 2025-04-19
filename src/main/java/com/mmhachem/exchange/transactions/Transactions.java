package com.mmhachem.exchange.transactions;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.ExchangeService;
import com.mmhachem.exchange.api.model.Transaction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Transactions implements Initializable {

    @FXML
    private VBox cardContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String token = "Bearer " + Authentication.getInstance().getToken();

        ExchangeService.exchangeApi().getTransactions(token).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Platform.runLater(() -> populateCards(response.body()));
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable throwable) {
                System.err.println("Failed to load transactions: " + throwable.getMessage());
            }
        });
    }

    private void populateCards(List<Transaction> transactions) {
        cardContainer.getChildren().clear();

        for (Transaction tx : transactions) {
            HBox card = new HBox(30);
            card.getStyleClass().add("transaction-card");
            card.setPadding(new Insets(15));

            Label usdLabel = new Label("USD: " + tx.getUsd_amount());
            usdLabel.getStyleClass().add("card-label");

            Label lbpLabel = new Label("LBP: " + tx.getLbp_amount());
            lbpLabel.getStyleClass().add("card-label");

            Label dateLabel = new Label("Date: " + tx.getAdded_date());
            dateLabel.getStyleClass().add("card-label");

            card.getChildren().addAll(usdLabel, lbpLabel, dateLabel);
            cardContainer.getChildren().add(card);
        }
    }
}
