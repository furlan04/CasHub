package it.unimib.CasHub.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import it.unimib.CasHub.model.StockQuote;
import it.unimib.CasHub.utils.StockResponseCallback;

public class StockMockRepository implements IStockRepository {

    private static final String TAG = StockMockRepository.class.getSimpleName();

    public StockMockRepository(Application application) {
        // Constructor vuoto per mantenere compatibilità
    }

    @Override
    public void getStockQuote(String symbol, StockResponseCallback callback) {
        // Simula un ritardo di rete
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Crea dati mock
            StockQuote mockQuote = new StockQuote();
            mockQuote.setSymbol(symbol);
            mockQuote.setPrice("150.25");
            mockQuote.setChange("-1.50");
            mockQuote.setChangePercent("-0.99%");
            mockQuote.setPreviousClose("151.75");
            mockQuote.setVolume("1234567");

            callback.onSuccess(mockQuote);
        }, 1000); // 1 secondo di ritardo
    }
}
