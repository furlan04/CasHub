package it.unimib.CasHub.repository;

import it.unimib.CasHub.utils.ResponseCallback;

public interface ForexRepositoryInterface {
    void getRates(String base, ResponseCallback callback);
    void getCurrencies(ResponseCallback callback);
}