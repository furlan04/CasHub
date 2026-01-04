package it.unimib.CasHub.source.portfolio;

public interface PortfolioResponseCallback<T> {
    void onSuccess(T response);
    void onError(String errorMessage);
}
