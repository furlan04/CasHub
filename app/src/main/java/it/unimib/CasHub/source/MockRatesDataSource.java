package it.unimib.CasHub.source;

import java.io.IOException;

import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.utils.Constants;
import it.unimib.CasHub.utils.JSONParserUtils;

public class MockRatesDataSource extends BaseRatesDataSource {

    private final JSONParserUtils jsonParserUtil;

    public MockRatesDataSource(JSONParserUtils jsonParserUtil) {
        this.jsonParserUtil = jsonParserUtil;
    }

    @Override
    public void getRates(String base) {
        try {
            ForexAPIResponse response = jsonParserUtil.parseJSONFileWithGSonForexRates(Constants.SAMPLE_RATES_JSON);
            response.setBase(base); // Ensure the base is updated as per the request
            callback.onRatesSuccessFromRemote(response, System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
            callback.onRatesFailureFromRemote(new Exception("Error reading mock rates data: " + e.getMessage()));
        }
    }
}
