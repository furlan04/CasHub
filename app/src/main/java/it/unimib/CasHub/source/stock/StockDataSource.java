package it.unimib.CasHub.source.stock;

import static it.unimib.CasHub.utils.Constants.CHART_ENDPOINT;
import static it.unimib.CasHub.utils.Constants.QUOTE_ENDPOINT;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.DailyTimeSeries;
import it.unimib.CasHub.model.DailyTimeSeriesResponse;
import it.unimib.CasHub.model.StockQuoteResponse;
import it.unimib.CasHub.service.StockAPIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import it.unimib.CasHub.utils.Constants.*;


public class StockDataSource extends BaseStockDataSource {
    private final StockAPIService stockAPIService;
    private final String apiKey;


    public StockDataSource(StockAPIService stockAPIService, String apiKey) {
        this.stockAPIService = stockAPIService;
        this.apiKey = apiKey;
    }
    @Override
    public void getStockQuote(String symbol) {
        Call<StockQuoteResponse> call = stockAPIService.getStockQuote(
                QUOTE_ENDPOINT,
                symbol,
                apiKey
        );

        call.enqueue(new Callback<StockQuoteResponse>() {
            @Override
            public void onResponse(@NonNull Call<StockQuoteResponse> call,
                                   @NonNull Response<StockQuoteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StockQuoteResponse stockResponse = response.body();

                    if (stockResponse.getGlobalQuote() != null && stockResponse.getGlobalQuote().getSymbol() != null) {
                        callback.onStockDetailsSuccess(stockResponse.getGlobalQuote());
                    } else {
                        callback.onStockDetailsFailure(symbol, "Dati non disponibili per questo simbolo");
                    }
                } else {
                    callback.onStockDetailsFailure(symbol, "Errore nella risposta API: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StockQuoteResponse> call, @NonNull Throwable t) {
                callback.onStockDetailsFailure(symbol, "Errore di connessione: " + t.getMessage());
            }
        });
    }

    @Override
    public void getWeeklyChart(String symbol) {
        Call<DailyTimeSeriesResponse> call = stockAPIService.getDailyTimeSeries(
                CHART_ENDPOINT,
                symbol,
                apiKey
        );

        call.enqueue(new Callback<DailyTimeSeriesResponse>() {
            @Override
            public void onResponse(@NonNull Call<DailyTimeSeriesResponse> call,
                                   @NonNull Response<DailyTimeSeriesResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    DailyTimeSeriesResponse data = response.body();


                    if (data.getTimeSeries() != null && !data.getTimeSeries().isEmpty()) {

                        Map<String, DailyTimeSeries> timeSeries = data.getTimeSeries();
                        List<String> dates = new ArrayList<>();
                        List<Float> prices = new ArrayList<>();

                        List<Map.Entry<String, DailyTimeSeries>> entries =
                                new ArrayList<>(timeSeries.entrySet());

                        int count = Math.min(5, entries.size());
                        for (int i = count - 1; i >= 0; i--) {
                            Map.Entry<String, DailyTimeSeries> entry = entries.get(i);
                            dates.add(entry.getKey());
                            try {
                                float closePrice = Float.parseFloat(entry.getValue().getClose());
                                prices.add(closePrice);
                            } catch (NumberFormatException e) {
                                callback.onChartCallFailure("Error parsing price");
                            }
                        }

                        if (!dates.isEmpty() && !prices.isEmpty()) {
                            ChartData chartData = new ChartData(dates, prices);
                            callback.onChartCallSuccess(chartData);
                        } else {
                            callback.onChartCallFailure("Nessun dato disponibile per il grafico");
                        }
                    } else {
                        callback.onChartCallFailure("Rate limit raggiunto. Riprova tra qualche minuto.");
                    }
                } else {
                    if (response.code() == 429) {
                        callback.onChartCallFailure("Rate limit raggiunto. Riprova tra qualche secondo.");
                    } else {
                        callback.onChartCallFailure("Errore nella risposta API: " + response.code());
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<DailyTimeSeriesResponse> call, @NonNull Throwable t) {
                callback.onChartCallFailure("Errore di connessione: " + t.getMessage());
            }
        });
    }
}
