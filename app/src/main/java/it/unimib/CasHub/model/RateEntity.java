package it.unimib.CasHub.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rates_table")
public class RateEntity {

    @PrimaryKey
    @NonNull
    public String currencyCode;

    public double rateValue;
    public double amount;
    public String base;
    public String date;

    public RateEntity(@NonNull String currencyCode, double rateValue, double amount, String base, String date) {
        this.currencyCode = currencyCode;
        this.rateValue = rateValue;
        this.amount = amount;
        this.base = base;
        this.date = date;
    }
}