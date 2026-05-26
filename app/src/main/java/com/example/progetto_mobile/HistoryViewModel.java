package com.example.progetto_mobile;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.progetto_mobile.data.AppDatabase;
import com.example.progetto_mobile.data.Expense;
import com.example.progetto_mobile.data.ExpenseDao;
import java.util.Calendar;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    // Rappresenta il filtro attualmente selezionato
    public enum Filter {
        ALL, TODAY, THIS_WEEK, LAST_7_DAYS,
        THIS_MONTH, LAST_30_DAYS, LAST_365_DAYS, CUSTOM
    }

    private final ExpenseDao dao;
    private final MutableLiveData<long[]> dateRange = new MutableLiveData<>();
    public final LiveData<List<Expense>> expenses;  // ← solo dichiarazione, senza assegnazione

    public HistoryViewModel(Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).expenseDao();

        expenses = Transformations.switchMap(dateRange, range -> {
            if (range == null) {
                return dao.getAllExpenses();
            } else {
                return dao.getExpensesBetween(range[0], range[1]);
            }
        });

        dateRange.setValue(null);
    }

    // Chiamato quando l'utente seleziona un filtro rapido
    public void setFilter(Filter filter) {
        dateRange.setValue(calculateRange(filter));
    }

    // Chiamato quando l'utente sceglie un range custom
    public void setCustomRange(long from, long to) {
        // "to" è impostato alla fine del giorno selezionato
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(to);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        dateRange.setValue(new long[]{from, cal.getTimeInMillis()});
    }

    // Calcola il range di timestamp per ogni filtro
    private long[] calculateRange(Filter filter) {
        if (filter == Filter.ALL) return null;

        Calendar start = Calendar.getInstance();
        Calendar end   = Calendar.getInstance();

        // Fine sempre = fine di oggi
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);

        switch (filter) {
            case TODAY:
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                break;

            case THIS_WEEK:
                // Da lunedì di questa settimana
                start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                break;

            case LAST_7_DAYS:
                start.add(Calendar.DAY_OF_YEAR, -7);
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                break;

            case THIS_MONTH:
                // Dall'1 del mese corrente
                start.set(Calendar.DAY_OF_MONTH, 1);
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                break;

            case LAST_30_DAYS:
                start.add(Calendar.DAY_OF_YEAR, -30);
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                break;

            case LAST_365_DAYS:
                start.add(Calendar.DAY_OF_YEAR, -365);
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                break;
        }

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }
}