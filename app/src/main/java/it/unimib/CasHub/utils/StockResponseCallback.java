package it.unimib.CasHub.utils;

import it.unimib.CasHub.model.StockQuote;

public interface StockResponseCallback {
    void onSuccess(StockQuote stockQuote);
    void onFailure(String errorMessage);
}
