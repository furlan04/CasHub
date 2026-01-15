package it.unimib.CasHub.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.unimib.CasHub.model.Agency;
import it.unimib.CasHub.model.Result;
import it.unimib.CasHub.repository.agency.IAgencyRepository;

public class AgencyViewModel extends ViewModel {

    private final IAgencyRepository agencyRepository;
    private LiveData<Result<List<Agency>>> agencies;


    public AgencyViewModel(IAgencyRepository agencyRepository) {
        this.agencyRepository = agencyRepository;
    }

    public LiveData<Result<List<Agency>>> getAllAgencies(String query) {
        fetchAgencies(query);
        return agencies;
    }

    void fetchAgencies(String query) {
        agencies = agencyRepository.getAllAgencies(query);
    }
}
