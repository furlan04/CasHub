package it.unimib.CasHub.model;

import  java.util.Map;

public class ForexAPIResponse {
    private double amount;
    private String base;
    private String date;
    private Map<String, Double> rates;
    private Map<String, String> currencies;

    public ForexAPIResponse(double amount, String base, String date, Map<String, Double> rates) {
        this.amount = amount;
        this.base = base;
        this.date = date;
        this.rates = rates;
    }

    public ForexAPIResponse() {
    }

    public double getAmount() {
        return amount;
    }

    public String getBase() {
        return base;
    }

    public String getDate() {
        return date;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public Double getRateFor(String currency) {
        return rates.get(currency);
    }

    public Map<String, String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Map<String, String> currencies) {
        this.currencies = currencies;
    }
}
