package it.unimib.CasHub.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimib.CasHub.R;
import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.DailyTimeSeries;
import it.unimib.CasHub.model.DailyTimeSeriesResponse;
import it.unimib.CasHub.model.StockQuoteResponse;
import it.unimib.CasHub.service.StockAPIService;
import it.unimib.CasHub.utils.StockResponseCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StockAPIRepository implements IStockRepository {

    private static final String TAG = StockAPIRepository.class.getSimpleName();
    private static final String BASE_URL = "https://www.alphavantage.co/";
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minuti

    private static StockAPIRepository instance;
    private final StockAPIService stockAPIService;
    private final String apiKey;

    // Cache per i dati del grafico
    private final Map<String, CachedChartData> chartCache = new HashMap<>();

    private StockAPIRepository(Application application) {
        this.apiKey = application.getString(R.string.sma_api_key);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.stockAPIService = retrofit.create(StockAPIService.class);
    }

    public static synchronized StockAPIRepository getInstance(Application application) {
        if (instance == null) {
            instance = new StockAPIRepository(application);
        }
        return instance;
    }

    @Override
    public void getStockQuote(String symbol, StockResponseCallback callback) {
        Call<StockQuoteResponse> call = stockAPIService.getStockQuote(
                "GLOBAL_QUOTE",
                symbol,
                apiKey
        );

        call.enqueue(new Callback<StockQuoteResponse>() {
            @Override
            public void onResponse(@NonNull Call<StockQuoteResponse> call,
                                   @NonNull Response<StockQuoteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StockQuoteResponse stockResponse = response.body();

                    if (stockResponse.getGlobalQuote() != null) {
                        callback.onSuccess(stockResponse.getGlobalQuote());
                    } else {
                        callback.onFailure("Dati non disponibili per questo simbolo");
                    }
                } else {
                    callback.onFailure("Errore nella risposta API: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StockQuoteResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Errore chiamata API: " + t.getMessage());
                callback.onFailure("Errore di connessione: " + t.getMessage());
            }
        });
    }

    /**
     * Carica i dati settimanali per il grafico (ultimi 7 giorni) con cache
     */
    public void getWeeklyChart(String symbol, ChartCallback chartCallback) {
        // Controlla se c'è una cache valida
        if (chartCache.containsKey(symbol)) {
            CachedChartData cached = chartCache.get(symbol);
            if (cached != null && !cached.isExpired()) {
                Log.d(TAG, "Usando dati cache per " + symbol);
                chartCallback.onSuccess(cached.data);
                return;
            }
        }

        Log.d(TAG, "Chiamata API per grafico: " + symbol);

        Call<DailyTimeSeriesResponse> call = stockAPIService.getDailyTimeSeries(
                "TIME_SERIES_DAILY",
                symbol,
                apiKey
        );

        call.enqueue(new Callback<DailyTimeSeriesResponse>() {
            @Override
            public void onResponse(@NonNull Call<DailyTimeSeriesResponse> call,
                                   @NonNull Response<DailyTimeSeriesResponse> response) {

                Log.d(TAG, "getWeeklyChart response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    DailyTimeSeriesResponse data = response.body();

                    Log.d(TAG, "Meta Data: " + data.getMetaData());
                    Log.d(TAG, "Time Series null? " + (data.getTimeSeries() == null));

                    if (data.getTimeSeries() != null && !data.getTimeSeries().isEmpty()) {
                        Log.d(TAG, "Time Series size: " + data.getTimeSeries().size());

                        // Estrai gli ultimi 7 giorni
                        Map<String, DailyTimeSeries> timeSeries = data.getTimeSeries();
                        List<String> dates = new ArrayList<>();
                        List<Float> prices = new ArrayList<>();

                        // Converti la mappa in lista (le date sono in ordine decrescente dall'API)
                        List<Map.Entry<String, DailyTimeSeries>> entries =
                                new ArrayList<>(timeSeries.entrySet());

                        // Prendi solo gli ultimi 5 giorni e inverti l'ordine (dal più vecchio al più recente)
                        int count = Math.min(5, entries.size());
                        for (int i = count - 1; i >= 0; i--) {
                            Map.Entry<String, DailyTimeSeries> entry = entries.get(i);
                            dates.add(entry.getKey());
                            try {
                                float closePrice = Float.parseFloat(entry.getValue().getClose());
                                prices.add(closePrice);
                                Log.d(TAG, "Data: " + entry.getKey() + " Price: " + closePrice);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing price: " + e.getMessage());
                            }
                        }

                        if (!dates.isEmpty() && !prices.isEmpty()) {
                            ChartData chartData = new ChartData(dates, prices);

                            // Salva in cache
                            chartCache.put(symbol, new CachedChartData(chartData));

                            chartCallback.onSuccess(chartData);
                        } else {
                            chartCallback.onFailure("Nessun dato disponibile per il grafico");
                        }
                    } else {
                        Log.e(TAG, "Time Series è null o vuoto! (Probabilmente rate limit)");

                        // Prova a usare dati cache anche se scaduti
                        if (chartCache.containsKey(symbol)) {
                            Log.d(TAG, "Usando cache scaduta per " + symbol);
                            CachedChartData cached = chartCache.get(symbol);
                            if (cached != null) {
                                chartCallback.onSuccess(cached.data);
                                return;
                            }
                        }

                        chartCallback.onFailure("Rate limit raggiunto. Riprova tra qualche minuto.");
                    }
                } else {
                    Log.e(TAG, "Errore risposta API grafico. Code: " + response.code());
                    if (response.code() == 429) {
                        chartCallback.onFailure("Rate limit raggiunto. Riprova tra qualche secondo.");
                    } else {
                        chartCallback.onFailure("Errore nella risposta API: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DailyTimeSeriesResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Errore chiamata API grafico: " + t.getMessage());
                chartCallback.onFailure("Errore di connessione: " + t.getMessage());
            }
        });
    }

    /**
     * Classe interna per gestire la cache con timestamp
     */
    private static class CachedChartData {
        final ChartData data;
        final long timestamp;

        CachedChartData(ChartData data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > CACHE_DURATION;
        }
    }

    /**
     * Callback per i dati del grafico
     */
    public interface ChartCallback {
        void onSuccess(ChartData chartData);
        void onFailure(String errorMessage);
    }
}
