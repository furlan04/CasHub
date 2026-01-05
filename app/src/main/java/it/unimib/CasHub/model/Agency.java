package it.unimib.CasHub.model;

import androidx.room.PrimaryKey;
import java.io.Serializable;

public class Agency implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String symbol;
    private String name;
    private String currency;
    private String exchangeFullName;
    private String exchange;

    public Agency(String symbol, String name, String currency, String exchangeFullName, String exchange) {
        this.symbol = symbol;
        this.name = name;
        this.currency = currency;
        this.exchangeFullName = exchangeFullName;
        this.exchange = exchange;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchangeFullName() {
        return exchangeFullName;
    }

    public void setExchangeFullName(String exchangeFullName) {
        this.exchangeFullName = exchangeFullName;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}