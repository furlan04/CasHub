package it.unimib.CasHub.model;

public class PortfolioStock {
    private String symbol;
    private String name;
    private String currency;
    private String exchange;
    private String exchangeFullName;
    private long timestamp;

    private double quantity;
    private double averagePrice;
    private double currentPrice;

    public PortfolioStock() {

    }

    public PortfolioStock(String symbol, String name, String currency,
                          String exchange, String exchangeFullName) {
        this.symbol = symbol;
        this.name = name;
        this.currency = currency;
        this.exchange = exchange;
        this.exchangeFullName = exchangeFullName;
        this.timestamp = System.currentTimeMillis();
        this.quantity = 0;
        this.averagePrice = 0;
    }



    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getExchangeFullName() { return exchangeFullName; }
    public void setExchangeFullName(String exchangeFullName) { this.exchangeFullName = exchangeFullName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }


    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getAveragePrice() { return averagePrice; }
    public void setAveragePrice(double averagePrice) { this.averagePrice = averagePrice; }
    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

}
