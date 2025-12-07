package it.unimib.CasHub.utils;

import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.model.ForexAPIResponse;

import java.util.List;

public interface ResponseCallback {
    void onCurrencyListSuccess(List<Currency> currencyList, long lastUpdate);
    void onRatesSuccess(ForexAPIResponse rates, long lastUpdate);
    void onFailure(String errorMessage);
}