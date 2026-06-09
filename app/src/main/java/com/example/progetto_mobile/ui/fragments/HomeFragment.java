package com.example.progetto_mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.progetto_mobile.UserSession;
import com.example.progetto_mobile.data.Expense;
import com.example.progetto_mobile.ui.adapters.ExpenseAdapter;
import com.example.progetto_mobile.HomeViewModel;
import com.example.progetto_mobile.R;
import com.example.progetto_mobile.databinding.FragmentHomeBinding;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ExpenseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupRecyclerView();
        observeExpenses();
        setupSeeAll();
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(Expense expense) {
                ExpenseDetailBottomSheet.newInstance(expense)
                        .show(getParentFragmentManager(), "ExpenseDetail");
            }

            @Override
            public void onExpenseLongClick(Expense expense) {

            }

        });

        binding.rvRecentExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentExpenses.setAdapter(adapter);
    }

    private void observeExpenses() {
        viewModel.getRecentExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses == null || expenses.isEmpty()) {
                binding.rvRecentExpenses.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvRecentExpenses.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
                adapter.setExpenses(expenses);
            }
        });

        viewModel.getTodayTotal().observe(getViewLifecycleOwner(), total -> {
            double amount = (total != null) ? total : 0.0;
            String baseCurrency = UserSession.getBaseCurrency(requireContext());
            binding.tvTodayTotal.setText(String.format(
                    Locale.getDefault(), getString(R.string.today_you_spent), amount, baseCurrency));
            updateBudgetCard(amount);
        });
    }
    private void updateBudgetCard(double todaySpent) {
        double dailyBudget = viewModel.getDailyBudget(requireContext());
        String baseCurrency = UserSession.getBaseCurrency(requireContext());

        if (dailyBudget <= 0) {
            // Budget non impostato
            binding.tvBudgetStatus.setText(R.string.no_budget);
            binding.progressBudget.setVisibility(View.GONE);
            return;
        }

        binding.progressBudget.setVisibility(View.VISIBLE);

        double percentage = (todaySpent / dailyBudget) * 100;
        int progress = (int) Math.min(percentage, 100);
        binding.progressBudget.setProgress(progress);

        double remaining = dailyBudget - todaySpent;

        if (percentage >= 100) {
            binding.tvBudgetStatus.setText(String.format(Locale.getDefault(),
                    getString(R.string.budget_reached), Math.abs(remaining), baseCurrency));
            binding.progressBudget.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(
                                    requireContext(), R.color.budget_exceeded)));

        } else if (percentage >= 80) {
            binding.tvBudgetStatus.setText(String.format(Locale.getDefault(),
                    getString(R.string.budget_remaining), remaining, baseCurrency));
            binding.progressBudget.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(
                                    requireContext(), R.color.budget_warning)));

        } else {
            binding.tvBudgetStatus.setText(String.format(Locale.getDefault(),
                    getString(R.string.budget_remaining), remaining, baseCurrency));
            binding.progressBudget.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.WHITE));
        }
    }

    //se premo vedi tutto apri il fragment dello storico
    private void setupSeeAll() {
        binding.tvSeeAll.setOnClickListener(v -> {
            requireActivity()
                    .findViewById(R.id.bottom_navigation)
                    .<com.google.android.material.bottomnavigation.BottomNavigationView>
                            findViewById(R.id.bottom_navigation)
                    .setSelectedItemId(R.id.historyFragment);
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        Double currentTotal = viewModel.getTodayTotal().getValue();
        updateBudgetCard(currentTotal != null ? currentTotal : 0.0);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}