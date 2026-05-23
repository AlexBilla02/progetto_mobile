package com.example.progetto_mobile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Expense {

    private static long idCounter = 0; // id temporaneo finché non c'è Room

    private long id;
    private String name;
    private Category category;
    private double amount;
    private String currency;  // es. "EUR", "USD"
    private long date;        // timestamp in millisecondi
    private String note;

    public Expense(String name, Category category, double amount, String currency, long date, String note) {
        this.id = ++idCounter;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.date = date;
        this.note = note;
    }

    // Getters
    public long getId()           { return id; }
    public String getName()       { return name; }
    public Category getCategory() { return category; }
    public double getAmount()     { return amount; }
    public String getCurrency()   { return currency; }
    public long getDate()         { return date; }
    public String getNote()       { return note; }

    // Restituisce l'ora formattata, es. "14:32"
    public String getFormattedTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(date));
    }

    // Restituisce la data formattata, es. "21 mag 2026"
    public String getFormattedDate() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(date));
    }

    // Restituisce l'importo formattato, es. "12,50 €"
    public String getFormattedAmount() {
        String symbol;
        switch (currency) {
            case "USD": symbol = "$"; break;
            case "GBP": symbol = "£"; break;
            case "CHF": symbol = "Fr"; break;
            default:    symbol = "€"; break;
        }
        return String.format(Locale.getDefault(), "%s %.2f", symbol, amount);
    }
}