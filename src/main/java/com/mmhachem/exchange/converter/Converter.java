package com.mmhachem.exchange.converter;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.ConversionRequest;
import com.mmhachem.exchange.api.model.ConversionResponse;
import com.mmhachem.exchange.api.model.Currency;
import com.mmhachem.exchange.api.model.Exchange;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.stream.Collectors;

public class Converter {

    @FXML private ComboBox<String> fromCurrency;
    @FXML private ComboBox<String> toCurrency;
    @FXML private TextField amountField;
    @FXML private Label resultLabel;

    @FXML
    public void initialize() {
        String token = "Bearer " + Authentication.getInstance().getToken();
        Exchange api = com.mmhachem.exchange.api.model.ExchangeService.exchangeApi();

        api.getCurrencies(token).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Currency>> call, Response<List<Currency>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> codes = response.body().stream().map(Currency::getCode).collect(Collectors.toList());
                    Platform.runLater(() -> {
                        fromCurrency.getItems().addAll(codes);
                        toCurrency.getItems().addAll(codes);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Currency>> call, Throwable t) {
                Platform.runLater(() ->
                        resultLabel.setText(" Failed to load currencies")
                );
            }
        });
    }

    @FXML
    public void handleConvert() {
        String from = fromCurrency.getValue();
        String to = toCurrency.getValue();
        String amountText = amountField.getText().trim();

        if (from == null || to == null || amountText.isEmpty()) {
            resultLabel.setText(" Fill all fields");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            Exchange api = com.mmhachem.exchange.api.model.ExchangeService.exchangeApi();
            String token = "Bearer " + Authentication.getInstance().getToken();

            ConversionRequest request = new ConversionRequest(from, to, amount);
            api.convertCurrency(request, token).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ConversionResponse> call, Response<ConversionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Platform.runLater(() ->
                                resultLabel.setText(" Result: " + response.body().getResult() + " " + to)
                        );
                    } else {
                        Platform.runLater(() ->
                                resultLabel.setText(" Failed to convert")
                        );
                    }
                }

                @Override
                public void onFailure(Call<ConversionResponse> call, Throwable t) {
                    Platform.runLater(() ->
                            resultLabel.setText(" Error: " + t.getMessage())
                    );
                }
            });

        } catch (NumberFormatException e) {
            resultLabel.setText(" Invalid amount format");
        }
    }
}
