package it.unimib.CasHub.utils;

import it.unimib.CasHub.BuildConfig;

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

    public static final String STOCK_BASE_URL = "https://www.alphavantage.co/";
    public static final String STOCK_API_KEY = "demo";
    public static final String CHART_ENDPOINT = "TIME_SERIES_DAILY";
    public static final String QUOTE_ENDPOINT = "GLOBAL_QUOTE";

    // Sample Data
    public static final String SAMPLE_CURRENCIES_JSON = "sample_currencies.json";
    public static final String SAMPLE_RATES_JSON = "sample_rates.json";
    public static final String SAMPLE_TRANSACTIONS_JSON = "sample_transactions.json";

    // Authentication strings
    public static final String UNEXPECTED_ERROR = "unexpected_error";
    public static final String INVALID_USER_ERROR = "invalidUserError";
    public static final String INVALID_CREDENTIALS_ERROR = "invalidCredentials";
    public static final String USER_COLLISION_ERROR = "userCollisionError";
    public static final String WEAK_PASSWORD_ERROR = "passwordIsWeak";

    // Database
    public static final String REALTIME_DB_URL = BuildConfig.REALTIME_DB_URL;
    public static final String FIREBASE_USERS_COLLECTION = "users";
    public static final String FIREBASE_TRANSACTIONS_COLLECTION = "transactions";

    // Timeout for fetching fresh data
    public static final long FRESH_TIMEOUT = 60000; // 1 minute

    public static final int MINIMUM_LENGTH_PASSWORD = 8;
}
