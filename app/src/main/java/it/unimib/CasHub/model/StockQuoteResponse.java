package it.unimib.CasHub.model;

import com.google.gson.annotations.SerializedName;

public class StockQuoteResponse {

    @SerializedName("Global Quote")
    private StockQuote globalQuote;

    public StockQuote getGlobalQuote() {
        return globalQuote;
    }

    public void setGlobalQuote(StockQuote globalQuote) {
        this.globalQuote = globalQuote;
    }
}
