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

    private final BaseForexDataSource remoteDataSource;
    private final BaseForexDataSource localdatasource;

    public ForexRepository(BaseForexDataSource remotedataSource, BaseForexDataSource localdatasource) {
        this.currenciesMutableLiveData = new MutableLiveData<>();
        this.ratesMutableLiveData = new MutableLiveData<>();
        this.remoteDataSource = remotedataSource;
        this.remoteDataSource.setCallback(this);
        this.localdatasource = localdatasource;
        this.localdatasource.setCallback(this);
    }

    public MutableLiveData<Result<List<CurrencyEntity>>> fetchCurrencies() {
        remoteDataSource.getCurrencies();
        return currenciesMutableLiveData;
    }

    public MutableLiveData<Result<ForexAPIResponse>> fetchRates(String base) {
        remoteDataSource.getRates(base);
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
        localdatasource.saveCurrencies(currencies);
        currenciesMutableLiveData.postValue(new Result.Success<>(currencies));
    }

    @Override
    public void onCurrenciesFailureFromRemote(Exception exception) {
        localdatasource.getCurrencies();
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
