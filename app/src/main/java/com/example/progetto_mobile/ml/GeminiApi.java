package com.example.progetto_mobile.ml;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import com.example.progetto_mobile.data.Category;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApi {

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" +
                    "gemini-2.5-flash-lite:generateContent?key=";

    private static final MediaType JSON = MediaType.get("application/json");

    // Risultato strutturato della query Gemini
    public static class GeminiResult {
        public String merchantName = "";
        public double amount       = 0.0;
        public String date         = "";   // formato dd/MM/yyyy
        public String time         = "";   // formato HH:mm
        public String category     = "";   // una delle categorie predefinite
    }

    public interface GeminiCallback {
        void onSuccess(GeminiResult result);
        void onFailure(String errorMessage);
    }

    private final Context context;
    private final String apiKey;
    private final OkHttpClient client;

    public GeminiApi(Context context, String apiKey) {
        this.context = context;
        this.apiKey  = apiKey;
        this.client  = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    // Chiamata asincrona — il callback viene chiamato sul thread di background
    // Ricordati di passare al main thread per aggiornare la UI
    public void analyzeReceipt(String ocrText, GeminiCallback callback) {
        android.util.Log.d("GEMINI_DEBUG", "analyzeReceipt chiamato");

        if (!isNetworkAvailable()) {
            android.util.Log.d("GEMINI_DEBUG", "Rete non disponibile");
            callback.onFailure("Nessuna connessione internet");
            return;
        }

        android.util.Log.d("GEMINI_DEBUG", "Rete disponibile, avvio thread");

        new Thread(() -> {
            android.util.Log.d("GEMINI_DEBUG", "Thread avviato");
            try {
                GeminiResult result = callGemini(ocrText);
                android.util.Log.d("GEMINI_DEBUG", "callGemini completato");
                callback.onSuccess(result);
            } catch (Exception e) {
                android.util.Log.e("GEMINI_DEBUG", "Eccezione: " + e.getMessage());
                callback.onFailure("Errore: " + e.getMessage());
            }
        }).start();
    }

    private GeminiResult callGemini(String ocrText) throws Exception {
        // Costruisce la lista categorie da passare a Gemini
        StringBuilder categories = new StringBuilder();
        for (Category c : Category.values()) {
            categories.append(c.getLabel()).append(", ");
        }

        // Prompt che forza una risposta JSON strutturata
        String prompt = "Analizza il seguente testo estratto da uno scontrino italiano " +
                "e rispondi SOLO con un oggetto JSON valido, senza markdown, " +
                "senza backtick, senza spiegazioni.\n\n" +
                "Il JSON deve avere esattamente questi campi:\n" +
                "- merchantName: nome dell'esercente (stringa)\n" +
                "- amount: importo totale pagato in euro (numero decimale)\n" +
                "- date: data nel formato dd/MM/yyyy (stringa)\n" +
                "- time: orario nel formato HH:mm (stringa, vuota se non trovata)\n" +
                "- category: UNA delle seguenti categorie: " + categories + "\n\n" +
                "Testo scontrino:\n" + ocrText + "\n\n" +
                "Rispondi SOLO con il JSON, nient'altro.";

        // Costruisce il body della richiesta
        JSONObject part = new JSONObject();
        part.put("text", prompt);

        JSONArray parts = new JSONArray();
        parts.put(part);

        JSONObject content = new JSONObject();
        content.put("parts", parts);

        JSONArray contents = new JSONArray();
        contents.put(content);

        // Configurazione: temperatura 0 per risposte deterministiche
        JSONObject genConfig = new JSONObject();
        genConfig.put("temperature", 0);

        JSONObject body = new JSONObject();
        body.put("contents", contents);
        body.put("generationConfig", genConfig);

        Request request = new Request.Builder()
                .url(BASE_URL + apiKey)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            android.util.Log.d("GEMINI_DEBUG", "Status: " + response.code());
            android.util.Log.d("GEMINI_DEBUG", "Body: " + responseBody);

            if (!response.isSuccessful()) {
                throw new IOException("Risposta API non valida: " + response.code());
            }
            return parseGeminiResponse(responseBody);
        }
    }

    private GeminiResult parseGeminiResponse(String responseBody) throws Exception {
        // Estrae il testo dalla risposta Gemini
        JSONObject root = new JSONObject(responseBody);
        String text = root
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim();

        android.util.Log.d("GEMINI_RAW", "Risposta grezza: " + text);

        // Pulisce eventuali backtick residui
        text = text.replace("```json", "").replace("```", "").trim();

        JSONObject json = new JSONObject(text);
        GeminiResult result = new GeminiResult();
        result.merchantName = json.optString("merchantName", "");
        result.amount       = json.optDouble("amount", 0.0);
        result.date         = json.optString("date", "");
        result.time         = json.optString("time", "");
        result.category     = json.optString("category", "");

        android.util.Log.d("GEMINI_PARSED",
                "Merchant: " + result.merchantName +
                        ", Amount: " + result.amount +
                        ", Date: " + result.date +
                        ", Category: " + result.category);

        return result;
    }
}