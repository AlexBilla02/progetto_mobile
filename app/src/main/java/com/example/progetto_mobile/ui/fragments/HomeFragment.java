package com.example.progetto_mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.progetto_mobile.Expense;
import com.example.progetto_mobile.ExpenseAdapter;
import com.example.progetto_mobile.HomeViewModel;
import com.example.progetto_mobile.R;
import com.example.progetto_mobile.databinding.FragmentHomeBinding;
import java.util.List;
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
                // dettaglio spesa — lo faremo dopo
            }

            @Override
            public void onExpenseLongClick(Expense expense) {
                // elimina spesa — lo faremo dopo
            }
        });

        binding.rvRecentExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentExpenses.setAdapter(adapter);
    }

    private void observeExpenses() {
        viewModel.getExpenses().observe(getViewLifecycleOwner(), expenses -> {
            // Mostra solo le ultime 5 nella home
            List<Expense> recent = expenses.size() > 5
                    ? expenses.subList(0, 5)
                    : expenses;

            adapter.setExpenses(recent);

            // Mostra/nascondi messaggio vuoto
            if (expenses.isEmpty()) {
                binding.rvRecentExpenses.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvRecentExpenses.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
            }

            // Aggiorna totale giornaliero
            double total = viewModel.getTodayTotal();
            binding.tvTodayTotal.setText(
                    String.format(Locale.getDefault(), "Oggi hai speso: %.2f €", total)
            );
        });
    }

    private void setupSeeAll() {
        binding.tvSeeAll.setOnClickListener(v -> {
            // Seleziona il tab Storico come se l'utente lo avesse premuto
            requireActivity()
                    .findViewById(R.id.bottom_navigation)
                    // cast necessario per chiamare setSelectedItemId
                    .<com.google.android.material.bottomnavigation.BottomNavigationView>
                            findViewById(R.id.bottom_navigation)
                    .setSelectedItemId(R.id.historyFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}