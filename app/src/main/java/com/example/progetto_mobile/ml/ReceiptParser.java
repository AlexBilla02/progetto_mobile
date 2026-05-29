package com.example.progetto_mobile.ml;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptParser {

    // Risultato del parsing — tutti i campi estratti
    public static class ParsedReceipt {
        public String merchantName = "";   // nome esercente
        public double amount       = 0.0;  // importo totale
        public String currency     = "EUR";
        public long   date         = System.currentTimeMillis(); // default oggi
        public String rawText      = "";   // testo grezzo per debug
    }

    public static ParsedReceipt parse(String ocrText) {
        ParsedReceipt result = new ParsedReceipt();
        result.rawText = ocrText;

        String[] lines = ocrText.split("\n");

        result.merchantName = extractMerchant(lines);
        result.amount       = extractAmount(ocrText);
        result.date         = extractDate(ocrText);

        return result;
    }

    // Il nome dell'esercente è quasi sempre nelle prime righe
    // in maiuscolo, prima dell'indirizzo
    private static String extractMerchant(String[] lines) {
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            // Salta righe troppo corte o che sembrano indirizzi/numeri
            if (line.length() < 3) continue;
            if (line.matches(".*\\d{5}.*")) continue;      // CAP
            if (line.toLowerCase().contains("via ")) continue;
            if (line.toLowerCase().contains("viale ")) continue;
            if (line.toLowerCase().contains("corso ")) continue;
            if (line.toLowerCase().contains("piazza ")) continue;
            if (line.toLowerCase().contains("p.iva")) continue;
            if (line.toLowerCase().contains("tel")) continue;
            return capitalizeWords(line);
        }
        return "";
    }

    // Cerca il totale con pattern comuni sugli scontrini italiani
    private static double extractAmount(String text) {
        String[] lines = text.split("\n");

        // Strategia 1: cerca keyword TOTALE (anche parziale/corrotto)
        // usando una regex fuzzy che accetta qualsiasi carattere dopo "TOT"
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim().toLowerCase();

            // Matcha "totale" anche se seguito da parole corrotte
            if (line.matches(".*tot[a-z]+.*") &&
                    !line.contains("subtotale") &&
                    !line.contains("sconto")) {

                // Cerca importo sulla stessa riga
                Matcher m = Pattern.compile("(\\d{1,4}[.,]\\d{2})").matcher(lines[i]);
                if (m.find()) {
                    return parseAmount(m.group(1));
                }

                // Cerca importo nelle 3 righe successive
                for (int j = i + 1; j < Math.min(i + 4, lines.length); j++) {
                    Matcher mNext = Pattern.compile("(\\d{1,4}[.,]\\d{2})")
                            .matcher(lines[j]);
                    if (mNext.find()) {
                        double val = parseAmount(mNext.group(1));
                        // Ignora percentuali IVA (es. 22,00%)
                        if (!lines[j].contains("%") && val > 0) {
                            return val;
                        }
                    }
                }
            }
        }

        // Strategia 2: fallback — prende il numero che appare più volte
        // (il totale tende a ripetersi sullo scontrino)
        Map<Double, Integer> frequency = new HashMap<>();
        Matcher mAll = Pattern.compile("\\b(\\d{1,4}[.,]\\d{2})\\b").matcher(text);
        while (mAll.find()) {
            double val = parseAmount(mAll.group(1));
            if (val > 1 && val < 10000 && !text.contains(mAll.group(1) + "%")) {
                frequency.put(val, frequency.getOrDefault(val, 0) + 1);
            }
        }

        return frequency.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0.0);
    }

    private static long extractDate(String text) {
        // Pattern più permissivo: accetta spazio opzionale prima del trattino
        Pattern dateTimePattern = Pattern.compile(
                "\\b(\\d{1,2})[/\\-\\.](\\d{1,2})[/\\-\\.](\\d{2,4})" +
                        "\\s*-?\\s*(\\d{1,2}):(\\d{2})\\b");

        Matcher m = dateTimePattern.matcher(text);
        if (m.find()) {
            try {
                int day    = Integer.parseInt(m.group(1));
                int month  = Integer.parseInt(m.group(2)) - 1;
                int year   = Integer.parseInt(m.group(3));
                int hour   = Integer.parseInt(m.group(4));
                int minute = Integer.parseInt(m.group(5));
                if (year < 100) year += 2000;

                if (day < 1 || day > 31 || month < 0 || month > 11)
                    return System.currentTimeMillis();

                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day, hour, minute, 0);
                return cal.getTimeInMillis();
            } catch (NumberFormatException e) {
                // ignora
            }
        }

        // Fallback: solo data senza ora
        Pattern dateOnly = Pattern.compile(
                "\\b(\\d{1,2})[/\\-\\.](\\d{1,2})[/\\-\\.](\\d{2,4})\\b");
        Matcher mDate = dateOnly.matcher(text);
        if (mDate.find()) {
            try {
                int day   = Integer.parseInt(mDate.group(1));
                int month = Integer.parseInt(mDate.group(2)) - 1;
                int year  = Integer.parseInt(mDate.group(3));
                if (year < 100) year += 2000;

                if (day < 1 || day > 31 || month < 0 || month > 11)
                    return System.currentTimeMillis();

                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day, 0, 0, 0);
                return cal.getTimeInMillis();
            } catch (NumberFormatException e) {
                // ignora
            }
        }

        return System.currentTimeMillis();
    }
    private static double parseAmount(String raw) {
        if (raw == null) return 0;
        // Normalizza separatore decimale
        String normalized = raw.replace(",", ".");
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String capitalizeWords(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}