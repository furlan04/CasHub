package it.unimib.CasHub.ui.home.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.database.DataSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.StockQuote;
import it.unimib.CasHub.repository.StockAPIRepository;
import it.unimib.CasHub.repository.portfolio.IPortfolioRepository;
import it.unimib.CasHub.source.portfolio.PortfolioResponseCallback;
import it.unimib.CasHub.utils.StockResponseCallback;

public class HomepageStocksViewModel extends ViewModel {

    private final IPortfolioRepository portfolioRepository;
    private final StockAPIRepository stockAPIRepository;
    private final Application application;
    private final MutableLiveData<Result> portfolioLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result> portfolioHistoryLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> snackbarMessage = new MutableLiveData<>();

    private static final String LAST_UPDATE_KEY = "last_update_date";

    public HomepageStocksViewModel(Application application, IPortfolioRepository portfolioRepository, StockAPIRepository stockAPIRepository) {
        this.application = application;
        this.portfolioRepository = portfolioRepository;
        this.stockAPIRepository = stockAPIRepository;
    }

    public LiveData<Result> getPortfolio() {
        return portfolioRepository.getPortfolio();
    }

    public LiveData<Result> getPortfolioHistory() {
        return portfolioRepository.getPortfolioHistory();
    }

    public LiveData<String> getSnackbarMessage() {
        return snackbarMessage;
    }

    public void refreshPortfolioStocks(DataSnapshot portfolioSnapshot) {
        List<PortfolioStock> stocksToUpdate = new ArrayList<>();
        for (DataSnapshot child : portfolioSnapshot.getChildren()) {
            PortfolioStock stock = child.getValue(PortfolioStock.class);
            if (stock != null) {
                stocksToUpdate.add(stock);
            }
        }

        if (stocksToUpdate.isEmpty()) return;

        for (PortfolioStock stock : stocksToUpdate) {
            stockAPIRepository.getStockQuote(stock.getSymbol(), new StockResponseCallback() {
                @Override
                public void onSuccess(StockQuote stockQuote) {
                    try {
                        double currentPrice = Double.parseDouble(stockQuote.getPrice());
                        stock.setCurrentPrice(currentPrice);
                        portfolioRepository.updateStockInPortfolio(stock);
                    } catch (NumberFormatException e) {
                        // Handle parsing error
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Handle API error for a single stock
                }
            });
        }

        saveUpdateDate();
        snackbarMessage.postValue("Prezzi del portfolio aggiornati");
    }

    public boolean shouldUpdatePortfolio() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        String lastUpdateDate = prefs.getString(LAST_UPDATE_KEY, "");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        return !lastUpdateDate.equals(todayDate);
    }

    private void saveUpdateDate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        SharedPreferences.Editor editor = prefs.edit();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editor.putString(LAST_UPDATE_KEY, todayDate);
        editor.apply();
    }

    public void savePortfolioSnapshot(double totalValue) {
        portfolioRepository.savePortfolioSnapshot(totalValue);
    }

    public LiveData<Result> removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove) {
        return portfolioRepository.removeStockFromPortfolio(stock, quantityToRemove);
    }
}
