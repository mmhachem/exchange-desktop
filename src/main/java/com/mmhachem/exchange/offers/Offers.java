package com.mmhachem.exchange.offers;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.*;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Offers implements Initializable {
    @FXML private VBox offersContainer;
    @FXML private VBox requestsContainer;
    @FXML private TableColumn<ExchangeOffer, String> typeColumn;
    @FXML private TableColumn<ExchangeOffer, Float> amountColumn;
    @FXML private TableColumn<ExchangeOffer, Float> rateColumn;
    @FXML private TableColumn<ExchangeOffer, String> lbpEquivalentColumn;
    @FXML private TableColumn<ExchangeOffer, Void> actionColumn;

    @FXML private RadioButton buyRadio;
    @FXML private RadioButton sellRadio;
    @FXML private TextField usdAmountField;
    @FXML private TextField rateField;
    @FXML private Label lbpAmountLabel;
    @FXML private Label createOfferStatus;
    @FXML
    private VBox myOffersContainer;
    private Optional<Integer> currentSelectedOfferId = Optional.empty();


    private final Map<Integer, String> requesterCache = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ToggleGroup offerTypeGroup = new ToggleGroup();
        buyRadio.setToggleGroup(offerTypeGroup);
        sellRadio.setToggleGroup(offerTypeGroup);

        ChangeListener<Object> updateLbp = (obs, oldVal, newVal) -> calculateLbpAmount();
        usdAmountField.textProperty().addListener(updateLbp);
        rateField.textProperty().addListener(updateLbp);

        refreshOffers();
        refreshMyOffers();
    }

    private void calculateLbpAmount() {
        try {
            float usd = Float.parseFloat(usdAmountField.getText());
            float rate = Float.parseFloat(rateField.getText());
            lbpAmountLabel.setText(String.format("%,.0f", usd * rate));
        } catch (NumberFormatException e) {
            lbpAmountLabel.setText("0");
        }
    }

    @FXML
    private void refreshOffers() {
        ExchangeService.exchangeApi().getOffers().enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<ExchangeOffer>> call, Response<List<ExchangeOffer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ExchangeOffer> offers = response.body();
                    javafx.application.Platform.runLater(() -> {
                        offersContainer.getChildren().clear();
                        for (ExchangeOffer offer : offers) {
                            offersContainer.getChildren().add(createOfferCard(offer));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ExchangeOffer>> call, Throwable t) {
                System.err.println("Failed to load offers: " + t.getMessage());
            }
        });
    }

    private VBox createOfferCard(ExchangeOffer offer) {
        Label type = new Label((offer.offerType.equals("buy") ? "Buy USD 💰" : "Sell USD 💵"));
        Label usd = new Label("USD: " + offer.usdAmount);
        Label rate = new Label("Rate: " + offer.rate);
        Label status = new Label("Status: " + offer.status);

        VBox info = new VBox(5, type, usd, rate, status);
        info.getStyleClass().add("offer-details");

        HBox card = new HBox(20);
        card.getStyleClass().add("offer-card");
        card.setPadding(new Insets(10));

        if (Authentication.getInstance().getUserId() == offer.userId) {
            card.getChildren().add(info);
            card.setOnMouseClicked(e -> loadRequestsForOffer(offer.id));
        } else {
            Button requestBtn = new Button("Request");
            requestBtn.setOnAction(e -> sendRequest(offer.id));
            card.getChildren().addAll(info, requestBtn);
        }

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(5));
        wrapper.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ccc; -fx-border-radius: 6;");

        return wrapper;
    }


    private void sendRequest(int offerId) {
        String token = Authentication.getInstance().getToken();
        if (token == null) return;

        ExchangeService.exchangeApi().getOffers().enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<ExchangeOffer>> call, Response<List<ExchangeOffer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExchangeOffer selected = response.body().stream()
                            .filter(o -> o.id == offerId)
                            .findFirst().orElse(null);

                    if (selected == null) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Offer not found.");
                        return;
                    }

                    ExchangeService.exchangeApi().getBalance("Bearer " + token).enqueue(new retrofit2.Callback<>() {
                        @Override
                        public void onResponse(Call<UserBalance> call, Response<UserBalance> res) {
                            if (res.isSuccessful() && res.body() != null) {
                                UserBalance b = res.body();
                                float usd = selected.usdAmount;
                                float lbp = usd * selected.rate;

                                boolean hasEnough = selected.offerType.equals("sell")
                                        ? b.lbpBalance != null && b.lbpBalance >= lbp
                                        : b.usdBalance != null && b.usdBalance >= usd;

                                if (!hasEnough) {
                                    javafx.application.Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Insufficient Balance",
                                            selected.offerType.equals("sell")
                                                    ? "You need at least " + (int) lbp + " LBP to request this offer."
                                                    : "You need at least " + usd + " USD to request this offer."));
                                    return;
                                }

                                ExchangeService.exchangeApi().requestOffer(offerId, "Bearer " + token)
                                        .enqueue(new retrofit2.Callback<>() {
                                            @Override
                                            public void onResponse(Call<OfferRequest> call, Response<OfferRequest> r) {
                                                javafx.application.Platform.runLater(() -> {
                                                    if (r.isSuccessful()) {
                                                        showAlert(Alert.AlertType.INFORMATION, "Success", "Offer requested successfully!");
                                                    } else {
                                                        showAlert(Alert.AlertType.ERROR, "Failed", "You already requested this offer.");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(Call<OfferRequest> call, Throwable t) {
                                                javafx.application.Platform.runLater(() ->
                                                        showAlert(Alert.AlertType.ERROR, "Failed", t.getMessage()));
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onFailure(Call<UserBalance> call, Throwable t) {
                            javafx.application.Platform.runLater(() ->
                                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to fetch balance."));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ExchangeOffer>> call, Throwable t) {
                System.err.println("Offer fetch failed: " + t.getMessage());
            }
        });
    }

    private void loadRequestsForOffer(int offerId) {
        String token = Authentication.getInstance().getToken();
        ExchangeService.exchangeApi().getOfferRequests(offerId, "Bearer " + token).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<OfferRequest>> call, Response<List<OfferRequest>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    List<OfferRequest> requests = res.body().stream()
                            .filter(r -> "pending".equalsIgnoreCase(r.status)).toList();

                    for (OfferRequest r : requests) {
                        r.requesterName = requesterCache.computeIfAbsent(r.requesterId, id -> "User #" + id);
                    }

                    javafx.application.Platform.runLater(() -> {
                        requestsContainer.getChildren().clear();
                        for (OfferRequest req : requests) {
                            HBox row = new HBox(10);
                            row.setPadding(new Insets(5));
                            Label label = new Label(req.requesterName);
                            Button accept = new Button("Accept");
                            Button reject = new Button("Reject");

                            accept.setOnAction(e -> handleRequestAction(req.id, true));
                            reject.setOnAction(e -> handleRequestAction(req.id, false));

                            row.getChildren().addAll(label, accept, reject);
                            requestsContainer.getChildren().add(row);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<OfferRequest>> call, Throwable t) {
                System.err.println("Failed to load requests: " + t.getMessage());
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
            public void onResponse(Call<Object> call, Response<Object> res) {
                javafx.application.Platform.runLater(() -> {
                    if (res.isSuccessful()) {
                        if (approve) {
                            refreshOffers(); // if approved, offers might change
                            requestsContainer.getChildren().clear();
                        } else {

                            requestsContainer.getChildren().removeIf(node -> {
                                if (node instanceof HBox row) {
                                    return row.getChildren().stream().anyMatch(child ->
                                            child instanceof Label && ((Label) child).getText().contains("User #"));
                                }
                                return false;
                            });

                            // 🔁 Reselect the offer to reload updated requests
                            currentSelectedOfferId.ifPresent(this::loadRequestsForOffer);
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to process request.");
                    }
                });
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                javafx.application.Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Error", "Request failed: " + t.getMessage()));
            }

            private void loadRequestsForOffer(int offerId) {
                Offers.this.loadRequestsForOffer(offerId); // to call from nested class
            }
        });
    }


    @FXML
    private void createOffer() {
        try {
            String type = buyRadio.isSelected() ? "buy" : "sell";
            float usd = Float.parseFloat(usdAmountField.getText());
            float rate = Float.parseFloat(rateField.getText());
            if (usd <= 0 || rate <= 0) throw new NumberFormatException();

            String token = Authentication.getInstance().getToken();
            float lbpNeeded = usd * rate;

            ExchangeService.exchangeApi().getBalance("Bearer " + token).enqueue(new retrofit2.Callback<>() {
                @Override
                public void onResponse(Call<UserBalance> call, Response<UserBalance> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        UserBalance b = res.body();
                        boolean enough = (type.equals("sell") && b.usdBalance >= usd)
                                || (type.equals("buy") && b.lbpBalance >= lbpNeeded);

                        if (!enough) {
                            javafx.application.Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Insufficient",
                                    type.equals("sell") ? "Not enough USD" : "Not enough LBP"));
                            return;
                        }

                        ExchangeOffer offer = new ExchangeOffer(type, usd, rate);
                        ExchangeService.exchangeApi().createOffer(offer, "Bearer " + token)
                                .enqueue(new retrofit2.Callback<>() {
                                    @Override
                                    public void onResponse(Call<ExchangeOffer> call, Response<ExchangeOffer> r) {
                                        javafx.application.Platform.runLater(() -> {
                                            if (r.isSuccessful()) {
                                                createOfferStatus.setText("Offer created!");
                                                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                                                pause.setOnFinished(e -> createOfferStatus.setText(""));
                                                pause.play();
                                                refreshOffers();
                                                refreshMyOffers();
                                            } else {
                                                createOfferStatus.setText("Creation failed.");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<ExchangeOffer> call, Throwable t) {
                                        javafx.application.Platform.runLater(() ->
                                                createOfferStatus.setText("Error: " + t.getMessage()));
                                    }
                                });
                    }
                }

                @Override
                public void onFailure(Call<UserBalance> call, Throwable t) {
                    javafx.application.Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Balance Error", t.getMessage()));
                }
            });
        } catch (NumberFormatException e) {
            createOfferStatus.setText("Invalid input.");
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev-> createOfferStatus.setText(""));
            pause.play();
        }
    }
    @FXML
    private void refreshMyOffers() {
        String token = Authentication.getInstance().getToken();
        ExchangeService.exchangeApi().getOffers().enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<ExchangeOffer>> call, Response<List<ExchangeOffer>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    int uid = Authentication.getInstance().getUserId();
                    List<ExchangeOffer> mine = res.body().stream()
                            .filter(o -> uid == o.userId).toList();

                    javafx.application.Platform.runLater(() -> {
                        myOffersContainer.getChildren().clear();
                        for (ExchangeOffer offer : mine) {
                            myOffersContainer.getChildren().add(createMyOfferCard(offer));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ExchangeOffer>> call, Throwable t) {
                System.err.println("Failed to load offers: " + t.getMessage());
            }
        });
    }

    private HBox createMyOfferCard(ExchangeOffer offer) {
        Label type = new Label((offer.offerType.equals("buy") ? "Buy USD 💰" : "Sell USD 💳"));
        Label usd = new Label("USD: " + offer.usdAmount);
        Label rate = new Label("Rate: " + offer.rate);
        Label status = new Label("Status: " + offer.status);

        VBox info = new VBox(5, type, usd, rate, status);
        info.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 10;");

        Button dummy = new Button(); // optional: to simulate symmetry like request button space
        dummy.setVisible(false);

        HBox card = new HBox(20, info, dummy);
        card.getStyleClass().add("offer-card"); // 💎 Match style with available offers
        card.setPadding(new Insets(10));

        card.setOnMouseClicked(e -> {
            clearSelections();
            card.setStyle("-fx-border-color: #003049; -fx-border-radius: 10; -fx-background-color: white;");
            currentSelectedOfferId = Optional.of(offer.id);
            loadRequestsForOffer(offer.id);
        });

        return card;
    }

    private void clearSelections() {
        for (javafx.scene.Node node : myOffersContainer.getChildren()) {
            if (node instanceof HBox) {
                node.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10;");
            }
        }
    }




    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

