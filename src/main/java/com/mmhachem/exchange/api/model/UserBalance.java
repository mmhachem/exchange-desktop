package com.mmhachem.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class UserBalance {
    @SerializedName("usd_balance")
    public Float usdBalance;

    @SerializedName("lbp_balance")
    public Float lbpBalance;

    @SerializedName("points")
    public Integer points;
}