package com.example.progetto_mobile.data;

// Rappresenta la riga di intestazione di ogni giorno nello storico
public class DayHeader {

    private final String dateLabel;   // es. "20/05/2026"
    private final double dayTotal;    // totale speso quel giorno

    public DayHeader(String dateLabel, double dayTotal) {
        this.dateLabel = dateLabel;
        this.dayTotal  = dayTotal;
    }

    public String getDateLabel() { return dateLabel; }
    public double getDayTotal()  { return dayTotal; }
}