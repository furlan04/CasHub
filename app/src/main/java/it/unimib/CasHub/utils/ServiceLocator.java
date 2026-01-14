package it.unimib.CasHub.utils;

import android.app.Application;

import it.unimib.CasHub.R;
import it.unimib.CasHub.database.CurrencyDao;
import it.unimib.CasHub.database.CurrencyRoomDatabase;
import it.unimib.CasHub.database.TransactionDao;
import it.unimib.CasHub.database.TransactionRoomDatabase;
import it.unimib.CasHub.repository.ForexRepository;
import it.unimib.CasHub.repository.agency.AgencyAPIRepository;
import it.unimib.CasHub.repository.forex.ForexRepository;
import it.unimib.CasHub.repository.portfolio.PortfolioRepository;
import it.unimib.CasHub.repository.stock.StockRepository;
import it.unimib.CasHub.repository.transaction.TransactionRepository;
import it.unimib.CasHub.repository.user.IUserRepository;
import it.unimib.CasHub.repository.user.UserRepository;
import it.unimib.CasHub.service.AgencyAPIService;
import it.unimib.CasHub.service.ForexAPIService;
import it.unimib.CasHub.service.StockAPIService;
import it.unimib.CasHub.source.BaseForexDataSource;
import it.unimib.CasHub.source.ForexAPIDataSource;
import it.unimib.CasHub.source.ForexMockDataSource;
import it.unimib.CasHub.source.agency.AgencyDataSource;
import it.unimib.CasHub.source.agency.BaseAgencyDataSource;
import it.unimib.CasHub.source.forex.BaseForexDataSource;
import it.unimib.CasHub.source.forex.ForexAPIDataSource;
import it.unimib.CasHub.source.forex.ForexLocalDataSource;
import it.unimib.CasHub.source.forex.ForexMockDataSource;
import it.unimib.CasHub.source.portfolio.PortfolioFirebaseDataSource;
import it.unimib.CasHub.source.stock.BaseStockDataSource;
import it.unimib.CasHub.source.stock.StockDataSource;
import it.unimib.CasHub.source.transaction.BaseFirebaseTransactionDataSource;
import it.unimib.CasHub.source.transaction.BaseLocalTransactionDataSource;
import it.unimib.CasHub.source.transaction.TransactionFirebaseDataSource;
import it.unimib.CasHub.source.transaction.LocalTransactionLocalDataSource;
import it.unimib.CasHub.source.transaction.LocalTransactionMockDataSource;
import it.unimib.CasHub.source.user.BaseUserAuthenticationRemoteDataSource;
import it.unimib.CasHub.source.user.BaseUserDataRemoteDataSource;
import it.unimib.CasHub.source.user.UserAuthenticationFirebaseDataSource;
import it.unimib.CasHub.source.user.UserFirebaseDataSource;
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
    public StockAPIService getStockAPIService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.STOCK_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(StockAPIService.class);
    }

    private CurrencyRoomDatabase getCurrencyDB(Application application) {
        return CurrencyRoomDatabase.getDatabase(application);
    }

    private TransactionRoomDatabase getTransactionDB(Application application) {
        return TransactionRoomDatabase.getDatabase(application);
    }

    public ForexRepository getForexRepository(Application application, boolean debugMode) {
        ForexAPIService apiService = getForexAPIService();
        JSONParserUtils jsonParserUtils = new JSONParserUtils(application);

        BaseForexDataSource remoteDataSource = new ForexAPIDataSource(apiService);
        BaseForexDataSource localDataSource;
        CurrencyDao currencyDao = getCurrencyDB(application).currencyDao();


        if (debugMode) {
            localDataSource = new ForexMockDataSource(jsonParserUtils);
        } else {
            localDataSource = new ForexLocalDataSource(currencyDao);
        }

        return new ForexRepository(remoteDataSource, localDataSource);
    }

    public StockRepository getStockRepository(Application application) {
        StockAPIService apiService = getStockAPIService();
        String apiKey = application.getString(R.string.sma_api_key);
        BaseStockDataSource dataSource = new StockDataSource(apiService, apiKey);
        return new StockRepository(dataSource);
    }

    public AgencyAPIRepository getAgencyRepository(Application application) {
        AgencyAPIService apiService = getAgencyAPIService();
        String apiKey = application.getString(R.string.stocks_api_key);
        BaseAgencyDataSource dataSource = new AgencyDataSource(apiService, apiKey);
        return new AgencyAPIRepository(dataSource);
    }

    public TransactionRepository getTransactionRepository(Application application, boolean debugMode) {
        TransactionDao transactionDao = getTransactionDB(application).transactionDao();
        JSONParserUtils jsonParserUtils = new JSONParserUtils(application);
        BaseLocalTransactionDataSource localDataSource;
        BaseFirebaseTransactionDataSource remoteDataSource = new TransactionFirebaseDataSource();

        if (debugMode) {
            localDataSource = new LocalTransactionMockDataSource(jsonParserUtils);
        } else {
            localDataSource = new LocalTransactionLocalDataSource(transactionDao);
        }

        return new TransactionRepository(localDataSource, remoteDataSource);
    }

    public PortfolioRepository getPortfolioRepository() {
        PortfolioFirebaseDataSource remoteDataSource = new PortfolioFirebaseDataSource();
        return new PortfolioRepository(remoteDataSource);
    }

    public IUserRepository getUserRepository(Application application) {
        SharedPreferencesUtils sharedPreferencesUtil = new SharedPreferencesUtils(application);

        BaseUserAuthenticationRemoteDataSource userRemoteAuthenticationDataSource =
                new UserAuthenticationFirebaseDataSource();

        BaseUserDataRemoteDataSource userDataRemoteDataSource =
                new UserFirebaseDataSource(sharedPreferencesUtil);


        return new UserRepository(userRemoteAuthenticationDataSource,
                userDataRemoteDataSource);
    }
}
