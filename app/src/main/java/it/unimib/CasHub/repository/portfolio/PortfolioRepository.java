package it.unimib.CasHub.repository.portfolio;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.source.portfolio.BasePortfolioDataSource;
import it.unimib.CasHub.source.portfolio.PortfolioResponseCallback;

public class PortfolioRepository implements IPortfolioRepository, PortfolioResponseCallback {

    private final MutableLiveData<Result<List<PortfolioStock>>> portfolioLiveData;
    private final MutableLiveData<Result<List<DataSnapshot>>> historyLiveData;
    private final BasePortfolioDataSource remoteDataSource;
    private final String userId;

    public PortfolioRepository(BasePortfolioDataSource remoteDataSource) {
        this.portfolioLiveData = new MutableLiveData<>();
        this.historyLiveData = new MutableLiveData<>();
        this.remoteDataSource = remoteDataSource;
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.remoteDataSource.setCallback(this);
    }

    @Override
    public MutableLiveData<Result<List<PortfolioStock>>> getPortfolio() {
        remoteDataSource.getPortfolio();
        return portfolioLiveData;
    }

    @Override
    public void savePortfolioSnapshot(double totalValue) {
        remoteDataSource.savePortfolioSnapshot(totalValue);
    }

    @Override
    public void removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove) {
        remoteDataSource.removeStockFromPortfolio(stock, quantityToRemove);
    }

    @Override
    public MutableLiveData<Result<List<DataSnapshot>>> getPortfolioHistory() {
        remoteDataSource.getPortfolioHistory();
        return historyLiveData;
    }

    @Override
    public void addStockToPortfolio(PortfolioStock stock) {
        remoteDataSource.addStockToPortfolio(stock);
    }

    @Override
    public void updateStockInPortfolio(PortfolioStock stock) {
        remoteDataSource.updateStockInPortfolio(stock);
    }

    @Override
    public void onPortfolioSuccess(List<PortfolioStock> portfolio) {
        List<PortfolioStock> userPortfolio = new ArrayList<>();
        if (portfolio != null) {
            for (PortfolioStock p : portfolio) {
                if (p != null && userId.equals(userId)) {
                    userPortfolio.add(p);
                }
            }
        }
        portfolioLiveData.postValue(new Result.Success<>(userPortfolio));
    }

    @Override
    public void onPortfolioFailure(Exception exception) {
        portfolioLiveData.postValue(new Result.Error<>(exception.getMessage()));
    }

    @Override
    public void onStockAdded() {
        remoteDataSource.getPortfolio();
    }

    @Override
    public void onStockRemoved() {
        remoteDataSource.getPortfolio();
    }

    @Override
    public void onStockUpdated() {
        remoteDataSource.getPortfolio();
    }

    @Override
    public void onHistorySuccess(List<DataSnapshot> history) {
        historyLiveData.postValue(new Result.Success<>(history));
    }
}
