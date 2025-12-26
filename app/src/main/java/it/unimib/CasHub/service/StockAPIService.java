package it.unimib.CasHub.service;

import it.unimib.CasHub.model.DailyTimeSeriesResponse;
import it.unimib.CasHub.model.StockQuoteResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockAPIService {

    @GET("query")
    Call<StockQuoteResponse> getStockQuote(
            @Query("function") String function,
            @Query("symbol") String symbol,
            @Query("apikey") String apiKey
    );

    @GET("query")
    Call<DailyTimeSeriesResponse> getDailyTimeSeries(
            @Query("function") String function,
            @Query("symbol") String symbol,
            @Query("apikey") String apiKey
    );
}
