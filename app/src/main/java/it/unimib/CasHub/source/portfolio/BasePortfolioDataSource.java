package it.unimib.CasHub.source.portfolio;

import com.google.firebase.database.DataSnapshot;
import it.unimib.CasHub.model.PortfolioStock;

public abstract class BasePortfolioDataSource {
    public abstract void getPortfolio(PortfolioResponseCallback<DataSnapshot> callback);
    public abstract void savePortfolioSnapshot(double totalValue);
    public abstract void removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove, PortfolioResponseCallback<Void> callback);
    public abstract void getPortfolioHistory(PortfolioResponseCallback<DataSnapshot> callback);
    public abstract void addStockToPortfolio(PortfolioStock stock, PortfolioResponseCallback<Void> callback);
    public abstract void updateStockInPortfolio(PortfolioStock stock);
}
