package com.example.progetto_mobile.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.progetto_mobile.HistoryViewModel;
import com.example.progetto_mobile.data.Expense;
import com.example.progetto_mobile.databinding.FragmentHistoryBinding;
import com.example.progetto_mobile.ui.adapters.HistoryAdapter;
import java.util.Calendar;
import java.util.List;
import androidx.core.content.ContextCompat;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        setupRecyclerView();
        setupChips();
        setupCustomRange();
        observeExpenses();
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(expense ->
                ExpenseDetailBottomSheet.newInstance(expense)
                        .show(getParentFragmentManager(), "ExpenseDetail")
        );
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);
    }

    private void setupChips() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);

            if      (id == binding.chipAll.getId())       viewModel.setFilter(HistoryViewModel.Filter.ALL);
            else if (id == binding.chipToday.getId())     viewModel.setFilter(HistoryViewModel.Filter.TODAY);
            else if (id == binding.chipThisWeek.getId())  viewModel.setFilter(HistoryViewModel.Filter.THIS_WEEK);
            else if (id == binding.chipLast7.getId())     viewModel.setFilter(HistoryViewModel.Filter.LAST_7_DAYS);
            else if (id == binding.chipThisMonth.getId()) viewModel.setFilter(HistoryViewModel.Filter.THIS_MONTH);
            else if (id == binding.chipLast30.getId())    viewModel.setFilter(HistoryViewModel.Filter.LAST_30_DAYS);
            else if (id == binding.chipLast365.getId())   viewModel.setFilter(HistoryViewModel.Filter.LAST_365_DAYS);
        });
    }

    private void setupCustomRange() {
        binding.btnCustomRange.setOnClickListener(v -> showFromDatePicker());
    }

    private void showFromDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    Calendar from = Calendar.getInstance();
                    from.set(year, month, day, 0, 0, 0);
                    // Dopo aver scelto la data iniziale, chiedi quella finale
                    showToDatePicker(from.getTimeInMillis());
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showToDatePicker(long from) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    Calendar to = Calendar.getInstance();
                    to.set(year, month, day, 23, 59, 59);

                    // Deseleziona tutti i chip e applica il range custom
                    binding.chipGroupFilters.clearCheck();
                    viewModel.setCustomRange(from, to.getTimeInMillis());

                    // Mostra le date scelte accanto al pulsante
                    String label = android.text.format.DateFormat
                            .format("dd/MM/yy", from) + " → " +
                            android.text.format.DateFormat
                                    .format("dd/MM/yy", to.getTimeInMillis());
                    binding.tvCustomRange.setText(label);
                    binding.tvCustomRange.setTextColor(
                            ContextCompat.getColor(requireContext(),
                                    com.example.progetto_mobile.R.color.primary));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void observeExpenses() {
        viewModel.expenses.observe(getViewLifecycleOwner(), expenses -> {
            if (expenses == null || expenses.isEmpty()) {
                binding.rvHistory.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvHistory.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
                adapter.setExpenses(expenses);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}