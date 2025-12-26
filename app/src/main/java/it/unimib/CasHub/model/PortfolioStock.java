package it.unimib.CasHub.model;

public class PortfolioStock {
    private String symbol;
    private String name;
    private String currency;
    private String exchange;
    private String exchangeFullName;
    private long timestamp; // Quando è stato aggiunto

    public PortfolioStock() {
        // Costruttore vuoto richiesto da Firebase
    }

    public PortfolioStock(String symbol, String name, String currency,
                          String exchange, String exchangeFullName) {
        this.symbol = symbol;
        this.name = name;
        this.currency = currency;
        this.exchange = exchange;
        this.exchangeFullName = exchangeFullName;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters e Setters
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

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getExchangeFullName() {
        return exchangeFullName;
    }

    public void setExchangeFullName(String exchangeFullName) {
        this.exchangeFullName = exchangeFullName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
