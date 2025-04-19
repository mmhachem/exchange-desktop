package com.mmhachem.exchange.currencies;

import com.mmhachem.exchange.Authentication;
import com.mmhachem.exchange.api.model.Currency;
import com.mmhachem.exchange.api.model.ExchangeService;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class Currencies {

    @FXML private TableView<Currency> currencyTable;
    @FXML private TableColumn<Currency, String> codeColumn;
    @FXML private TableColumn<Currency, String> nameColumn;
    @FXML private TableColumn<Currency, String> symbolColumn;
    @FXML private TableColumn<Currency, Double> rateColumn;


    @FXML
    public void initialize() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("rate_to_usd"));

        String token = Authentication.getInstance().getToken();
        ExchangeService.exchangeApi().getCurrencies("Bearer " + token).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Currency>> call, Response<List<Currency>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currencyTable.getItems().setAll(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Currency>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
