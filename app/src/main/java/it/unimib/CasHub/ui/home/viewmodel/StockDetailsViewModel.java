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
import it.unimib.CasHub.repository.portfolio.IPortfolioRepository;
import it.unimib.CasHub.repository.stock.IStockRepository;
import it.unimib.CasHub.repository.transaction.ITransactionRepository;

public class StockDetailsViewModel extends ViewModel {

    private final IStockRepository stockRepository;
    private final IPortfolioRepository portfolioRepository;
    private final ITransactionRepository transactionRepository;
    private MutableLiveData<Result<StockQuote>> stockQuoteLiveData;
    private MutableLiveData<Result<ChartData>> chartDataLiveData;


    public StockDetailsViewModel(IStockRepository stockRepository, IPortfolioRepository portfolioRepository, ITransactionRepository transactionRepository) {
        this.stockRepository = stockRepository;
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
    }

    public LiveData<Result<StockQuote>> getStockQuote(String symbol) {
        if (stockQuoteLiveData == null){
            fetchStockQuote(symbol);
        }
        return stockQuoteLiveData;
    }

    void fetchStockQuote(String symbol) {
        stockQuoteLiveData = stockRepository.getStockQuote(symbol);
    }

    public LiveData<Result<ChartData>> getChartData(String symbol) {
        if (chartDataLiveData == null){
            fetchChartData(symbol);
        }
        return chartDataLiveData;
    }

    void fetchChartData(String symbol) {
        chartDataLiveData = stockRepository.getWeeklyChart(symbol);
    }

    public void addStockToPortfolio(PortfolioStock stock) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setCurrency(stock.getCurrency());
        transaction.setAmount(stock.getQuantity() * stock.getAveragePrice() * -1);
        transaction.setType(TransactionType.AZIONI.name());
        transaction.setName("Acquisto di " + stock.getSymbol());
        transactionRepository.insertTransaction(transaction);
        portfolioRepository.addStockToPortfolio(stock);
    }
}
