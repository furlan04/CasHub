package it.unimib.CasHub.service;

import static it.unimib.CasHub.utils.Constants.CURRENCY_EXCHANGE_ENDPOINT;
import static it.unimib.CasHub.utils.Constants.CURRENCY_EXCHANGE_BASE;
import static it.unimib.CasHub.utils.Constants.CURRENCY_LIST_ENDPOINT;

import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.model.Currency;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ForexAPIService {
    @GET(CURRENCY_EXCHANGE_ENDPOINT)
    Call<ForexAPIResponse> getRates(
            @Query(CURRENCY_EXCHANGE_BASE) String base);

    @GET(CURRENCY_LIST_ENDPOINT)
    Call<Map<String, String>> getCurrencies();
}
