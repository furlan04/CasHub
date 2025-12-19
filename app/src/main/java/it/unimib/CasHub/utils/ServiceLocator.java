package it.unimib.CasHub.utils;

import android.app.Application;

import it.unimib.CasHub.database.CurrencyDao;
import it.unimib.CasHub.database.CurrencyRoomDatabase;
import it.unimib.CasHub.repository.ForexRepository;
import it.unimib.CasHub.service.AgencyAPIService;
import it.unimib.CasHub.service.ForexAPIService;
import it.unimib.CasHub.source.BaseForexDataSource;
import it.unimib.CasHub.source.ForexAPIDataSource;
import it.unimib.CasHub.source.ForexMockDataSource;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceLocator {

    private static volatile ServiceLocator INSTANCE = null;

    private ServiceLocator() {}

    public static ServiceLocator getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceLocator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceLocator();
                }
            }
        }
        return INSTANCE;
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request request = chain.request().newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .build();
                return chain.proceed(request);
            })
            .build();

    public ForexAPIService getForexAPIService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.CURRENCY_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ForexAPIService.class);
    }
    public AgencyAPIService getAgencyAPIService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.AGENCY_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(AgencyAPIService.class);
    }

    private CurrencyRoomDatabase getCurrencyDB(Application application) {
        return CurrencyRoomDatabase.getDatabase(application);
    }

    public ForexRepository getForexRepository(Application application, boolean debugMode) {
        ForexAPIService apiService = getForexAPIService();
        JSONParserUtils jsonParserUtils = new JSONParserUtils(application);
        CurrencyDao currencyDao = getCurrencyDB(application).currencyDao();

        BaseForexDataSource dataSource;

        if (debugMode) {
            dataSource = new ForexMockDataSource(jsonParserUtils);
        } else {
            dataSource = new ForexAPIDataSource(apiService);
        }

        return new ForexRepository(dataSource, currencyDao);
    }
}
