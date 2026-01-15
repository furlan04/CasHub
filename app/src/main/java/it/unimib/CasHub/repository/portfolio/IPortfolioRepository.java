package it.unimib.CasHub.repository.portfolio;

import androidx.lifecycle.MutableLiveData;
import it.unimib.CasHub.model.ChartData;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;

import java.util.List;

public interface IPortfolioRepository {
    MutableLiveData<Result<List<PortfolioStock>>> getPortfolio();
    void savePortfolioSnapshot(double totalValue);
    void removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove);
    MutableLiveData<Result<ChartData>> getPortfolioHistory();
    void addStockToPortfolio(PortfolioStock stock);
    void updateStockInPortfolio(PortfolioStock stock);
}
