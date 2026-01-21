package it.unimib.CasHub.source.agency;

public abstract class BaseAgencyDataSource {
    protected AgencyResponseCallback callback;

    public void setCallback(AgencyResponseCallback callback) {
        this.callback = callback;
    }
    public abstract void getAllAgencies(String query);
}
