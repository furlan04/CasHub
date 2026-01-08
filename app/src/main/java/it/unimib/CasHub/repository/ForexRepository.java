package it.unimib.CasHub.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unimib.CasHub.database.CurrencyDao;
import it.unimib.CasHub.model.CurrencyEntity;
import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.source.BaseForexDataSource;
import it.unimib.CasHub.source.ForexCallback;

public class ForexRepository implements ForexCallback {

    private final MutableLiveData<Result<List<CurrencyEntity>>> currenciesMutableLiveData;
    private final MutableLiveData<Result<ForexAPIResponse>> ratesMutableLiveData;

    private final BaseForexDataSource dataSource;
    private final CurrencyDao currencyDao;

    public ForexRepository(BaseForexDataSource dataSource, CurrencyDao currencyDao) {
        this.currenciesMutableLiveData = new MutableLiveData<>();
        this.ratesMutableLiveData = new MutableLiveData<>();
        this.dataSource = dataSource;
        this.currencyDao = currencyDao;
        this.dataSource.setCallback(this);
    }

    public MutableLiveData<Result<List<CurrencyEntity>>> fetchCurrencies() {
        dataSource.getCurrencies();
        return currenciesMutableLiveData;
    }

    public MutableLiveData<Result<ForexAPIResponse>> fetchRates(String base) {
        dataSource.getRates(base);
        return ratesMutableLiveData;
    }

    @Override
    public void onRatesSuccessFromRemote(ForexAPIResponse rates, long lastUpdate) {
        ratesMutableLiveData.postValue(new Result.Success<>(rates));
    }

    @Override
    public void onRatesFailureFromRemote(Exception exception) {
        ratesMutableLiveData.postValue(new Result.Error<>(exception.getMessage()));
    }

    @Override
    public void onCurrenciesSuccessFromRemote(List<CurrencyEntity> currencies, long lastUpdate) {
        new Thread(() -> {
            currencyDao.insertAllCurrencies(currencies);
        }).start();
        currenciesMutableLiveData.postValue(new Result.Success<>(currencies));
    }

    @Override
    public void onCurrenciesFailureFromRemote(Exception exception) {
        // If remote fails, try local
        try {
            new Thread(() -> {
                List<CurrencyEntity> cached = currencyDao.getAllCurrencies();
                if (cached != null && !cached.isEmpty()) {
                    currenciesMutableLiveData.postValue(new Result.Success<>(cached));
                } else {
                    currenciesMutableLiveData.postValue(new Result.Error<>(exception.getMessage()));
                }
            }).start();
        } catch (Exception e) {
            currenciesMutableLiveData.postValue(new Result.Error<>(e.getMessage()));
        }
    }

    @Override
    public void onCurrenciesSuccessFromLocal(List<CurrencyEntity> currencies) {
        currenciesMutableLiveData.postValue(new Result.Success<>(currencies));
    }

    @Override
    public void onCurrenciesFailureFromLocal(Exception exception) {
        currenciesMutableLiveData.postValue(new Result.Error<>(exception.getMessage()));
    }
}
