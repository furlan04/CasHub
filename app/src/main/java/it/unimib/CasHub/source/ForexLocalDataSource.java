package it.unimib.CasHub.source;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.database.CurrencyDao;
import it.unimib.CasHub.model.CurrencyEntity;

public class ForexLocalDataSource extends BaseForexDataSource {

    private final CurrencyDao currencyDao;

    public ForexLocalDataSource(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    @Override
    public void getCurrencies() {
        new Thread(() -> {
            List<CurrencyEntity> cached = currencyDao.getAllCurrencies();
            if (cached != null && !cached.isEmpty()) {
                callback.onCurrenciesSuccessFromLocal(cached);
            } else {
                callback.onCurrenciesFailureFromLocal(new Exception("Local: No currencies found"));
            }
        }).start();
    }

    @Override
    public void getRates(String base) {
        throw new UnsupportedOperationException("ForexLocalDataSource does not support rates retrieval");
    }

    public void saveCurrencies(List<CurrencyEntity> currencyList) {
        new Thread(() -> {
            currencyDao.insertAllCurrencies(currencyList);
            // We call onCurrenciesSuccessFromLocal to notify the repository that the data is ready
            callback.onCurrenciesSuccessFromLocal(currencyList);
        }).start();
    }
}
