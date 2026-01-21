package it.unimib.CasHub.repository.stock;

import androidx.lifecycle.MutableLiveData;

import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.StockQuote;

public interface IStockRepository {
    MutableLiveData<Result<StockQuote>> getStockQuote(String symbol);
    MutableLiveData<Result<ChartData>> getWeeklyChart(String symbol);
}
