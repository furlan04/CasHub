package it.unimib.CasHub.model;

import java.util.List;

public class ChartData {
    private List<String> dates;
    private List<Float> prices;

    public ChartData(List<String> dates, List<Float> prices) {
        this.dates = dates;
        this.prices = prices;
    }

    public List<String> getDates() {
        return dates;
    }

    public List<Float> getPrices() {
        return prices;
    }
}
