package it.unimib.CasHub.ui.home.viewmodel;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.database.DataSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.StockQuote;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.model.TransactionType;
import it.unimib.CasHub.repository.StockAPIRepository;
import it.unimib.CasHub.repository.portfolio.IPortfolioRepository;
import it.unimib.CasHub.repository.transaction.ITransactionRepository;
import it.unimib.CasHub.utils.StockResponseCallback;

public class HomepageStocksViewModel extends ViewModel {

    private final IPortfolioRepository portfolioRepository;
    private final StockAPIRepository stockAPIRepository;
    private final Application application;

    private final ITransactionRepository transactionRepository;
    private final MutableLiveData<String> snackbarMessage = new MutableLiveData<>();
    private static final String TAG = "HomepageStocksViewModel";

    public HomepageStocksViewModel(Application application, IPortfolioRepository portfolioRepository, StockAPIRepository stockAPIRepository, ITransactionRepository transactionRepository) {
        this.application = application;
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
        this.stockAPIRepository = stockAPIRepository;
    }

    public LiveData<Result<List<PortfolioStock>>> getPortfolio() {
        return portfolioRepository.getPortfolio();
    }

    public LiveData<Result<List<DataSnapshot>>> getPortfolioHistory() {
        return portfolioRepository.getPortfolioHistory();
    }

    public LiveData<String> getSnackbarMessage() {
        return snackbarMessage;
    }

    public void refreshPortfolioStocks(List<PortfolioStock> portfolio) {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        List<PortfolioStock> stocksToUpdate = new ArrayList<>();
        List<PortfolioStock> alreadyUpdatedStocks = new ArrayList<>();

        for (PortfolioStock stock : portfolio) {
            if (stock != null) {
                if (!todayDate.equals(stock.getLastUpdate())) {
                    stocksToUpdate.add(stock);
                } else {
                    alreadyUpdatedStocks.add(stock);
                }
            }
        }

        if (stocksToUpdate.isEmpty()) {
            double totalValue = 0;
            for (PortfolioStock stock : portfolio) {
                if (stock != null) {
                    totalValue += stock.getQuantity() * stock.getAveragePrice();
                }
            }
            if (!portfolio.isEmpty()) {
                savePortfolioSnapshot(totalValue);
            }
            return;
        }

        final AtomicInteger updatesCounter = new AtomicInteger(stocksToUpdate.size());
        final List<PortfolioStock> allStocks = Collections.synchronizedList(new ArrayList<>(alreadyUpdatedStocks));

        for (PortfolioStock stock : stocksToUpdate) {
            stockAPIRepository.getStockQuote(stock.getSymbol(), new StockResponseCallback() {
                @Override
                public void onSuccess(StockQuote stockQuote) {
                    try {
                        double currentPrice = Double.parseDouble(stockQuote.getPrice());
                        stock.setAveragePrice(currentPrice);
                        stock.setLastUpdate(todayDate);
                        portfolioRepository.updateStockInPortfolio(stock);
                    } catch (NumberFormatException e) {
                        android.util.Log.e(TAG, "Error parsing price for " + stock.getSymbol(), e);
                    } finally {
                        allStocks.add(stock);
                        if (updatesCounter.decrementAndGet() == 0) {
                            updatePortfolioHistory(allStocks);
                        }
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    android.util.Log.e(TAG, "Error fetching stock quote for " + stock.getSymbol() + ": " + errorMessage);
                    allStocks.add(stock); // Add the stock with its old price
                    if (updatesCounter.decrementAndGet() == 0) {
                        updatePortfolioHistory(allStocks);
                    }
                }
            });
        }
        snackbarMessage.postValue("Prezzi del portfolio aggiornati");
    }

    private void updatePortfolioHistory(List<PortfolioStock> allStocks) {
        double totalValue = 0;
        for (PortfolioStock stock : allStocks) {
            totalValue += stock.getQuantity() * stock.getAveragePrice();
        }
        savePortfolioSnapshot(totalValue);
    }

    public void savePortfolioSnapshot(double totalValue) {
        portfolioRepository.savePortfolioSnapshot(totalValue);
    }

    public void removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setCurrency(stock.getCurrency());
        transaction.setAmount(quantityToRemove * stock.getAveragePrice());
        transaction.setType(TransactionType.AZIONI.name());
        transaction.setName("Vendita di " + stock.getName());
        transactionRepository.insertTransaction(transaction);
        portfolioRepository.removeStockFromPortfolio(stock, quantityToRemove);
    }
}
