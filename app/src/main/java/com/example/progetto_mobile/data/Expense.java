package com.example.progetto_mobile.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "expenses")  // questa classe = tabella "expenses"
public class Expense {

    @PrimaryKey(autoGenerate = true)  // id generato automaticamente da Room
    private long id;

    private String userId;
    private String name;
    private String category;   // salviamo la stringa, non l'enum direttamente
    private double amount;
    private double amountBase;
    private String currency;
    private long date;         // timestamp in millisecondi — contiene sia data che ora
    private String note;

    // Costruttore senza id (Room lo genera da solo)
    public Expense(String userId,String name, String category, double amount,
                   String currency, long date, String note) {
        this.userId=userId;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.date = date;
        this.note = note;
    }

    // Getters — Room ne ha bisogno per leggere i valori
    public String getUserId() { return userId; }
    public long getId()         { return id; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public double getAmount()   { return amount; }
    public String getCurrency() { return currency; }
    public long getDate()       { return date; }
    public String getNote()     { return note; }
    public double getAmountBase() { return amountBase; }
    // Setter per l'id — Room ne ha bisogno per scrivere l'id generato

    public void setUserId(String userId) { this.userId = userId; }
    public void setId(long id)  { this.id = id; }
    public void setName(String name)         { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount)     { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setDate(long date)           { this.date = date; }
    public void setNote(String note)         { this.note = note; }
    public void setAmountBase(double amountBase) { this.amountBase = amountBase; }

    // Metodi di formattazione — non sono colonne, Room li ignora
    public String getFormattedTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(date));
    }

    public String getFormattedDate() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date(date));
    }

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
    public String getFormattedDateTime() {
        return new SimpleDateFormat("dd/MM/yy, HH:mm", Locale.getDefault())
                .format(new Date(date));
    }

    // Metodo comodo per ottenere l'enum Category dalla stringa salvata
    public Category getCategoryEnum() {
        return Category.fromLabel(category);
    }
}