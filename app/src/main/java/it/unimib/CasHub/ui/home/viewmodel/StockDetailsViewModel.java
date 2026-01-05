package it.unimib.CasHub.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.StockQuote;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.model.TransactionType;
import it.unimib.CasHub.repository.StockAPIRepository;
import it.unimib.CasHub.repository.portfolio.IPortfolioRepository;
import it.unimib.CasHub.repository.transaction.ITransactionRepository;
import it.unimib.CasHub.utils.StockResponseCallback;

public class StockDetailsViewModel extends ViewModel {

    private final StockAPIRepository stockAPIRepository;
    private final IPortfolioRepository portfolioRepository;
    private final ITransactionRepository transactionRepository;
    private final MutableLiveData<Result> stockQuoteLiveData = new MutableLiveData<>();
    private final MutableLiveData<Result> chartDataLiveData = new MutableLiveData<>();

    public StockDetailsViewModel(StockAPIRepository stockAPIRepository, IPortfolioRepository portfolioRepository, ITransactionRepository transactionRepository) {
        this.stockAPIRepository = stockAPIRepository;
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
    }

    public LiveData<Result> getStockQuote(String symbol) {
        loadStockQuote(symbol);
        return stockQuoteLiveData;
    }

    public LiveData<Result> getChartData(String symbol) {
        loadChartData(symbol);
        return chartDataLiveData;
    }

    private void loadStockQuote(String symbol) {
        stockAPIRepository.getStockQuote(symbol, new StockResponseCallback() {
            @Override
            public void onSuccess(StockQuote stockQuote) {
                stockQuoteLiveData.postValue(new Result.Success<>(stockQuote));
            }

            @Override
            public void onFailure(String errorMessage) {
                stockQuoteLiveData.postValue(new Result.Error(errorMessage));
            }
        });
    }

    private void loadChartData(String symbol) {
        stockAPIRepository.getWeeklyChart(symbol, new StockAPIRepository.ChartCallback() {
            @Override
            public void onSuccess(ChartData chartData) {
                chartDataLiveData.postValue(new Result.Success<>(chartData));
            }

            @Override
            public void onFailure(String errorMessage) {
                chartDataLiveData.postValue(new Result.Error(errorMessage));
            }
        });
    }

    public LiveData<Result> addStockToPortfolio(PortfolioStock stock) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setCurrency(stock.getCurrency());
        transaction.setAmount(stock.getQuantity() * stock.getAveragePrice() * -1);
        transaction.setType(TransactionType.AZIONI.name());
        transaction.setName("Acquisto di " + stock.getSymbol());
        transactionRepository.insertTransaction(transaction);
        return portfolioRepository.addStockToPortfolio(stock);
    }
}
