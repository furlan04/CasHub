package it.unimib.CasHub.source.portfolio;

import com.google.firebase.database.DataSnapshot;
import it.unimib.CasHub.model.PortfolioStock;

public abstract class BasePortfolioDataSource {
    PortfolioResponseCallback callback;
    public void setCallback(PortfolioResponseCallback callback){
        this.callback = callback;
    }
    public abstract void getPortfolio();
    public abstract void savePortfolioSnapshot(double totalValue);
    public abstract void removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove);
    public abstract void getPortfolioHistory();
    public abstract void addStockToPortfolio(PortfolioStock stock);
    public abstract void updateStockInPortfolio(PortfolioStock stock);
}
