package it.unimib.CasHub.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimib.CasHub.model.CurrencyEntity;
import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.utils.Constants;
import it.unimib.CasHub.utils.JSONParserUtils;

public class ForexMockDataSource extends BaseForexDataSource {

    private final JSONParserUtils jsonParserUtil;

    public ForexMockDataSource(JSONParserUtils jsonParserUtil) {
        this.jsonParserUtil = jsonParserUtil;
    }

    @Override
    public void getRates(String base) {
        try {
            ForexAPIResponse response = jsonParserUtil.parseJSONFileWithGSonForexRates(Constants.SAMPLE_RATES_JSON);
            response.setBase(base);
            callback.onRatesSuccessFromRemote(response, System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
            callback.onRatesFailureFromRemote(new Exception("Error reading mock rates data: " + e.getMessage()));
        }
    }

    @Override
    public void getCurrencies() {
        try {
            Map<String, String> currenciesMap = jsonParserUtil.parseJSONFileWithGSonCurrencies(Constants.SAMPLE_CURRENCIES_JSON);
            List<CurrencyEntity> currencyList = new ArrayList<>();
            if (currenciesMap != null) {
                for (Map.Entry<String, String> entry : currenciesMap.entrySet()) {
                    currencyList.add(new CurrencyEntity(entry.getKey(), entry.getValue()));
                }
            }
            callback.onCurrenciesSuccessFromRemote(currencyList, System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
            callback.onCurrenciesFailureFromRemote(new Exception("Error reading mock currencies data: " + e.getMessage()));
        }
    }
}
