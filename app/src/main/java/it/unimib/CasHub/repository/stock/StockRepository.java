package it.unimib.CasHub.repository.stock;

import androidx.lifecycle.MutableLiveData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.StockQuote;
import it.unimib.CasHub.source.stock.BaseStockDataSource;

public class StockRepository implements IStockRepository, StockResponseCallback {

    private final Map<String, MutableLiveData<Result<StockQuote>>> stockLiveDataMap;
    private final MutableLiveData<Result<ChartData>> chartLiveData;
    private final BaseStockDataSource stockDataSource;

    public StockRepository(BaseStockDataSource stockDataSource) {
        this.chartLiveData = new MutableLiveData<>();
        this.stockDataSource = stockDataSource;
        this.stockDataSource.setCallback(this);
        this.stockLiveDataMap = new ConcurrentHashMap<>();
    }

    @Override
    public MutableLiveData<Result<StockQuote>> getStockQuote(String symbol) {

        MutableLiveData<Result<StockQuote>> liveData =
                stockLiveDataMap.computeIfAbsent(symbol, s -> new MutableLiveData<>());

        stockDataSource.getStockQuote(symbol);

        return liveData;
    }

    @Override
    public MutableLiveData<Result<ChartData>> getWeeklyChart(String symbol) {
        stockDataSource.getWeeklyChart(symbol);
        return chartLiveData;
    }

    @Override
    public void onStockDetailsSuccess(StockQuote stock) {
        MutableLiveData<Result<StockQuote>> liveData =
                stockLiveDataMap.get(stock.getSymbol());

        if (liveData != null) {
            liveData.postValue(new Result.Success<>(stock));
        }
    }

    @Override
    public void onStockDetailsFailure(String symbol, String message) {

        MutableLiveData<Result<StockQuote>> liveData =
                stockLiveDataMap.get(symbol);

        if (liveData != null) {
            liveData.postValue(new Result.Error<>(message));
        }
    }


    @Override
    public void onChartCallSuccess(ChartData chartData) {
        chartLiveData.postValue(new Result.Success<>(chartData));
    }

    @Override
    public void onChartCallFailure(String message) {
        chartLiveData.postValue(new Result.Error<>(message));
    }
}
