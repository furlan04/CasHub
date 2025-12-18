package it.unimib.CasHub.utils;

public class Constants {

    public static final String CURRENCY_BASE_URL = "https://api.frankfurter.app/";
    public static final String CURRENCY_EXCHANGE_ENDPOINT = "/latest";
    public static final String CURRENCY_EXCHANGE_BASE = "base";
    public static final String CURRENCY_LIST_ENDPOINT = "/currencies";


    public static final String AGENCY_BASE_URL = "https://financialmodelingprep.com/stable/";
    public static final String AGENCY_LIST_ENDPOINT = "search-name";
    public static final String AGENCY_START_QUERY = "query";
    public static final String AGENCY_STRING_APIKEY = "apikey";
    public static final String TEST_AGENCY_APP = "test_agency_app.json";

    // Sample Data
    public static final String SAMPLE_CURRENCIES_JSON = "sample_currencies.json";
    public static final String SAMPLE_RATES_JSON = "sample_rates.json";

    // Timeout for fetching fresh data
    public static final long FRESH_TIMEOUT = 60000; // 1 minute
}
