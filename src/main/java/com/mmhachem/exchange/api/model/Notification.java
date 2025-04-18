package com.mmhachem.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("type")
    public String type;

    @SerializedName("message")
    public String message;

    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("offer_id")
    public Integer offerId;

    @SerializedName("status")
    public String status;
}
