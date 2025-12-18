package it.unimib.CasHub.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimib.CasHub.model.Currency;
import it.unimib.CasHub.utils.Constants;
import it.unimib.CasHub.utils.JSONParserUtils;

public class MockCurrenciesDataSource extends BaseCurrenciesDataSource {

    private final JSONParserUtils jsonParserUtil;

    public MockCurrenciesDataSource(JSONParserUtils jsonParserUtil) {
        this.jsonParserUtil = jsonParserUtil;
    }

    @Override
    public void getCurrencies() {
        try {
            Map<String, String> currenciesMap = jsonParserUtil.parseJSONFileWithGSonCurrencies(Constants.SAMPLE_CURRENCIES_JSON);
            List<Currency> currencyList = new ArrayList<>();
            if (currenciesMap != null) {
                for (Map.Entry<String, String> entry : currenciesMap.entrySet()) {
                    currencyList.add(new Currency(entry.getKey(), entry.getValue()));
                }
            }
            callback.onCurrenciesSuccessFromRemote(currencyList, System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
            callback.onCurrenciesFailureFromRemote(new Exception("Error reading mock currencies data: " + e.getMessage()));
        }
    }
}
