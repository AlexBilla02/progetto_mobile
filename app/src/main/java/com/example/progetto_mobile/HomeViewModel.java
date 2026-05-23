package com.example.progetto_mobile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    // Lista in memoria — quando integreremo Room, questo diventerà un DAO
    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Expense>> getExpenses() {
        return expenses;
    }

    public void addExpense(Expense expense) {
        List<Expense> current = expenses.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(0, expense); // aggiunge in cima (più recente prima)
        expenses.setValue(current);
    }

    public void removeExpense(Expense expense) {
        List<Expense> current = expenses.getValue();
        if (current != null) {
            current.remove(expense);
            expenses.setValue(current);
        }
    }

    // Somma le spese di oggi
    public double getTodayTotal() {
        List<Expense> list = expenses.getValue();
        if (list == null) return 0;

        long startOfDay = getStartOfDayMillis();
        double total = 0;
        for (Expense e : list) {
            if (e.getDate() >= startOfDay) {
                total += e.getAmount();
            }
        }
        return total;
    }

    private long getStartOfDayMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}