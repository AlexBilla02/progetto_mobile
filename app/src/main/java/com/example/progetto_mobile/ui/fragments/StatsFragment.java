package com.example.progetto_mobile.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.progetto_mobile.R;
import com.example.progetto_mobile.StatsViewModel;
import com.example.progetto_mobile.UserSession;
import com.example.progetto_mobile.data.Category;
import com.example.progetto_mobile.data.CategoryTotal;
import com.example.progetto_mobile.data.DailyTotal;
import com.example.progetto_mobile.databinding.FragmentStatsBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        setupCharts();
        setupMonthNavigation();
        observeData();
        updateMonthLabel();
    }

    private void setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener(v -> {
            int[] ym = viewModel.getCurrentYearMonth();
            Calendar cal = Calendar.getInstance();
            cal.set(ym[0], ym[1], 1);
            cal.add(Calendar.MONTH, -1);
            viewModel.setMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
            updateMonthLabel();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            int[] ym = viewModel.getCurrentYearMonth();
            Calendar cal = Calendar.getInstance();
            cal.set(ym[0], ym[1], 1);
            cal.add(Calendar.MONTH, 1);
            // Non permette di andare oltre il mese corrente
            Calendar now = Calendar.getInstance();
            if (cal.get(Calendar.YEAR) <= now.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) <= now.get(Calendar.MONTH)) {
                viewModel.setMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
                updateMonthLabel();
            }
        });
    }

    private void updateMonthLabel() {
        int[] ym = viewModel.getCurrentYearMonth();
        Calendar cal = Calendar.getInstance();
        cal.set(ym[0], ym[1], 1);
        String label = new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(cal.getTime());
        binding.tvMonthLabel.setText(label.substring(0, 1).toUpperCase()
                + label.substring(1));
    }

    private void setupCharts() {
        // Pie chart
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleRadius(40f);
        binding.pieChart.setHoleColor(ContextCompat.getColor(
                requireContext(), R.color.color_card_background));
        binding.pieChart.setTransparentCircleRadius(45f);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.getLegend().setEnabled(true);
        binding.pieChart.getLegend().setTextColor(
                ContextCompat.getColor(requireContext(), R.color.color_on_surface));
        binding.pieChart.setEntryLabelColor(Color.WHITE);
        binding.pieChart.setEntryLabelTextSize(11f);

        // Bar chart
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.getAxisRight().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
        binding.barChart.getAxisLeft().setTextColor(
                ContextCompat.getColor(requireContext(), R.color.color_on_surface_secondary));
        binding.barChart.getXAxis().setTextColor(
                ContextCompat.getColor(requireContext(), R.color.color_on_surface_secondary));
        binding.barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.barChart.getXAxis().setGranularity(1f);
        binding.barChart.getAxisLeft().setAxisMinimum(0f);
        binding.barChart.setBackgroundColor(Color.TRANSPARENT);
    }

    private void observeData() {
        String baseCurrency = UserSession.getBaseCurrency(requireContext());

        // Budget
        viewModel.monthTotal.observe(getViewLifecycleOwner(), total -> {
            double spent = total != null ? total : 0.0;
            double budget = UserSession.getMonthlyBudget(requireContext());
            updateBudgetCard(spent, budget, baseCurrency);
        });

        // Pie chart
        viewModel.categoryTotals.observe(getViewLifecycleOwner(), totals -> {
            if (totals == null || totals.isEmpty()) {
                binding.pieChart.setVisibility(View.GONE);
                binding.tvPieEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.pieChart.setVisibility(View.VISIBLE);
                binding.tvPieEmpty.setVisibility(View.GONE);
                updatePieChart(totals);
            }
        });

        // Bar chart
        viewModel.dailyTotals.observe(getViewLifecycleOwner(), totals -> {
            if (totals == null || totals.isEmpty()) {
                binding.barChart.setVisibility(View.GONE);
                binding.tvBarEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.barChart.setVisibility(View.VISIBLE);
                binding.tvBarEmpty.setVisibility(View.GONE);
                updateBarChart(totals);
            }
        });
    }

    private void updateBudgetCard(double spent, double budget, String baseCurrency) {
        if (budget <= 0) {
            binding.progressMonthly.setVisibility(View.GONE);
            binding.tvSpent.setVisibility(View.GONE);
            binding.tvBudgetRemaining.setVisibility(View.GONE);
            binding.tvBudgetNotSet.setVisibility(View.VISIBLE);
            return;
        }

        binding.progressMonthly.setVisibility(View.VISIBLE);
        binding.tvSpent.setVisibility(View.VISIBLE);
        binding.tvBudgetRemaining.setVisibility(View.VISIBLE);
        binding.tvBudgetNotSet.setVisibility(View.GONE);

        double percentage = (spent / budget) * 100;
        int progress = (int) Math.min(percentage, 100);

        binding.progressMonthly.setProgress(progress);
        binding.tvSpent.setText(String.format(Locale.getDefault(),
                getString(R.string.spent), spent, baseCurrency));

        double remaining = budget - spent;
        if (remaining >= 0) {
            binding.tvBudgetRemaining.setText(String.format(Locale.getDefault(),
                    getString(R.string.remaining), remaining, baseCurrency));
        } else {
            binding.tvBudgetRemaining.setText(String.format(Locale.getDefault(),
                    getString(R.string.exceed), Math.abs(remaining), baseCurrency));
        }

        // Colore progress bar
        int color;
        if (percentage >= 100) {
            color = ContextCompat.getColor(requireContext(), R.color.budget_exceeded);
        } else if (percentage >= 80) {
            color = ContextCompat.getColor(requireContext(), R.color.budget_warning);
        } else {
            color = ContextCompat.getColor(requireContext(), R.color.budget_ok);
        }
        binding.progressMonthly.setProgressTintList(
                android.content.res.ColorStateList.valueOf(color));
    }

    private void updatePieChart(List<CategoryTotal> totals) {
        List<PieEntry> entries  = new ArrayList<>();
        List<Integer>  colors   = new ArrayList<>();

        for (CategoryTotal ct : totals) {
            if (ct.total <= 0) continue;
            entries.add(new PieEntry((float) ct.total, ct.category));
            colors.add(ContextCompat.getColor(requireContext(),
                    Category.fromLabel(ct.category).getColorRes()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(2f);

        binding.pieChart.setData(new PieData(dataSet));
        binding.pieChart.invalidate();
    }

    private void updateBarChart(List<DailyTotal> totals) {
        // 1. Determina quanti giorni ha il mese attualmente selezionato nel ViewModel
        int[] ym = viewModel.getCurrentYearMonth();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, ym[0]);
        cal.set(Calendar.MONTH, ym[1]);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 2. Calcola il timestamp di partenza del primo giorno del mese per mappare i dati del DB
        Calendar startCal = Calendar.getInstance();
        startCal.set(ym[0], ym[1], 1, 0, 0, 0);
        long startDay = startCal.getTimeInMillis() / 86400000L;

        // 3. Inizializza le strutture dati per TUTTI i giorni del mese
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Riuniamo l'asse X partendo da 0
        float[] dailyValues = new float[daysInMonth + 1];

        if (totals != null) {
            for (DailyTotal dt : totals) {
                int dayOfMonth = (int) (dt.day - startDay) + 1;
                if (dayOfMonth >= 1 && dayOfMonth <= daysInMonth) {
                    dailyValues[dayOfMonth] = (float) dt.total;
                }
            }
        }

        // 4. Popola MPAndroidChart con ogni singolo giorno del mese
        for (int i = 1; i <= daysInMonth; i++) {
            entries.add(new BarEntry(i, dailyValues[i]));
            labels.add(String.valueOf(i));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setValueTextColor(
                ContextCompat.getColor(requireContext(), R.color.color_on_surface));
        dataSet.setValueTextSize(9f);

        // Mostra i valori numerici sopra la barra solo se la spesa è maggiore di zero
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                if (barEntry.getY() == 0f) {
                    return ""; // Nasconde lo "0.0" sui giorni senza spese per non affollare il grafico
                }
                return String.format(Locale.getDefault(), "%.1f", barEntry.getY());
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f); // Larghezza della barra proporzionata allo spazio di un giorno

        // 5. Configura l'asse X in modo rigido sul range del mese corrente
        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setAxisMinimum(0.5f);
        xAxis.setAxisMaximum(daysInMonth + 0.5f);
        xAxis.setLabelCount(daysInMonth);

        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int day = (int) value;
                if (day >= 1 && day <= daysInMonth) {
                    return String.valueOf(day);
                }
                return "";
            }
        });

        binding.barChart.setData(barData);
        binding.barChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}