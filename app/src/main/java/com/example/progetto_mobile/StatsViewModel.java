package com.example.progetto_mobile;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.progetto_mobile.data.AppDatabase;
import com.example.progetto_mobile.data.CategoryTotal;
import com.example.progetto_mobile.data.DailyTotal;
import com.example.progetto_mobile.data.ExpenseDao;
import java.util.Calendar;
import java.util.List;

public class StatsViewModel extends AndroidViewModel {

    private final ExpenseDao dao;
    private final String userId;

    // Mese corrente in analisi — anno e mese
    private final MutableLiveData<long[]> monthRange = new MutableLiveData<>();

    public final LiveData<Double> monthTotal;
    public final LiveData<List<CategoryTotal>> categoryTotals;
    public final LiveData<List<DailyTotal>> dailyTotals;

    public StatsViewModel(Application application) {
        super(application);
        dao    = AppDatabase.getInstance(application).expenseDao();
        userId = UserSession.getCurrentUserId();

        // Default: mese corrente
        monthRange.setValue(getCurrentMonthRange());

        monthTotal = Transformations.switchMap(monthRange, range ->
                dao.getTotalBetween(userId, range[0], range[1]));

        categoryTotals = Transformations.switchMap(monthRange, range ->
                dao.getTotalByCategory(userId, range[0], range[1]));

        dailyTotals = Transformations.switchMap(monthRange, range ->
                dao.getDailyTotals(userId, range[0], range[1]));
    }

    public void setMonth(int year, int month) {
        Calendar start = Calendar.getInstance();
        start.set(year, month, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(year, month, start.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

        monthRange.setValue(new long[]{start.getTimeInMillis(), end.getTimeInMillis()});
    }

    public long[] getCurrentMonthRange() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.DAY_OF_MONTH,
                end.getActualMaximum(Calendar.DAY_OF_MONTH));
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    // Restituisce anno e mese correntemente visualizzati
    public int[] getCurrentYearMonth() {
        long[] range = monthRange.getValue();
        Calendar cal = Calendar.getInstance();
        if (range != null) cal.setTimeInMillis(range[0]);
        return new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)};
    }
}