package com.example.progetto_mobile;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.progetto_mobile.data.AppDatabase;
import com.example.progetto_mobile.data.Expense;
import com.example.progetto_mobile.data.ExpenseDao;

import java.util.Calendar;
import java.util.List;

// AndroidViewModel invece di ViewModel perché abbiamo bisogno del Context
// per creare il database
public class HomeViewModel extends AndroidViewModel {

    private final ExpenseDao dao;
    private final LiveData<List<Expense>> recentExpenses;
    private final LiveData<Double> todayTotal;

    public HomeViewModel(Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).expenseDao();
        recentExpenses = dao.getRecentExpenses(5);
        // Niente più "now" fisso — prende tutto da inizio giornata in poi
        todayTotal = dao.getTotalFrom(getStartOfDayMillis());
    }

    public LiveData<List<Expense>> getRecentExpenses() {
        return recentExpenses;
    }

    public LiveData<Double> getTodayTotal() {
        return todayTotal;
    }

    public void addExpense(Expense expense) {
        // Eseguito in background tramite ExecutorService
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
}