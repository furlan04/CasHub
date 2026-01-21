package it.unimib.CasHub.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import it.unimib.CasHub.model.ForexAPIResponse;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.model.TransactionEntity;
import it.unimib.CasHub.repository.forex.ForexRepository;

public class RatesConversionViewModel extends ViewModel {
    private final ForexRepository forexRepository;
    private LiveData<Result<List<TransactionEntity>>> transactionsLiveData;


    public RatesConversionViewModel(ForexRepository forexRepository) {
        this.forexRepository = forexRepository;
    }

    public LiveData<Result<List<TransactionEntity>>> getBasedList(
            List<TransactionEntity> transactions, String base) {
        calculateGetBasedList(transactions, base);
        return transactionsLiveData;
    }

    public void calculateGetBasedList(
            List<TransactionEntity> transactions, String base) {

        MediatorLiveData<Result<List<TransactionEntity>>> result = new MediatorLiveData<>();

        LiveData<Result<ForexAPIResponse>> ratesLiveData = forexRepository.fetchRates(base);

        result.addSource(ratesLiveData, ratesResult -> {
            if (ratesResult instanceof Result.Error) {
                result.setValue(new Result.Error<>(((Result.Error<?>) ratesResult).getMessage()));
            } else if (ratesResult instanceof Result.Success) {
                ForexAPIResponse rates = ((Result.Success<ForexAPIResponse>) ratesResult).getData();

                // Crea una copia profonda per evitare modifiche all'originale
                List<TransactionEntity> convertedTransactions = new ArrayList<>();
                for (TransactionEntity transaction : transactions) {
                    TransactionEntity copy = new TransactionEntity(transaction);
                    if (copy.getCurrency() != null && !copy.getCurrency().isEmpty()) {
                        String currencyCode = copy.getCurrency();
                        try {
                            double rate = rates.getRateFor(currencyCode);
                            if (rate > 0) {
                                copy.setAmount(copy.getAmount() / rate);
                            }
                        } catch (Exception e) {
                            // Mantieni l'importo originale se c'è un errore
                        }
                    }
                    convertedTransactions.add(copy);
                }

                result.setValue(new Result.Success<>(convertedTransactions));
            }
        });

        transactionsLiveData = result;
    }
}