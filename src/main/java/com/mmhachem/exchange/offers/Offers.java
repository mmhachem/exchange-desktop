package com.mmhachem.exchange.offers;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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


    @FXML private TableView<ExchangeOffer> myOffersTable;
    @FXML private TableColumn<ExchangeOffer, Integer> myOfferIdColumn;
    @FXML private TableColumn<ExchangeOffer, String> myOfferTypeColumn;
    @FXML private TableColumn<ExchangeOffer, Float> myOfferAmountColumn;
    @FXML private TableColumn<ExchangeOffer, Float> myOfferRateColumn;

    @FXML private TableView<OfferRequest> requestsTable;
    @FXML private TableColumn<OfferRequest, Integer> requestIdColumn;
    @FXML private TableColumn<OfferRequest, String> requestStatusColumn;
    @FXML private TableColumn<OfferRequest, Void> requestActionColumn;


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

        // Setup com.mmhachem.exchange.notifications table


        // Add listeners to calculate LBP amount
        ChangeListener<Object> updateLbpAmount = (observable, oldValue, newValue) -> calculateLbpAmount();
        usdAmountField.textProperty().addListener(updateLbpAmount);
        rateField.textProperty().addListener(updateLbpAmount);

        // Initial fetch
        refreshOffers();
        refreshMyOffers();

        myOfferIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        myOfferTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().offerType.equals("buy") ? "Buy" : "Sell"));
        myOfferAmountColumn.setCellValueFactory(new PropertyValueFactory<>("usdAmount"));
        myOfferRateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        myOffersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadRequestsForOffer(newVal.id);
            }
        });

        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        setupRequestActionColumn();

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
    private void refreshMyOffers() {
        String token = Authentication.getInstance().getToken();
        if (token == null) return;

        ExchangeService.exchangeApi().getOffers().enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<ExchangeOffer>> call, Response<List<ExchangeOffer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ExchangeOffer> all = response.body();
                    int myId = Authentication.getInstance().getUserId(); // implement getUserId() if needed
                    List<ExchangeOffer> mine = all.stream().filter(o -> o.userId != null && o.userId.equals(myId)).toList();
                    javafx.application.Platform.runLater(() -> {
                        myOffersTable.getItems().setAll(mine);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ExchangeOffer>> call, Throwable t) {
                System.err.println("Failed to load my offers: " + t.getMessage());
            }
        });
    }

    private void loadRequestsForOffer(int offerId) {
        String token = Authentication.getInstance().getToken();
        ExchangeService.exchangeApi().getOfferRequests(offerId, "Bearer " + token).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<OfferRequest>> call, Response<List<OfferRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    javafx.application.Platform.runLater(() -> {
                        requestsTable.getItems().setAll(response.body());
                    });
                }
            }

            @Override
            public void onFailure(Call<List<OfferRequest>> call, Throwable t) {
                System.err.println("Failed to load requests: " + t.getMessage());
            }
        });
    }

    private void setupRequestActionColumn() {
        requestActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptBtn = new Button("Accept");
            private final Button rejectBtn = new Button("Reject");
            private final HBox box = new HBox(5, acceptBtn, rejectBtn);

            {
                acceptBtn.setOnAction(e -> {
                    OfferRequest req = getTableView().getItems().get(getIndex());
                    handleRequestAction(req.id, true);
                });
                rejectBtn.setOnAction(e -> {
                    OfferRequest req = getTableView().getItems().get(getIndex());
                    handleRequestAction(req.id, false);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void handleRequestAction(int requestId, boolean approve) {
        String token = Authentication.getInstance().getToken();
        Call<Object> call = approve
                ? ExchangeService.exchangeApi().approveRequest(requestId, "Bearer " + token)
                : ExchangeService.exchangeApi().rejectRequest(requestId, "Bearer " + token);

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    javafx.application.Platform.runLater(() -> {
                        refreshMyOffers(); // refresh UI
                        requestsTable.getItems().clear();
                    });
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                System.err.println("Failed to update request: " + t.getMessage());
            }
        });
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


}