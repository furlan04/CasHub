package it.unimib.CasHub.source;

public abstract class BaseRatesDataSource {
    protected ForexCallback callback;

    public void setCallback(ForexCallback callback) {
        this.callback = callback;
    }

    public abstract void getRates(String base);
}
