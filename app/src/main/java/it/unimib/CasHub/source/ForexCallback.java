package it.unimib.CasHub.source;

import java.util.List;

import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.model.ForexAPIResponse;

public interface ForexCallback {

    // Rates callbacks
    void onRatesSuccessFromRemote(ForexAPIResponse rates, long lastUpdate);
    void onRatesFailureFromRemote(Exception exception);

    // Currencies callbacks
    void onCurrenciesSuccessFromRemote(List<Currency> currencies, long lastUpdate);
    void onCurrenciesFailureFromRemote(Exception exception);
    void onCurrenciesSuccessFromLocal(List<Currency> currencies);
    void onCurrenciesFailureFromLocal(Exception exception);
}
