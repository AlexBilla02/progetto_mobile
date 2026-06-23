package com.example.progetto_mobile;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExchangeRateManager {


    private static final String API_URL ="https://api.exchangerate-api.com/v4/latest/EUR";

    // Shared Preferences come cache per i tassi di conversione da aggiornare una volta al giorno
    private static final String PREF_NAME     = "exchange_rates";
    private static final String PREF_RATES    = "rates_json";
    private static final String PREF_TIMESTAMP = "rates_timestamp";
    private static final long   CACHE_DURATION = 24 * 60 * 60 * 1000L;

    private static ExchangeRateManager instance;
    private final SharedPreferences prefs;
    private JSONObject cachedRates = null;

    public interface RatesCallback {
        void onRatesReady(JSONObject rates);
        void onFailure();
    }

    private ExchangeRateManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // carico la cache esistente in memoria se è presente
        String json = prefs.getString(PREF_RATES, null);
        if (json != null) {
            try {
                cachedRates = new JSONObject(json);
            } catch (Exception e) {
                cachedRates = null;
            }
        }
    }

    public static ExchangeRateManager getInstance(Context context) {
        if (instance == null) {
            instance = new ExchangeRateManager(context.getApplicationContext());
        }
        return instance;
    }

    // funzione per restituire i tassi di conversione
    public void getRates(RatesCallback callback) {
        long lastUpdate = prefs.getLong(PREF_TIMESTAMP, 0);
        boolean cacheValid = (System.currentTimeMillis() - lastUpdate) < CACHE_DURATION;

        if (cacheValid && cachedRates != null) {
            callback.onRatesReady(cachedRates);
            return;
        }

        // Scarico in un nuovo thread i tassi con okhttp
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();

                Request request = new Request.Builder().url(API_URL).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        fallbackToCache(callback);
                        return;
                    }
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    JSONObject rates = json.getJSONObject("rates");

                    // Salva in cache
                    cachedRates = rates;
                    prefs.edit()
                            .putString(PREF_RATES, rates.toString())
                            .putLong(PREF_TIMESTAMP, System.currentTimeMillis())
                            .apply();

                    callback.onRatesReady(rates);
                }
            } catch (Exception e) {
                fallbackToCache(callback);
            }
        }).start();
    }

    // funzione per convertire ad una qualsiasi valuta
    public static double convertToBase(double amount, String fromCurrency,
                                       String baseCurrency, JSONObject rates) {
        if (fromCurrency.equals(baseCurrency)) return amount;
        try {
            if (baseCurrency.equals("EUR")) {
                // Da qualsiasi valuta a EUR
                double rate = rates.getDouble(fromCurrency);
                return amount / rate;
            } else if (fromCurrency.equals("EUR")) {
                // Da EUR a qualsiasi valuta
                double rate = rates.getDouble(baseCurrency);
                return amount * rate;
            } else {
                // Da valuta A a valuta B
                double rateFrom = rates.getDouble(fromCurrency);
                double rateTo   = rates.getDouble(baseCurrency);
                return (amount / rateFrom) * rateTo;
            }
        } catch (Exception e) {
            return amount;
        }
    }

    // Controlla se i tassi sono disponibili in cache
    public boolean hasCache() {
        return cachedRates != null;
    }

    public JSONObject getCachedRates() {
        return cachedRates;
    }

    private void fallbackToCache(RatesCallback callback) {
        if (cachedRates != null) {
            callback.onRatesReady(cachedRates);
        } else {
            callback.onFailure();
        }
    }
}