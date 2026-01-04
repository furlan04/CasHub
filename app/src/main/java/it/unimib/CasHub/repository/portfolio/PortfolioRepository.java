package it.unimib.CasHub.repository.portfolio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.database.DataSnapshot;
import it.unimib.CasHub.model.PortfolioStock;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.source.portfolio.BasePortfolioDataSource;
import it.unimib.CasHub.source.portfolio.PortfolioResponseCallback;

public class PortfolioRepository implements IPortfolioRepository {

    private final BasePortfolioDataSource portfolioFirebaseDataSource;

    public PortfolioRepository(BasePortfolioDataSource portfolioFirebaseDataSource) {
        this.portfolioFirebaseDataSource = portfolioFirebaseDataSource;
    }

    @Override
    public LiveData<Result> getPortfolio() {
        MutableLiveData<Result> portfolioData = new MutableLiveData<>();
        portfolioFirebaseDataSource.getPortfolio(new PortfolioResponseCallback<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                portfolioData.postValue(new Result.Success<>(snapshot));
            }

            @Override
            public void onError(String errorMessage) {
                portfolioData.postValue(new Result.Error(errorMessage));
            }
        });
        return portfolioData;
    }

    @Override
    public void savePortfolioSnapshot(double totalValue) {
        portfolioFirebaseDataSource.savePortfolioSnapshot(totalValue);
    }

    @Override
    public LiveData<Result> removeStockFromPortfolio(PortfolioStock stock, double quantityToRemove) {
        MutableLiveData<Result> result = new MutableLiveData<>();
        portfolioFirebaseDataSource.removeStockFromPortfolio(stock, quantityToRemove, new PortfolioResponseCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                result.postValue(new Result.Success<>("Stock removed"));
            }

            @Override
            public void onError(String errorMessage) {
                result.postValue(new Result.Error(errorMessage));
            }
        });
        return result;
    }

    @Override
    public LiveData<Result> getPortfolioHistory() {
        MutableLiveData<Result> historyData = new MutableLiveData<>();
        portfolioFirebaseDataSource.getPortfolioHistory(new PortfolioResponseCallback<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                historyData.postValue(new Result.Success<>(snapshot));
            }

            @Override
            public void onError(String errorMessage) {
                historyData.postValue(new Result.Error(errorMessage));
            }
        });
        return historyData;
    }

    @Override
    public LiveData<Result> addStockToPortfolio(PortfolioStock stock) {
        MutableLiveData<Result> result = new MutableLiveData<>();
        portfolioFirebaseDataSource.addStockToPortfolio(stock, new PortfolioResponseCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                result.postValue(new Result.Success<>("Stock added"));
            }

            @Override
            public void onError(String errorMessage) {
                result.postValue(new Result.Error(errorMessage));
            }
        });
        return result;
    }

    @Override
    public void updateStockInPortfolio(PortfolioStock stock) {
        portfolioFirebaseDataSource.updateStockInPortfolio(stock);
    }
}
