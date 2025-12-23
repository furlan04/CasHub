package it.unimib.CasHub.ui.home.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.CasHub.repository.TransactionRepository;
import it.unimib.CasHub.utils.ServiceLocator;

public class HomepageTransactionViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final boolean debugMode;

    public HomepageTransactionViewModelFactory(Application application, boolean debugMode) {
        this.application = application;
        this.debugMode = debugMode;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomepageTransactionViewModel.class)) {
            TransactionRepository transactionRepository = ServiceLocator.getInstance().getTransactionRepository(application, debugMode);
            return (T) new HomepageTransactionViewModel(transactionRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
