package it.unimib.CasHub.source.agency;

import android.util.Log;

import java.util.List;

import it.unimib.CasHub.model.Agency;
import it.unimib.CasHub.service.AgencyAPIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgencyDataSource extends BaseAgencyDataSource {
    private final AgencyAPIService agencyAPIService;
    private final String apiKey;
    public AgencyDataSource(AgencyAPIService agencyAPIService, String apiKey) {
        this.agencyAPIService = agencyAPIService;
        this.apiKey = apiKey;
    }
    @Override
    public void getAllAgencies(String query){
        // Creo la chiamata API con il parametro query (e l'API key)
        Call<List<Agency>> call = agencyAPIService.getAgencies(query, apiKey);

        Log.e("API_DEBUG", "URL: " + call.request().url());

        // Chiamata async
        call.enqueue(new Callback<List<Agency>>() {
            @Override
            public void onResponse(Call<List<Agency>> call, Response<List<Agency>> response) {
                Log.e("API_DEBUG", "Response code: " + response.code());
                Log.e("API_DEBUG", "Body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    List<Agency> agencies = response.body();
                    if (agencies != null && !agencies.isEmpty()) {
                        callback.onSuccess(agencies, System.currentTimeMillis());
                    } else {
                        callback.onFailure("Nessun risultato");
                    }
                } else {
                    callback.onFailure("Errore HTTP: " + response.code() + " - " + response.message());
                }

            }

            @Override
            public void onFailure(Call<List<Agency>> call, Throwable t) {
                callback.onFailure("Errore rete: " + t.getMessage());
            }
        });
    }
}
