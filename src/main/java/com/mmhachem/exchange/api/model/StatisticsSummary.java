package com.mmhachem.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class StatisticsSummary {
    @SerializedName("highest_rate")
    public Float highestRate;

    @SerializedName("lowest_rate")
    public Float lowestRate;

    @SerializedName("average_rate")
    public Float averageRate;

    @SerializedName("median_rate")
    public Float medianRate;

    @SerializedName("volatility")
    public Float volatility;

    @SerializedName("total_users")
    public Integer totalUsers;

    @SerializedName("total_transactions")
    public Integer totalTransactions;

    @SerializedName("total_volume_usd")
    public Float totalVolumeUsd;

    @SerializedName("total_volume_lbp")
    public Float totalVolumeLbp;

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("end_date")
    public String endDate;
}