package it.unimib.CasHub.source;

import androidx.annotation.NonNull;

import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.service.ForexAPIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RatesRemoteDataSource extends BaseRatesDataSource {

    private final ForexAPIService forexAPIService;

    public RatesRemoteDataSource(ForexAPIService forexAPIService) {
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
                    callback.onRatesFailureFromRemote(new Exception("Remote: Network error or empty response"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForexAPIResponse> call, @NonNull Throwable t) {
                callback.onRatesFailureFromRemote(new Exception(t));
            }
        });
    }
}
