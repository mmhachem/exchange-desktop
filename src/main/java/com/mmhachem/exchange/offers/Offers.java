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
    @FXML private TableColumn<OfferRequest, String> requesterNameColumn;
    @FXML private TableColumn<ExchangeOffer, String> myOfferStatusColumn;



    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final SimpleDateFormat readableFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        requesterNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().requesterName));

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
        myOfferStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        myOffersTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ExchangeOffer offer, boolean empty) {
                super.updateItem(offer, empty);
                if (offer == null || empty) {
                    setStyle("");
                } else if ("completed".equals(offer.status)) {
                    setStyle("-fx-background-color: #e0e0e0;");
                } else {
                    setStyle(""); // Reset style for others
                }
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
                    List<OfferRequest> requests = response.body();

                    // For each request, assign a name from the cache or placeholder
                    for (OfferRequest r : requests) {
                        fetchRequesterName(r.requesterId, r);
                    }

                    javafx.application.Platform.runLater(() -> {
                        requestsTable.getItems().setAll(requests);
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
                javafx.application.Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        showAlert(Alert.AlertType.INFORMATION,
                                approve ? "Request Approved" : "Request Rejected",
                                approve ? "The request has been approved and balances updated."
                                        : "The request has been rejected.");
                        refreshMyOffers();
                        ExchangeOffer selected = myOffersTable.getSelectionModel().getSelectedItem();
                        if (selected != null) {
                            loadRequestsForOffer(selected.id);
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Action Failed",
                                "Failed to process request. It may be already handled.");
                    }
                });
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                javafx.application.Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Network Error", "Could not reach the server."));
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

        ExchangeService.exchangeApi().requestOffer(offerId, "Bearer " + token)
                .enqueue(new retrofit2.Callback<>() {
                    @Override
                    public void onResponse(Call<OfferRequest> call, Response<OfferRequest> response) {
                        javafx.application.Platform.runLater(() -> {
                            if (response.isSuccessful()) {
                                showAlert(Alert.AlertType.INFORMATION, "Request Sent", "Offer requested successfully!");
                                refreshMyOffers(); // optional
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Request Failed", "You already requested this offer or it's not available.");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<OfferRequest> call, Throwable t) {
                        javafx.application.Platform.runLater(() ->
                                showAlert(Alert.AlertType.ERROR, "Request Failed", "Error sending request: " + t.getMessage())
                        );
                    }
                });
    }


    @FXML
    private void createOffer() {
        try {
            String offerType = buyRadio.isSelected() ? "buy" : "sell";
            float usd = Float.parseFloat(usdAmountField.getText());
            float rate = Float.parseFloat(rateField.getText());
            String token = Authentication.getInstance().getToken();

            // ✅ Step 1: Get user balance
            ExchangeService.exchangeApi().getBalance("Bearer " + token).enqueue(new retrofit2.Callback<>() {
                @Override
                public void onResponse(Call<UserBalance> call, Response<UserBalance> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserBalance balance = response.body();
                        float requiredLBP = usd * rate;

                        // ✅ Step 2: Check if the user has enough balance
                        boolean hasEnough = (offerType.equals("sell") && balance.usdBalance >= usd)
                                || (offerType.equals("buy") && balance.lbpBalance >= requiredLBP);

                        if (!hasEnough) {
                            javafx.application.Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, "Insufficient Balance",
                                        offerType.equals("sell")
                                                ? "You don't have enough USD to create this offer."
                                                : "You don't have enough LBP to create this offer.");
                            });
                            return;
                        }

                        // ✅ Step 3: Proceed with offer creation
                        ExchangeOffer offer = new ExchangeOffer(offerType, usd, rate);
                        ExchangeService.exchangeApi().createOffer(offer, "Bearer " + token)
                                .enqueue(new retrofit2.Callback<>() {
                                    @Override
                                    public void onResponse(Call<ExchangeOffer> call, Response<ExchangeOffer> offerResponse) {
                                        javafx.application.Platform.runLater(() -> {
                                            if (offerResponse.isSuccessful()) {
                                                createOfferStatus.setText("Offer created!");
                                                refreshOffers();
                                                refreshMyOffers();
                                            } else {
                                                createOfferStatus.setText("Failed to create offer.");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<ExchangeOffer> call, Throwable t) {
                                        javafx.application.Platform.runLater(() ->
                                                createOfferStatus.setText("Error: " + t.getMessage()));
                                    }
                                });

                    } else {
                        javafx.application.Platform.runLater(() ->
                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to check your balance."));
                    }
                }

                @Override
                public void onFailure(Call<UserBalance> call, Throwable t) {
                    javafx.application.Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Network Error", "Could not fetch balance: " + t.getMessage()));
                }
            });

        } catch (NumberFormatException e) {
            createOfferStatus.setText("Invalid input.");
        }
    }





    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private final java.util.Map<Integer, String> requesterCache = new java.util.HashMap<>();

    private void fetchRequesterName(int requesterId, OfferRequest request) {
        // Check if we already fetched this user's name
        if (requesterCache.containsKey(requesterId)) {
            request.requesterName = requesterCache.get(requesterId);
            requestsTable.refresh();
            return;
        }

        // Since we cannot call the backend for another user's profile directly,
        // we fallback to a placeholder name
        String name = "User #" + requesterId;
        requesterCache.put(requesterId, name);
        request.requesterName = name;
        requestsTable.refresh();
    }




}