package it.unimib.CasHub.ui.home.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.CasHub.repository.forex.ForexRepository;

public class RatesConversionViewModelFactory  implements ViewModelProvider.Factory {
    private final ForexRepository forexRepository;

    public RatesConversionViewModelFactory(ForexRepository forexRepository) {
        this.forexRepository = forexRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RatesConversionViewModel.class)) {
            return (T) new RatesConversionViewModel(forexRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
