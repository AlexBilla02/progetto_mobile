package com.example.progetto_mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.progetto_mobile.databinding.BottomSheetAddChoiceBinding;

public class AddExpenseBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddChoiceBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddChoiceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnManual.setOnClickListener(v -> {
            dismiss(); // chiude questo sheet
            // Apre il form
            new AddExpenseFormBottomSheet().show(
                    getParentFragmentManager(),
                    "AddExpenseForm"
            );
        });

        // btnCamera è disabilitato per ora, non serve listener
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}