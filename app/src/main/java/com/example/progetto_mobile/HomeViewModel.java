package com.example.progetto_mobile;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.progetto_mobile.data.AppDatabase;
import com.example.progetto_mobile.data.Expense;
import com.example.progetto_mobile.data.ExpenseDao;

import java.util.Calendar;
import java.util.List;


public class HomeViewModel extends AndroidViewModel {

    private final ExpenseDao dao;
    private final LiveData<List<Expense>> recentExpenses;
    private final LiveData<Double> todayTotal;

    public HomeViewModel(Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).expenseDao();

        String userId = UserSession.getCurrentUserId();
        recentExpenses = dao.getRecentExpenses(userId, 5);
        todayTotal = dao.getTotalFrom(userId, getStartOfDayMillis());
    }

    public LiveData<List<Expense>> getRecentExpenses() {
        return recentExpenses;
    }

    public LiveData<Double> getTodayTotal() {
        return todayTotal;
    }

    public void addExpense(Expense expense) {
        AppDatabase.executor.execute(() -> dao.insert(expense));
    }

    public void updateExpense(Expense expense) {
        AppDatabase.executor.execute(() -> dao.update(expense));
    }
    public void removeExpense(Expense expense) {
        AppDatabase.executor.execute(() -> dao.delete(expense));
    }

    private long getStartOfDayMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    public double getDailyBudget(Context context) {
        double monthly = UserSession.getMonthlyBudget(context);
        if (monthly <= 0) return 0;
        // Divide per i giorni del mese corrente
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        return monthly / daysInMonth;
    }
}