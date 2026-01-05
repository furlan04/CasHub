package it.unimib.CasHub.utils;

import java.util.List;

import it.unimib.CasHub.model.Agency;

public interface AgencyResponseCallBack {
    void onSuccess(List<Agency> agencyList, long lastUpdate);
    void onFailure(String errorMessage);
}
