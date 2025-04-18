package com.mmhachem.exchange.api.model;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
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

    @GET("/statistics")
    Call<StatisticsSummary> getStatistics(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );

    @GET("/statistics")
    Call<List<StatisticsSummary>> getStatisticsGrouped(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("granularity") String granularity
    );

    @GET("/exchangeRate/history")
    Call<List<RateHistoryEntry>> getRateHistory(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("granularity") String granularity
    );

    @POST("/offers")
    Call<ExchangeOffer> createOffer(
            @Body ExchangeOffer offer,
            @Header("Authorization") String authorization
    );

    @GET("/offers")
    Call<List<ExchangeOffer>> getOffers();

    @POST("/offers/{offerId}/request")
    Call<OfferRequest> requestOffer(
            @Path("offerId") Integer offerId,
            @Header("Authorization") String authorization
    );

    @GET("/offers/{offerId}/requests")
    Call<List<OfferRequest>> getOfferRequests(
            @Path("offerId") Integer offerId,
            @Header("Authorization") String authorization
    );

    @POST("/requests/{requestId}/approve")
    Call<Object> approveRequest(
            @Path("requestId") Integer requestId,
            @Header("Authorization") String authorization
    );

    @POST("/requests/{requestId}/reject")
    Call<Object> rejectRequest(
            @Path("requestId") Integer requestId,
            @Header("Authorization") String authorization
    );

    @GET("/notifications")
    Call<List<Notification>> getNotifications(
            @Header("Authorization") String authorization
    );

    @GET("/balance")
    Call<UserBalance> getBalance(
            @Header("Authorization") String authorization
    );

    @POST("/balance")
    Call<UserBalance> updateBalance(
            @Body UserBalance balance,
            @Header("Authorization") String authorization
    );

    @GET("/profile")
    Call<UserProfile> getProfile(
            @Header("Authorization") String authorization
    );

    @GET("/predictRate")
    Call<RatePrediction> predictRate();


}

