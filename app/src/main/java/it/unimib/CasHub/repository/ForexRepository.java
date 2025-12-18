package it.unimib.CasHub.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.source.BaseCurrenciesDataSource;
import it.unimib.CasHub.source.BaseRatesDataSource;
import it.unimib.CasHub.source.ForexCallback;
import it.unimib.CasHub.utils.Constants;

public class ForexRepository implements ForexCallback {

    private final MutableLiveData<Result<List<Currency>>> currenciesMutableLiveData;
    private final MutableLiveData<Result<ForexAPIResponse>> ratesMutableLiveData;

    private final BaseCurrenciesDataSource remoteCurrenciesDataSource;
    private final BaseCurrenciesDataSource localCurrenciesDataSource;
    private final BaseRatesDataSource remoteRatesDataSource;

    public ForexRepository(BaseCurrenciesDataSource remoteCurrenciesDataSource,
                           BaseCurrenciesDataSource localCurrenciesDataSource,
                           BaseRatesDataSource remoteRatesDataSource) {

        this.currenciesMutableLiveData = new MutableLiveData<>();
        this.ratesMutableLiveData = new MutableLiveData<>();

        this.remoteCurrenciesDataSource = remoteCurrenciesDataSource;
        this.localCurrenciesDataSource = localCurrenciesDataSource;
        this.remoteRatesDataSource = remoteRatesDataSource;

        this.remoteCurrenciesDataSource.setCallback(this);
        this.localCurrenciesDataSource.setCallback(this);
        this.remoteRatesDataSource.setCallback(this);
    }

    public MutableLiveData<Result<List<Currency>>> fetchCurrencies(long lastUpdate) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate > Constants.FRESH_TIMEOUT) {
            remoteCurrenciesDataSource.getCurrencies();
        } else {
            localCurrenciesDataSource.getCurrencies();
        }
        return currenciesMutableLiveData;
    }

    public MutableLiveData<Result<ForexAPIResponse>> fetchRates(String base) {
        remoteRatesDataSource.getRates(base);
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
    public void onCurrenciesSuccessFromRemote(List<Currency> currencies, long lastUpdate) {
        ((it.unimib.CasHub.source.CurrenciesLocalDataSource) localCurrenciesDataSource).saveCurrencies(currencies);
        // We don't update LiveData here, because saveCurrencies will trigger onCurrenciesSuccessFromLocal
    }

    @Override
    public void onCurrenciesFailureFromRemote(Exception exception) {
        // If remote fails, try to get data from local source
        localCurrenciesDataSource.getCurrencies();
    }

    @Override
    public void onCurrenciesSuccessFromLocal(List<Currency> currencies) {
        currenciesMutableLiveData.postValue(new Result.Success<>(currencies));
    }

    @Override
    public void onCurrenciesFailureFromLocal(Exception exception) {
        currenciesMutableLiveData.postValue(new Result.Error<>(exception.getMessage()));
    }
}
