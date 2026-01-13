package it.unimib.CasHub.source.stock;

import it.unimib.CasHub.repository.stock.StockResponseCallback;

public abstract class BaseStockDataSource {

    protected StockResponseCallback callback;

    public void setCallback(StockResponseCallback callback) {
        this.callback = callback;
    }

    public abstract void getStockQuote(String symbol);
    public abstract void getWeeklyChart(String symbol);

}
