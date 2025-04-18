package com.mmhachem.exchange.transactions;

import com.mmhachem.exchange.api.model.Transaction;
import com.mmhachem.exchange.api.model.ExchangeService;
import com.mmhachem.exchange.Authentication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Transactions implements Initializable {
    @FXML
    public TableColumn<Transaction, Long> lbpAmount;
    @FXML
    public TableColumn<Transaction, Long> usdAmount;
    @FXML
    public TableColumn<Transaction, String> transactionDate;
    @FXML
    public TableView<Transaction> tableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lbpAmount.setCellValueFactory(new PropertyValueFactory<>("lbp_amount"));
        usdAmount.setCellValueFactory(new PropertyValueFactory<>("usd_amount"));
        transactionDate.setCellValueFactory(new PropertyValueFactory<>("added_date"));

        ExchangeService.exchangeApi().getTransactions("Bearer " + Authentication.getInstance().getToken())
                .enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                        tableView.getItems().setAll(response.body());
                    }

                    @Override
                    public void onFailure(Call<List<Transaction>> call, Throwable throwable) {
                        System.err.println("Failed to load com.mmhachem.exchange.transactions: " + throwable.getMessage());
                    }
                });
    }
}
