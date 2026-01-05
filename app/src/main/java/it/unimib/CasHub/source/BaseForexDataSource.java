package it.unimib.CasHub.source;

public abstract class BaseForexDataSource {
    protected ForexCallback callback;

    public void setCallback(ForexCallback callback) {
        this.callback = callback;
    }

    public abstract void getRates(String base);
    public abstract void getCurrencies();
}
