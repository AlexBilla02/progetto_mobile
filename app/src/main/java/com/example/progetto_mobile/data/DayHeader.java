package com.example.progetto_mobile.data;

// Rappresnta la riga di intestazione di ogni giorno nello storico
public class DayHeader {

    private final String dateLabel;
    private final double dayTotal;

    public DayHeader(String dateLabel, double dayTotal) {
        this.dateLabel = dateLabel;
        this.dayTotal  = dayTotal;
    }

    public String getDateLabel() {
        return dateLabel;
    }
    public double getDayTotal()  {
        return dayTotal;
    }
}