package it.unimib.CasHub.model;

import com.google.gson.annotations.SerializedName;

public class DailyTimeSeries {

    @SerializedName("1. open")
    private String open;

    @SerializedName("2. high")
    private String high;

    @SerializedName("3. low")
    private String low;

    @SerializedName("4. close")
    private String close;

    @SerializedName("5. volume")
    private String volume;

    public DailyTimeSeries() {}

    // Getters
    public String getOpen() {
        return open;
    }

    public String getHigh() {
        return high;
    }

    public String getLow() {
        return low;
    }

    public String getClose() {
        return close;
    }

    public String getVolume() {
        return volume;
    }

    // Setters
    public void setOpen(String open) {
        this.open = open;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }
}
