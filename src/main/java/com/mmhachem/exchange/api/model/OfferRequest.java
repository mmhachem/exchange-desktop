package com.mmhachem.exchange.api.model;

import com.google.gson.annotations.SerializedName;

public class OfferRequest {
    @SerializedName("id")
    public Integer id;

    @SerializedName("offer_id")
    public Integer offerId;

    @SerializedName("requester_id")
    public Integer requesterId;

    @SerializedName("status")
    public String status;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("requester_name")
    public String requesterName; // 👈 Add this



}