package it.unimib.CasHub.ui.home.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.CasHub.repository.agency.IAgencyRepository;
import it.unimib.CasHub.utils.ServiceLocator;

public class AgencyViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public AgencyViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AgencyViewModel.class)) {
            Application application = this.application;
            IAgencyRepository agencyRepository = ServiceLocator.getInstance().getAgencyRepository(application);
            return (T) new AgencyViewModel(agencyRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
