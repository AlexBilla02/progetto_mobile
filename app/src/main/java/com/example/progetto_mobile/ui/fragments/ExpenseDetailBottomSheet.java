package com.example.progetto_mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.progetto_mobile.data.Category;
import com.example.progetto_mobile.data.Expense;
import com.example.progetto_mobile.HomeViewModel;
import com.example.progetto_mobile.databinding.BottomSheetExpenseDetailBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ExpenseDetailBottomSheet extends BottomSheetDialogFragment {

    // Chiave per passare la spesa nel Bundle
    public static final String ARG_EXPENSE_ID       = "expense_id";
    public static final String ARG_EXPENSE_NAME     = "expense_name";
    public static final String ARG_EXPENSE_CATEGORY = "expense_category";
    public static final String ARG_EXPENSE_AMOUNT   = "expense_amount";
    public static final String ARG_EXPENSE_CURRENCY = "expense_currency";
    public static final String ARG_EXPENSE_DATE     = "expense_date";
    public static final String ARG_EXPENSE_NOTE     = "expense_note";

    private BottomSheetExpenseDetailBinding binding;
    private HomeViewModel viewModel;
    private Expense expense;

    // Factory method — modo pulito per creare il bottom sheet con i dati
    public static ExpenseDetailBottomSheet newInstance(Expense expense) {
        ExpenseDetailBottomSheet sheet = new ExpenseDetailBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_EXPENSE_ID,           expense.getId());
        args.putString(ARG_EXPENSE_NAME,       expense.getName());
        args.putString(ARG_EXPENSE_CATEGORY,   expense.getCategory());
        args.putDouble(ARG_EXPENSE_AMOUNT,     expense.getAmount());
        args.putString(ARG_EXPENSE_CURRENCY,   expense.getCurrency());
        args.putLong(ARG_EXPENSE_DATE,         expense.getDate());
        args.putString(ARG_EXPENSE_NOTE,       expense.getNote());
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetExpenseDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Ricostruisce l'oggetto Expense dai parametri del Bundle
        Bundle args = requireArguments();
        expense = new Expense(
                args.getString(ARG_EXPENSE_NAME),
                args.getString(ARG_EXPENSE_CATEGORY),
                args.getDouble(ARG_EXPENSE_AMOUNT),
                args.getString(ARG_EXPENSE_CURRENCY),
                args.getLong(ARG_EXPENSE_DATE),
                args.getString(ARG_EXPENSE_NOTE)
        );
        expense.setId(args.getLong(ARG_EXPENSE_ID));

        populateFields();
        setupButtons();
    }

    private void populateFields() {
        // Cerchio categoria
        String initial = expense.getCategory().substring(0, 1).toUpperCase();
        binding.tvDetailCategoryIcon.setText(initial);
        int color = ContextCompat.getColor(requireContext(),
                Category.fromLabel(expense.getCategory()).getColorRes());
        binding.tvDetailCategoryIcon.getBackground().setTint(color);

        binding.tvDetailName.setText(expense.getName());
        binding.tvDetailCategory.setText(expense.getCategory());
        binding.tvDetailAmount.setText(expense.getFormattedAmount());
        binding.tvDetailDatetime.setText(expense.getFormattedDateTime());
        binding.tvDetailCurrency.setText(expense.getCurrency());

        // Mostra la nota solo se presente
        if (expense.getNote() != null && !expense.getNote().isEmpty()) {
            binding.layoutNote.setVisibility(View.VISIBLE);
            binding.tvDetailNote.setText(expense.getNote());
        }
    }

    private void setupButtons() {
        binding.btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        binding.btnEdit.setOnClickListener(v -> {
            dismiss();
            // Apre il form passando la spesa da modificare
            AddExpenseFormBottomSheet form =
                    AddExpenseFormBottomSheet.newInstance(expense);
            form.show(getParentFragmentManager(), "EditExpense");
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Elimina spesa")
                .setMessage("Sei sicuro di voler eliminare \"" + expense.getName() + "\"?")
                .setPositiveButton("Elimina", (dialog, which) -> {
                    viewModel.removeExpense(expense);
                    dismiss();
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}