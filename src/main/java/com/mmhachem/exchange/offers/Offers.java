package com.mmhachem.exchange.offers;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import retrofit2.Call;
import retrofit2.Response;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.text.ParseException;

public class Offers implements Initializable {
    @FXML
    private TableView<ExchangeOffer> offersTable;
    @FXML
    private TableColumn<ExchangeOffer, Integer> idColumn;
    @FXML
    private TableColumn<ExchangeOffer, String> typeColumn;
    @FXML
    private TableColumn<ExchangeOffer, Float> amountColumn;
    @FXML
    private TableColumn<ExchangeOffer, Float> rateColumn;
    @FXML
    private TableColumn<ExchangeOffer, String> lbpEquivalentColumn;
    @FXML
    private TableColumn<ExchangeOffer, Void> actionColumn;

    @FXML
    private RadioButton buyRadio;
    @FXML
    private RadioButton sellRadio;
    @FXML
    private TextField usdAmountField;
    @FXML
    private TextField rateField;
    @FXML
    private Label lbpAmountLabel;
    @FXML
    private Label createOfferStatus;

    @FXML
    private TableView<Notification> notificationsTable;
    @FXML
    private TableColumn<Notification, String> notifTypeColumn;
    @FXML
    private TableColumn<Notification, String> notifMessageColumn;
    @FXML
    private TableColumn<Notification, String> notifTimestampColumn;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final SimpleDateFormat readableFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().offerType.equals("buy") ? "Buy USD" : "Sell USD"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("usdAmount"));
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        lbpEquivalentColumn.setCellValueFactory(cellData -> {
            ExchangeOffer offer = cellData.getValue();
            return new SimpleStringProperty(String.format("%,.0f", offer.usdAmount * offer.rate));
        });

        // Setup action column
        setupActionColumn();

        // Setup notifications table
        notifTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().type));
        notifMessageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        notifTimestampColumn.setCellValueFactory(cellData -> {
            try {
                Date date = dateFormat.parse(cellData.getValue().timestamp);
                return new SimpleStringProperty(readableFormat.format(date));
            } catch (ParseException e) {
                return new SimpleStringProperty(cellData.getValue().timestamp);
            }
        });

        // Add listeners to calculate LBP amount
        ChangeListener<Object> updateLbpAmount = (observable, oldValue, newValue) -> calculateLbpAmount();
        usdAmountField.textProperty().addListener(updateLbpAmount);
        rateField.textProperty().addListener(updateLbpAmount);

        // Initial fetch
        refreshOffers();
        refreshNotifications();
    }

    private void calculateLbpAmount() {
        try {
            float usd = Float.parseFloat(usdAmountField.getText());
            float rate = Float.parseFloat(rateField.getText());
            float lbp = usd * rate;
            lbpAmountLabel.setText(String.format("%,.0f", lbp));
        } catch (NumberFormatException e) {
            lbpAmountLabel.setText("0");
        }
    }

    @FXML
    private void refreshOffers() {
        ExchangeService.exchangeApi().getOffers().enqueue(new retrofit2.Callback<List<ExchangeOffer>>() {
            @Override
            public void onResponse(Call<List<ExchangeOffer>> call, Response<List<ExchangeOffer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ExchangeOffer> offers = response.body();
                    javafx.application.Platform.runLater(() -> {
                        offersTable.getItems().setAll(offers);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ExchangeOffer>> call, Throwable t) {
                System.err.println("Failed to load com.mmhachem.exchange.offers: " + t.getMessage());
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<ExchangeOffer, Void> call(final TableColumn<ExchangeOffer, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Request");

                    {
                        btn.setOnAction(event -> {
                            ExchangeOffer offer = getTableView().getItems().get(getIndex());
                            sendRequest(offer.id);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        });
    }

    private void sendRequest(int offerId) {
        String token = Authentication.getInstance().getToken();
        if (token == null) return;

        ExchangeService.exchangeApi().requestOffer(offerId, "Bearer " + token).enqueue(new retrofit2.Callback<OfferRequest>() {
            @Override
            public void onResponse(Call<OfferRequest> call, Response<OfferRequest> response) {
                if (response.isSuccessful()) {
                    javafx.application.Platform.runLater(() -> {
                        createOfferStatus.setText("Offer requested successfully!");
                        refreshNotifications();
                    });
                } else {
                    javafx.application.Platform.runLater(() -> createOfferStatus.setText("Request failed."));
                }
            }

            @Override
            public void onFailure(Call<OfferRequest> call, Throwable t) {
                System.err.println("Request failed: " + t.getMessage());
                javafx.application.Platform.runLater(() -> createOfferStatus.setText("Error sending request."));
            }
        });
    }

    @FXML
    private void createOffer() {
        try {
            String offerType = buyRadio.isSelected() ? "buy" : "sell";
            float usd = Float.parseFloat(usdAmountField.getText());
            float rate = Float.parseFloat(rateField.getText());

            ExchangeOffer offer = new ExchangeOffer(offerType, usd, rate);
            String token = Authentication.getInstance().getToken();

            ExchangeService.exchangeApi().createOffer(offer, "Bearer " + token).enqueue(new retrofit2.Callback<>() {
                @Override
                public void onResponse(Call<ExchangeOffer> call, Response<ExchangeOffer> response) {
                    if (response.isSuccessful()) {
                        javafx.application.Platform.runLater(() -> {
                            createOfferStatus.setText("Offer created!");
                            refreshOffers();
                        });
                    } else {
                        javafx.application.Platform.runLater(() -> createOfferStatus.setText("Failed to create offer."));
                    }
                }

                @Override
                public void onFailure(Call<ExchangeOffer> call, Throwable t) {
                    javafx.application.Platform.runLater(() -> createOfferStatus.setText("Error: " + t.getMessage()));
                }
            });
        } catch (NumberFormatException e) {
            createOfferStatus.setText("Invalid input.");
        }
    }

    @FXML
    private void refreshNotifications() {
        String token = Authentication.getInstance().getToken();
        if (token == null) return;

        ExchangeService.exchangeApi().getNotifications("Bearer " + token).enqueue(new retrofit2.Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    javafx.application.Platform.runLater(() -> notificationsTable.getItems().setAll(response.body()));
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                System.err.println("Failed to load notifications: " + t.getMessage());
            }
        });
    }
}