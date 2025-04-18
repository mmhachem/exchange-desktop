package rates;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import com.mmhachem.exchange.api.model.ExchangeService;
import com.mmhachem.exchange.api.model.ExchangeRates;
import com.mmhachem.exchange.api.model.Transaction;
import com.mmhachem.exchange.Authentication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Rates {

    @FXML
    public Label buyUsdRateLabel;

    @FXML
    public Label sellUsdRateLabel;

    @FXML
    public TextField lbpTextField;

    @FXML
    public TextField usdTextField;

    @FXML
    public ToggleGroup transactionType;


    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<String> directionBox;
    @FXML
    private Label resultLabel;

    @FXML
    public void initialize() {
        fetchRates();
        usdTextField.setOnKeyTyped(event -> setClipboard(null));
        lbpTextField.setOnKeyTyped(event -> setClipboard(null));
        directionBox.getItems().addAll("USD to LBP", "LBP to USD");

    }

    private void setClipboard(String value) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(value == null ? "" : value);
        clipboard.setContent(content);
    }

    private void fetchRates() {
        ExchangeService.exchangeApi().getExchangeRates().enqueue(new Callback<ExchangeRates>() {
            @Override
            public void onResponse(Call<ExchangeRates> call, Response<ExchangeRates> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExchangeRates exchangeRates = response.body();
                    Platform.runLater(() -> {
                        buyUsdRateLabel.setText(
                                exchangeRates.lbpToUsd != null ? exchangeRates.lbpToUsd.toString() : "N/A"
                        );
                        sellUsdRateLabel.setText(
                                exchangeRates.usdToLbp != null ? exchangeRates.usdToLbp.toString() : "N/A"
                        );
                    });

                } else {
                    System.err.println("Failed to fetch rates. HTTP Status: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ExchangeRates> call, Throwable throwable) {
                System.err.println("Network error: " + throwable.getMessage());
            }
        });
    }

    public void addTransaction(ActionEvent actionEvent) {
        try {
            String usdInput = usdTextField.getText().trim();
            String lbpInput = lbpTextField.getText().trim();

            if (!usdInput.matches("^\\d+(\\.\\d{1,2})?$") || !lbpInput.matches("^\\d+(\\.\\d{1,2})?$")) {
                throw new IllegalArgumentException("Invalid currency values");
            }

            float usdAmount = Float.parseFloat(usdInput);
            float lbpAmount = Float.parseFloat(lbpInput);

            if (usdAmount <= 0 || lbpAmount <= 0) {
                throw new IllegalArgumentException("Amounts must be greater than zero.");
            }

            if (transactionType.getSelectedToggle() == null) {
                throw new IllegalArgumentException("Please select Buy or Sell USD.");
            }

            boolean isSellingUsd = ((RadioButton) transactionType.getSelectedToggle())
                    .getText().equals("Sell USD");

            Transaction transaction = new Transaction(usdAmount, lbpAmount, isSellingUsd);

            String userToken = Authentication.getInstance().getToken();
            String authHeader = userToken != null ? "Bearer " + userToken : null;

            ExchangeService.exchangeApi().addTransaction(transaction, authHeader).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    fetchRates();
                    Platform.runLater(() -> {
                        usdTextField.setText("");
                        lbpTextField.setText("");
                        if (com.mmhachem.exchange.Parent.instance != null) {
                            com.mmhachem.exchange.Parent.instance.swapContent(
                                    com.mmhachem.exchange.Parent.Section.RATES
                            );
                        }
                    });
                }

                @Override
                public void onFailure(Call<Object> call, Throwable throwable) {
                    System.err.println("Transaction failed: " + throwable.getMessage());
                }
            });

        } catch (IllegalArgumentException e) {
            System.err.println("Input error: " + e.getMessage());
        }
    }

    @FXML
    public void onCalculate() {
        String amountText = amountField.getText().trim();
        String direction = directionBox.getValue();

        if (amountText.isEmpty() || direction == null) {
            resultLabel.setText("Please enter amount and select direction.");
            return;
        }

        try {
            float amount = Float.parseFloat(amountText);

            if (amount <= 0) {
                resultLabel.setText("Amount must be positive.");
                return;
            }

            if (direction.equals("USD to LBP")) {
                float rate = Float.parseFloat(buyUsdRateLabel.getText());
                float result = amount * rate;
                resultLabel.setText(String.format("%.2f USD = %.2f LBP", amount, result));
            } else if (direction.equals("LBP to USD")) {
                float rate = Float.parseFloat(sellUsdRateLabel.getText());
                float result = amount / rate;
                resultLabel.setText(String.format("%.2f LBP = %.2f USD", amount, result));
            } else {
                resultLabel.setText("Please select a valid conversion direction.");
            }

        } catch (NumberFormatException e) {
            resultLabel.setText("Invalid number format.");
        }
    }

}
