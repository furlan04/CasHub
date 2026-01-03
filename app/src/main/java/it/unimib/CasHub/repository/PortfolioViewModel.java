package it.unimib.CasHub.repository;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import java.util.List;

import it.unimib.CasHub.model.PortfolioStock;

public class PortfolioViewModel extends ViewModel {
    private static final String TAG = "PortfolioViewModel";
    private PortfolioFirebaseRepository repository;
    private PortfolioCallback callback;

    // 👇 INTERFACCIA callback
    public interface PortfolioCallback {
        void onPortfolioUpdateSuccess(String message);
        void onPortfolioUpdateFailure(String errorMessage);
    }

    public PortfolioViewModel(PortfolioCallback callback) {
        this.callback = callback;
        this.repository = new PortfolioFirebaseRepository();
    }

    public void addStock(String symbol, String name, String currency,
                         String exchange, String exchangeFullName,
                         double quantity, double currentPrice) {

        PortfolioStock stock = new PortfolioStock();
        stock.setSymbol(symbol);
        stock.setName(name);
        stock.setCurrency(currency);
        stock.setExchange(exchange);
        stock.setExchangeFullName(exchangeFullName);
        stock.setQuantity(quantity);
        stock.setAveragePrice(currentPrice);

        repository.addStockToPortfolio(stock, new PortfolioFirebaseRepository.PortfolioCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, message);
                if (callback != null) {
                    callback.onPortfolioUpdateSuccess(message);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, errorMessage);
                if (callback != null) {
                    callback.onPortfolioUpdateFailure(errorMessage);
                }
            }
        });
    }
}
