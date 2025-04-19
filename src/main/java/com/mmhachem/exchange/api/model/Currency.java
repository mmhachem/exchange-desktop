package com.mmhachem.exchange.api.model;

public class Currency {
    private String code;
    private String name;
    private String symbol;
    private double rate_to_usd;

    public Currency(String code, String name, String symbol, double rate_to_usd) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.rate_to_usd = rate_to_usd;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getRate_to_usd() {
        return rate_to_usd;
    }
}
