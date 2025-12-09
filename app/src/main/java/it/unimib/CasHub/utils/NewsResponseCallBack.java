package it.unimib.CasHub.utils;

import java.util.List;

import it.unimib.CasHub.model.Agency;

public interface NewsResponseCallBack {
    void onSuccess(List<Agency> articlesList, long lastUpdate);
    void onFailure(String errorMessage);
}
