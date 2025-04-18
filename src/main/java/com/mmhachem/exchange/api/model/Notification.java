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

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Integer getOfferId() {
        return offerId;
    }

    public String getStatus() {
        return status;
    }
}

