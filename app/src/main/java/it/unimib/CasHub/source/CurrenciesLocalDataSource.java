package it.unimib.CasHub.source;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.database.CurrencyDao;
import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.model.CurrencyEntity;
import it.unimib.CasHub.utils.SharedPreferencesUtils;

public class CurrenciesLocalDataSource extends BaseCurrenciesDataSource {

    private final CurrencyDao currencyDao;

    public CurrenciesLocalDataSource(CurrencyDao currencyDao, SharedPreferencesUtils sharedPreferencesUtils) {
        this.currencyDao = currencyDao;
    }

    @Override
    public void getCurrencies() {
        new Thread(() -> {
            List<CurrencyEntity> cached = currencyDao.getAllCurrencies();
            if (cached != null && !cached.isEmpty()) {
                List<Currency> list = new ArrayList<>();
                for (CurrencyEntity c : cached) {
                    list.add(new Currency(c.code, c.name));
                }
                callback.onCurrenciesSuccessFromLocal(list);
            } else {
                callback.onCurrenciesFailureFromLocal(new Exception("Local: No currencies found"));
            }
        }).start();
    }

    public void saveCurrencies(List<Currency> currencyList) {
        new Thread(() -> {
            List<CurrencyEntity> currencyEntities = new ArrayList<>();
            for(Currency c : currencyList) {
                currencyEntities.add(new CurrencyEntity(c.getCode(), c.getName()));
            }
            currencyDao.insertAllCurrencies(currencyEntities);
            // We call onCurrenciesSuccessFromLocal to notify the repository that the data is ready
            callback.onCurrenciesSuccessFromLocal(currencyList);
        }).start();
    }
}
