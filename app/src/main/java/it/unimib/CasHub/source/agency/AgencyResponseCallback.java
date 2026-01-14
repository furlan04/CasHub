package it.unimib.CasHub.source.agency;

import java.util.List;

import it.unimib.CasHub.model.Agency;

public interface AgencyResponseCallback {
    void onSuccess(List<Agency> agencyList, long lastUpdate);
    void onFailure(String errorMessage);
}
