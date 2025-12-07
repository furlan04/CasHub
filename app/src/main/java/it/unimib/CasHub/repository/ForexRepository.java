package it.unimib.CasHub.repository;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimib.CasHub.database.CurrencyRoomDatabase;
import it.unimib.CasHub.model.CurrencyEntity;
import it.unimib.CasHub.database.RatesRoomDatabase;
import it.unimib.CasHub.model.RateEntity;
import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.service.ForexAPIService;
import it.unimib.CasHub.utils.ResponseCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForexRepository implements ForexRepositoryInterface {

    private final ForexAPIService apiService;
    private final CurrencyRoomDatabase currencyDB;
    private final RatesRoomDatabase ratesDB;

    public ForexRepository(ForexAPIService apiService,
                           CurrencyRoomDatabase currencyDB,
                           RatesRoomDatabase ratesDB) {
        this.apiService = apiService;
        this.currencyDB = currencyDB;
        this.ratesDB = ratesDB;
    }

    @Override
    public void getRates(String base, ResponseCallback callback) {
        apiService.getRates(base).enqueue(new Callback<ForexAPIResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForexAPIResponse> call,
                                   @NonNull Response<ForexAPIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForexAPIResponse apiResp = response.body();
                    long timestamp = System.currentTimeMillis();

                    // salva nel DB dei tassi
                    new Thread(() -> {
                        List<RateEntity> entities = new ArrayList<>();
                        for (Map.Entry<String, Double> entry : apiResp.getRates().entrySet()) {
                            entities.add(new RateEntity(
                                    entry.getKey(),
                                    entry.getValue(),
                                    apiResp.getAmount(),
                                    apiResp.getBase(),
                                    apiResp.getDate()
                            ));
                        }
                        ratesDB.ratesDao().insertAllRates(entities);
                    }).start();

                    callback.onRatesSuccess(apiResp, timestamp);
                } else {
                    fallbackRates(base, callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForexAPIResponse> call, @NonNull Throwable t) {
                fallbackRates(base, callback);
            }
        });
    }

    private void fallbackRates(String base, ResponseCallback callback) {
        new Thread(() -> {
            List<RateEntity> allRates = ratesDB.ratesDao().getAllRates();
            List<RateEntity> cached = new ArrayList<>();
            for (RateEntity r : allRates) {
                if (base != null && base.equals(r.base)) {
                    cached.add(r);
                }
            }
            if (!cached.isEmpty()) {
                Map<String, Double> rates = new HashMap<>();
                long timestamp = System.currentTimeMillis();
                for (RateEntity r : cached) {
                    rates.put(r.currencyCode, r.rateValue);
                }
                ForexAPIResponse resp = new ForexAPIResponse(
                        cached.get(0).amount,
                        base,
                        cached.get(0).date != null ? cached.get(0).date : "",
                        rates
                );
                callback.onRatesSuccess(resp, timestamp);
            } else {
                callback.onFailure("Nessuna rete e nessun dato salvato.");
            }
        }).start();
    }

    @Override
    public void getCurrencies(ResponseCallback callback) {
        apiService.getCurrencies().enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call,
                                   @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> currenciesMap = response.body();
                    long timestamp = System.currentTimeMillis();

                    List<Currency> list = new ArrayList<>();
                    List<CurrencyEntity> entities = new ArrayList<>();

                    if (currenciesMap != null && !currenciesMap.isEmpty()) {
                        for (Map.Entry<String, String> entry : currenciesMap.entrySet()) {
                            list.add(new Currency(entry.getKey(), entry.getValue()));
                            entities.add(new CurrencyEntity(entry.getKey(), entry.getValue()));
                        }
                        System.out.println("Valute caricate: " + list.size()); // Debug log
                    } else {
                        System.out.println("Mappa valute vuota o null"); // Debug log
                    }

                    // salva nel DB delle valute
                    new Thread(() -> currencyDB.currencyDao().insertAllCurrencies(entities)).start();

                    callback.onCurrencyListSuccess(list, timestamp);
                } else {
                    String errorMsg = response.errorBody() != null ? response.errorBody().toString() : "Risposta non valida";
                    System.out.println("Errore risposta API: " + errorMsg); // Debug log
                    fallbackCurrencies(callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                t.printStackTrace(); // Log dell'errore per debug
                fallbackCurrencies(callback);
            }
        });
    }

    private void fallbackCurrencies(ResponseCallback callback) {
        new Thread(() -> {
            List<CurrencyEntity> cached = currencyDB.currencyDao().getAllCurrencies();
            if (!cached.isEmpty()) {
                List<Currency> list = new ArrayList<>();
                long ts = System.currentTimeMillis();
                for (CurrencyEntity c : cached) {
                    list.add(new Currency(c.code, c.name));
                }
                callback.onCurrencyListSuccess(list, ts);
            } else {
                callback.onFailure("Nessuna rete e nessuna valuta salvata.");
            }
        }).start();
    }
}
