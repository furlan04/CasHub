package it.unimib.CasHub.model;

import com.google.gson.annotations.SerializedName;

public class StockQuote {

    @SerializedName("01. symbol")
    private String symbol;

    @SerializedName("05. price")
    private String price;

    @SerializedName("09. change")
    private String change;

    @SerializedName("10. change percent")
    private String changePercent;

    @SerializedName("08. previous close")
    private String previousClose;

    @SerializedName("06. volume")
    private String volume;

    // Campi aggiuntivi che non vengono dall'API ma che settiamo manualmente
    private String name;
    private String currency;
    private String exchange;

    public StockQuote() {}


    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public String getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(String previousClose) {
        this.previousClose = previousClose;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}
