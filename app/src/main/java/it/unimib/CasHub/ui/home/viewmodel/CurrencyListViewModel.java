package it.unimib.CasHub.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.unimib.CasHub.model.CurrencyEntity;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.repository.ForexRepository;

public class CurrencyListViewModel extends ViewModel {

    private final ForexRepository forexRepository;
    private LiveData<Result<List<CurrencyEntity>>> currencies;

    public CurrencyListViewModel(ForexRepository forexRepository) {
        this.forexRepository = forexRepository;
    }

    public LiveData<Result<List<CurrencyEntity>>> getCurrencies() {
        if (currencies == null) {
            currencies = forexRepository.fetchCurrencies();
        }
        return currencies;
    }
}
