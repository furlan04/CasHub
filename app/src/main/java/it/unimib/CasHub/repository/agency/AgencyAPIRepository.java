package it.unimib.CasHub.repository.agency;

import androidx.lifecycle.MutableLiveData;
import java.util.List;

import it.unimib.CasHub.model.Agency;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.source.agency.BaseAgencyDataSource;
import it.unimib.CasHub.source.agency.AgencyResponseCallback;

public class AgencyAPIRepository implements IAgencyRepository, AgencyResponseCallback {

    private final BaseAgencyDataSource agencyDataSource;
    // Usiamo un singolo LiveData per i risultati delle agenzie
    private final MutableLiveData<Result<List<Agency>>> agencyLiveData;

    public AgencyAPIRepository(BaseAgencyDataSource agencyDataSource) {
        this.agencyDataSource = agencyDataSource;
        this.agencyDataSource.setCallback(this);
        this.agencyLiveData = new MutableLiveData<>();
    }

    @Override
    public MutableLiveData<Result<List<Agency>>> getAllAgencies(String query) {
        // Avviamo la chiamata asincrona
        agencyDataSource.getAllAgencies(query);

        // Restituiamo il riferimento al LiveData che verrà aggiornato dalle callback
        return agencyLiveData;
    }

    // Callback chiamata dalla DataSource in caso di successo
    @Override
    public void onSuccess(List<Agency> agencyList, long lastUpdate) {
        if (agencyList != null) {
            agencyLiveData.postValue(new Result.Success<>(agencyList));
        }
    }

    // Callback chiamata dalla DataSource in caso di errore
    @Override
    public void onFailure(String errorMessage) {
        agencyLiveData.postValue(new Result.Error<>(errorMessage));
    }
}