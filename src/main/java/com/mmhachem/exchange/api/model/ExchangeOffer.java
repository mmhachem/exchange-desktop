package com.mmhachem.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class ExchangeOffer {
    @SerializedName("id")
    public Integer id;

    @SerializedName("user_id")
    public Integer userId;

    @SerializedName("offer_type")
    public String offerType;

    @SerializedName("usd_amount")
    public Float usdAmount;

    @SerializedName("rate")
    public Float rate;

    @SerializedName("status")
    public String status;

    public ExchangeOffer(String offerType, Float usdAmount, Float rate) {
        this.offerType = offerType;
        this.usdAmount = usdAmount;
        this.rate = rate;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getOfferType() {
        return offerType;
    }

    public Float getUsdAmount() {
        return usdAmount;
    }

    public Float getRate() {
        return rate;
    }

    public String getStatus() {
        return status;
    }
}
