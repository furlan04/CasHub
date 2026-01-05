package it.unimib.CasHub.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class DailyTimeSeriesResponse {

    @SerializedName("Meta Data")
    private Map<String, String> metaData;

    @SerializedName("Time Series (Daily)")
    private Map<String, DailyTimeSeries> timeSeries;

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public Map<String, DailyTimeSeries> getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(Map<String, DailyTimeSeries> timeSeries) {
        this.timeSeries = timeSeries;
    }
}
