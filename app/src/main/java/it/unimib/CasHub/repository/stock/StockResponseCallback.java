package it.unimib.CasHub.repository.stock;

import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.StockQuote;

public interface StockResponseCallback {
    void onStockDetailsSuccess(StockQuote stock);
    void onStockDetailsFailure(String symbol, String message);
    void onChartCallSuccess(ChartData chartData);
    void onChartCallFailure(String message);
}
