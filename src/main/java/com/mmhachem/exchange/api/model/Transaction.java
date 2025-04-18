package com.mmhachem.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("id")
    public Integer id;

    @SerializedName("usd_amount")
    public Float usd_amount;

    @SerializedName("lbp_amount")
    public Float lbp_amount;

    @SerializedName("usd_to_lbp")
    public Boolean usd_to_lbp;

    @SerializedName("added_date")
    public String added_date;

    public String getAdded_date() {
        return added_date;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public Boolean getUsd_to_lbp() {
        return usd_to_lbp;
    }

    public Integer getId() {
        return id;
    }

    public Float getLbp_amount() {
        return lbp_amount;
    }

    public Float getUsd_amount() {
        return usd_amount;
    }

    @SerializedName("user_id")
    public Integer user_id;

    // Constructor
    public Transaction(Float usd_amount, Float lbp_amount, Boolean usd_to_lbp) {
        this.usd_amount = usd_amount;
        this.lbp_amount = lbp_amount;
        this.usd_to_lbp = usd_to_lbp;
    }
}
