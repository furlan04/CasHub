package it.unimib.CasHub.ui.home.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.CasHub.repository.StockAPIRepository;
import it.unimib.CasHub.repository.portfolio.IPortfolioRepository;
import it.unimib.CasHub.utils.ServiceLocator;

public class StockDetailsViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;

    public StockDetailsViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StockDetailsViewModel.class)) {
            StockAPIRepository stockAPIRepository = StockAPIRepository.getInstance(application);
            IPortfolioRepository portfolioRepository = ServiceLocator.getInstance().getPortfolioRepository();
            return (T) new StockDetailsViewModel(stockAPIRepository, portfolioRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
