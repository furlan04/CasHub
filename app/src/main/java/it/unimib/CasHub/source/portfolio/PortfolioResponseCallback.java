package it.unimib.CasHub.source.portfolio;

import com.google.firebase.database.DataSnapshot;

import java.util.List;
import it.unimib.CasHub.model.PortfolioStock;

public interface PortfolioResponseCallback {
    void onPortfolioSuccess(List<PortfolioStock> portfolio);
    void onPortfolioFailure(Exception exception);
    void onStockAdded();
    void onStockRemoved();
    void onStockUpdated();
    void onHistorySuccess(List<DataSnapshot> history);
}
