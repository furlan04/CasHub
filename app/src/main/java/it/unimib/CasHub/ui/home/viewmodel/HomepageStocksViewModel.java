package it.unimib.CasHub.ui.home.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.StockQuote;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.model.TransactionType;
import it.unimib.CasHub.repository.portfolio.IPortfolioRepository;
import it.unimib.CasHub.repository.stock.IStockRepository;
import it.unimib.CasHub.repository.transaction.ITransactionRepository;

public class HomepageStocksViewModel extends ViewModel {

    private final IPortfolioRepository portfolioRepository;
    private final IStockRepository stockRepository;
    private final Application application;

    private final ITransactionRepository transactionRepository;
    private final MutableLiveData<String> snackbarMessage = new MutableLiveData<>();
    private MutableLiveData<Result<List<PortfolioStock>>> portfolioLiveData;
    private MutableLiveData<Result<ChartData>> portfolioHistoryLiveData;

    // <TAG>
    private static final String TAG = "HomepageStocksViewModel";

    public HomepageStocksViewModel(Application application, IPortfolioRepository portfolioRepository, IStockRepository stockRepository, ITransactionRepository transactionRepository) {
        this.application = application;
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
        this.stockRepository = stockRepository;
    }

    public LiveData<Result<List<PortfolioStock>>> getPortfolio() {
        fetchPortfolio();
        return portfolioLiveData;
    }

    void fetchPortfolio(){
        portfolioLiveData = portfolioRepository.getPortfolio();
    }

    public LiveData<Result<ChartData>> getPortfolioHistory() {
        fetchPortfolioHistory();
        return portfolioHistoryLiveData;
    }

    void fetchPortfolioHistory() {
        portfolioHistoryLiveData = portfolioRepository.getPortfolioHistory();
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

            LiveData<Result<StockQuote>> stockLiveData =
                    stockRepository.getStockQuote(stock.getSymbol());

            stockLiveData.observeForever(new Observer<Result<StockQuote>>() {
                @Override
                public void onChanged(Result<StockQuote> result) {

                    stockLiveData.removeObserver(this);

                    if (result instanceof Result.Success) {
                        StockQuote stockData =
                                ((Result.Success<StockQuote>) result).getData();

                        try {
                            double currentPrice =
                                    Double.parseDouble(stockData.getPrice());

                            stock.setAveragePrice(currentPrice);
                            stock.setLastUpdate(todayDate);

                            portfolioRepository.updateStockInPortfolio(stock);
                            allStocks.add(stock);

                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing price for " + stock.getSymbol(), e);
                        }

                    } else if (result instanceof Result.Error) {
                        Log.e(TAG, "Error fetching " + stock.getSymbol() + ": "
                                + ((Result.Error<?>) result).getMessage());
                    }

                    if (updatesCounter.decrementAndGet() == 0) {
                        updatePortfolioHistory(allStocks);
                    }
                }
            });
        }
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
