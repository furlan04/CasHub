package it.unimib.CasHub.repository;

import android.app.Application;
import android.util.Log;

import java.util.List;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.Agency;
import it.unimib.CasHub.service.AgencyAPIService;
import it.unimib.CasHub.utils.ServiceLocator;
import it.unimib.CasHub.utils.AgencyResponseCallBack;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgencyAPIRepository implements IAgencyRepository {
    private final Application application;
    private final AgencyResponseCallBack responseCallback;
    private final AgencyAPIService apiService;
    private final String apiKey;
    public AgencyAPIRepository(Application application, AgencyResponseCallBack callback) {
        this.application = application;
        this.responseCallback = callback;

        // Prendi l'API key dal file resources (gradle resValue)
        this.apiKey = application.getString(R.string.stocks_api_key);

        // Recuperi il servizio Retrofit tramite il ServiceLocator
        this.apiService = ServiceLocator.getInstance().getAgencyAPIService();
    }
    @Override
    public void getAllAgencies(String query){
        // Creo la chiamata API con il parametro query (e l'API key)
        Call<List<Agency>> call = apiService.getAgencies(query, apiKey);

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
                        responseCallback.onSuccess(agencies, System.currentTimeMillis());
                    } else {
                        responseCallback.onFailure("Nessun risultato");
                    }
                } else {
                    responseCallback.onFailure("Errore HTTP: " + response.code() + " - " + response.message());
                }

            }

            @Override
            public void onFailure(Call<List<Agency>> call, Throwable t) {
                responseCallback.onFailure("Errore rete: " + t.getMessage());
            }
        });
    }
}
