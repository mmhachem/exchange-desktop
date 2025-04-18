package com.mmhachem.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class RateHistoryEntry {
    @SerializedName("period")
    public String period;

    @SerializedName("average_rate")
    public Float averageRate;

    @SerializedName("transactions")
    public Integer transactions;
}