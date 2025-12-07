package it.unimib.CasHub.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.utils.ResponseCallback;

public class MockForexRepository implements ForexRepositoryInterface {

    @Override
    public void getRates(String base, ResponseCallback callback) {

        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.12);
        rates.put("JPY", 160.0);
        rates.put("GBP", 0.86);

        ForexAPIResponse response = new ForexAPIResponse(
                1.0,
                base,
                "2024-01-01",
                rates
        );

        long fakeTimestamp = System.currentTimeMillis();

        callback.onRatesSuccess(response, fakeTimestamp);
    }

    @Override
    public void getCurrencies(ResponseCallback callback) {

        List<Currency> list = new ArrayList<>();
        list.add(new Currency("EUR", "Euro"));
        list.add(new Currency("USD", "United States Dollar"));
        list.add(new Currency("JPY", "Japanese Yen"));

        long fakeTimestamp = System.currentTimeMillis();

        callback.onCurrencyListSuccess(list, fakeTimestamp);
    }
}
