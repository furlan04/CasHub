package it.unimib.CasHub.repository;

import it.unimib.CasHub.utils.StockResponseCallback;

public interface IStockRepository {
    void getStockQuote(String symbol, StockResponseCallback callback);
}
