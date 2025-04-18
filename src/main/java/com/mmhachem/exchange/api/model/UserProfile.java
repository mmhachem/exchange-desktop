package com.mmhachem.exchange.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserProfile {
    @SerializedName("user_name")
    public String userName;

    @SerializedName("usd_balance")
    public Float usdBalance;

    @SerializedName("lbp_balance")
    public Float lbpBalance;

    @SerializedName("points")
    public Integer points;

    @SerializedName("level")
    public String level;

    @SerializedName("badges")
    public List<String> badges;
}
