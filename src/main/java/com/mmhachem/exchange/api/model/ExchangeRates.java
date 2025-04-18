package com.mmhachem.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class ExchangeRates {
    @SerializedName("usd_to_lbp_rate")
    public Float usdToLbp;

    @SerializedName("lbp_to_usd_rate")
    public Float lbpToUsd;
}
