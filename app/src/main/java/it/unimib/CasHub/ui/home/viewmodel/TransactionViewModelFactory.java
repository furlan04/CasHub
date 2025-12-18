package it.unimib.CasHub.ui.home.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.CasHub.repository.ForexRepository;

public class TransactionViewModelFactory implements ViewModelProvider.Factory {

    private final ForexRepository forexRepository;

    public TransactionViewModelFactory(ForexRepository forexRepository) {
        this.forexRepository = forexRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TransactionViewModel.class)) {
            return (T) new TransactionViewModel(forexRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
