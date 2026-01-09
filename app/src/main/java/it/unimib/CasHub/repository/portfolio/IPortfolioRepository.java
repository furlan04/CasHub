package it.unimib.CasHub.repository.portfolio;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;

public interface IPortfolioRepository {
    MutableLiveData<Result<List<PortfolioStock>>> getPortfolio();
    void savePortfolioSnapshot(double totalValue);
    void removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove);
    MutableLiveData<Result<List<DataSnapshot>>> getPortfolioHistory();
    void addStockToPortfolio(PortfolioStock stock);
    void updateStockInPortfolio(PortfolioStock stock);
}
