package it.unimib.CasHub.utils;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;

public class StockCache {
    private static final String PREF_NAME = "stock_cache";
    private static final long CACHE_DURATION = 30 * 60 * 1000;

    public static class CachedStock {
        public String companyName;
        public String currency;
        public String exchange;
        public String exchangeFull;
        public double currentPrice;
        public long timestamp;

        public CachedStock(String companyName, String currency, String exchange, String exchangeFull, double currentPrice) {
            this.companyName = companyName;
            this.currency = currency;
            this.exchange = exchange;
            this.exchangeFull = exchangeFull;
            this.currentPrice = currentPrice;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static void saveStock(Context context, String symbol, String companyName,
                                 String currency, String exchange, String exchangeFull, double currentPrice) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            JSONObject json = new JSONObject();
            json.put("companyName", companyName);
            json.put("currency", currency);
            json.put("exchange", exchange);
            json.put("exchangeFull", exchangeFull);
            json.put("currentPrice", currentPrice);
            json.put("timestamp", System.currentTimeMillis());

            editor.putString(symbol, json.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CachedStock getStock(Context context, String symbol) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(symbol, null);

        if (json == null) return null;

        try {
            JSONObject obj = new JSONObject(json);
            long timestamp = obj.getLong("timestamp");

            // Verifica se cache valida (30 minuti)
            if (System.currentTimeMillis() - timestamp > CACHE_DURATION) {
                return null;
            }

            return new CachedStock(
                    obj.getString("companyName"),
                    obj.getString("currency"),
                    obj.getString("exchange"),
                    obj.getString("exchangeFull"),
                    obj.getDouble("currentPrice")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clearCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
