package it.unimib.CasHub.ui.home.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.repository.ForexRepository;

public class TransactionViewModel extends ViewModel {

    private final ForexRepository forexRepository;
    private MutableLiveData<Result<List<Currency>>> currenciesLiveData;

    public TransactionViewModel(ForexRepository forexRepository) {
        this.forexRepository = forexRepository;
    }

    public MutableLiveData<Result<List<Currency>>> getCurrencies(long lastUpdate) {
        if (currenciesLiveData == null) {
            currenciesLiveData = forexRepository.fetchCurrencies(lastUpdate);
        }
        return currenciesLiveData;
    }
}
