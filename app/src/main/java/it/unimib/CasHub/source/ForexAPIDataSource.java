package it.unimib.CasHub.source;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimib.CasHub.model.CurrencyEntity;
import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.service.ForexAPIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForexAPIDataSource extends BaseForexDataSource {

    private final ForexAPIService forexAPIService;

    public ForexAPIDataSource(ForexAPIService forexAPIService) {
        this.forexAPIService = forexAPIService;
    }

    @Override
    public void getRates(String base) {
        forexAPIService.getRates(base).enqueue(new Callback<ForexAPIResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForexAPIResponse> call, @NonNull Response<ForexAPIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onRatesSuccessFromRemote(response.body(), System.currentTimeMillis());
                } else {
                    callback.onRatesFailureFromRemote(new Exception("Remote: Network error or empty response for rates"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForexAPIResponse> call, @NonNull Throwable t) {
                callback.onRatesFailureFromRemote(new Exception(t));
            }
        });
    }

    @Override
    public void getCurrencies() {
        forexAPIService.getCurrencies().enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> currenciesMap = response.body();
                    List<CurrencyEntity> currencyList = new ArrayList<>();
                    for(Map.Entry<String, String> entry : currenciesMap.entrySet()) {
                        currencyList.add(new CurrencyEntity(entry.getKey(), entry.getValue()));
                    }
                    callback.onCurrenciesSuccessFromRemote(currencyList, System.currentTimeMillis());
                } else {
                    callback.onCurrenciesFailureFromRemote(new Exception("Remote: Network error or empty response for currencies"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                callback.onCurrenciesFailureFromRemote(new Exception(t));
            }
        });
    }
    @Override
    public void saveCurrencies(List<CurrencyEntity> currencies) {
        // do nothing
    }
}