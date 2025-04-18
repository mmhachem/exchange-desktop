package com.mmhachem.exchange.statistics;

import com.mmhachem.exchange.api.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class Statistics implements Initializable {
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> granularityComboBox;
    @FXML
    private LineChart<String, Number> rateHistoryChart;
    @FXML
    private Label highestRateLabel;
    @FXML
    private Label lowestRateLabel;
    @FXML
    private Label averageRateLabel;
    @FXML
    private Label medianRateLabel;
    @FXML
    private Label volatilityLabel;
    @FXML
    private Label totalTransactionsLabel;
    @FXML
    private Label totalVolumeUsdLabel;
    @FXML
    private Label totalVolumeLbpLabel;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label predictionLabel;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize date pickers
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        startDatePicker.setValue(threeDaysAgo);
        endDatePicker.setValue(today);

        // Initialize granularity combo box
        granularityComboBox.getItems().addAll("hourly", "daily", "monthly", "yearly");
        granularityComboBox.setValue("daily");

        // Load initial data
        applyFilter();
        calculatePrediction();
    }

    @FXML
    private void applyFilter() {
        loadStatistics();
        loadRateHistory();
    }

    @FXML
    private void calculatePrediction() {
        ExchangeService.exchangeApi().predictRate().enqueue(new Callback<RatePrediction>() {
            @Override
            public void onResponse(Call<RatePrediction> call, Response<RatePrediction> response) {
                if (response.isSuccessful() && response.body() != null) {
                    javafx.application.Platform.runLater(() ->
                            predictionLabel.setText(String.format("%,.2f LBP/USD", response.body().predictedRate)));
                } else {
                    javafx.application.Platform.runLater(() ->
                            predictionLabel.setText("Prediction not available"));
                }
            }

            @Override
            public void onFailure(Call<RatePrediction> call, Throwable throwable) {
                javafx.application.Platform.runLater(() ->
                        predictionLabel.setText("Error fetching prediction"));
                System.err.println("Failed to get rate prediction: " + throwable.getMessage());
            }
        });
    }

    private void loadStatistics() {
        String startDate = startDatePicker.getValue().format(formatter);
        String endDate = endDatePicker.getValue().format(formatter);

        ExchangeService.exchangeApi().getStatistics(startDate, endDate).enqueue(new Callback<StatisticsSummary>() {
            @Override
            public void onResponse(Call<StatisticsSummary> call, Response<StatisticsSummary> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StatisticsSummary stats = response.body();
                    javafx.application.Platform.runLater(() -> {
                        highestRateLabel.setText(stats.highestRate != null ? String.format("%,.2f", stats.highestRate) : "N/A");
                        lowestRateLabel.setText(stats.lowestRate != null ? String.format("%,.2f", stats.lowestRate) : "N/A");
                        averageRateLabel.setText(stats.averageRate != null ? String.format("%,.2f", stats.averageRate) : "N/A");
                        medianRateLabel.setText(stats.medianRate != null ? String.format("%,.2f", stats.medianRate) : "N/A");
                        volatilityLabel.setText(stats.volatility != null ? String.format("%,.2f", stats.volatility) : "N/A");
                        totalTransactionsLabel.setText(String.valueOf(stats.totalTransactions));
                        totalVolumeUsdLabel.setText(String.format("%,.2f", stats.totalVolumeUsd));
                        totalVolumeLbpLabel.setText(String.format("%,.2f", stats.totalVolumeLbp));
                        totalUsersLabel.setText(String.valueOf(stats.totalUsers));
                    });
                }
            }

            @Override
            public void onFailure(Call<StatisticsSummary> call, Throwable throwable) {
                System.err.println("Failed to get com.mmhachem.exchange.statistics: " + throwable.getMessage());
            }
        });
    }

    private void loadRateHistory() {
        String startDate = startDatePicker.getValue().format(formatter);
        String endDate = endDatePicker.getValue().format(formatter);
        String granularity = granularityComboBox.getValue();

        ExchangeService.exchangeApi().getRateHistory(startDate, endDate, granularity).enqueue(new Callback<List<RateHistoryEntry>>() {
            @Override
            public void onResponse(Call<List<RateHistoryEntry>> call, Response<List<RateHistoryEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RateHistoryEntry> history = response.body();

                    javafx.application.Platform.runLater(() -> {
                        rateHistoryChart.getData().clear();

                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName("Exchange Rate (LBP/USD)");

                        for (RateHistoryEntry entry : history) {
                            series.getData().add(new XYChart.Data<>(entry.period, entry.averageRate));
                        }

                        rateHistoryChart.getData().add(series);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<RateHistoryEntry>> call, Throwable throwable) {
                System.err.println("Failed to get rate history: " + throwable.getMessage());
            }
        });
    }
}