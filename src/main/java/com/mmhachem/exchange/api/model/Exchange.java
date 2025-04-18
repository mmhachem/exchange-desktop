package com.mmhachem.exchange.api.model;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import java.util.List;
import retrofit2.http.Header;
import retrofit2.http.Query;


public interface Exchange {
    @POST("/user")
    Call<User> addUser(@Body User user);

    @POST("/authenticate")
    Call<Token> authenticate(@Body User user);

    @GET("/exchangeRate")
    Call<ExchangeRates> getExchangeRates();

    @POST("/transaction")
    Call<Object> addTransaction(@Body Transaction transaction, @Header("Authorization") String authorization);

    @GET("/transaction")
    Call<List<Transaction>> getTransactions(@Header("Authorization") String authorization);


}

