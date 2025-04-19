package com.mmhachem.exchange.api.model;

public class ConversionResponse {
    private String from;
    private String to;
    private double amount;
    private double result;

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public double getAmount() { return amount; }
    public double getResult() { return result; }
}
