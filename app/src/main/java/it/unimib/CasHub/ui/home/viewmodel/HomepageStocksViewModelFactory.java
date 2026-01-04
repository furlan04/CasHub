package it.unimib.CasHub.ui.home.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import it.unimib.CasHub.repository.StockAPIRepository;
import it.unimib.CasHub.repository.portfolio.IPortfolioRepository;
import it.unimib.CasHub.utils.ServiceLocator;

public class HomepageStocksViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;

    public HomepageStocksViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomepageStocksViewModel.class)) {
            IPortfolioRepository portfolioRepository = ServiceLocator.getInstance().getPortfolioRepository();
            StockAPIRepository stockAPIRepository = StockAPIRepository.getInstance(application);
            return (T) new HomepageStocksViewModel(application, portfolioRepository, stockAPIRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
