package it.unimib.CasHub.repository.portfolio;

import androidx.lifecycle.LiveData;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;

public interface IPortfolioRepository {
    LiveData<Result> getPortfolio();
    void savePortfolioSnapshot(double totalValue);
    LiveData<Result> removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove);
    LiveData<Result> getPortfolioHistory();
    LiveData<Result> addStockToPortfolio(PortfolioStock stock);
    void updateStockInPortfolio(PortfolioStock stock);
}
