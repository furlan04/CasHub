package it.unimib.CasHub.repository.agency;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.unimib.CasHub.model.Agency;
import it.unimib.CasHub.model.Result;

public interface IAgencyRepository {
    MutableLiveData<Result<List<Agency>>> getAllAgencies(String query);

}
