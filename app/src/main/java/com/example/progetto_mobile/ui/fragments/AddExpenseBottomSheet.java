package com.example.progetto_mobile.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.progetto_mobile.databinding.BottomSheetAddChoiceBinding;
import com.example.progetto_mobile.ml.OcrProcessor;
import com.example.progetto_mobile.ml.ReceiptParser;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddChoiceBinding binding;
    private Uri photoUri;
    private OcrProcessor ocrProcessor;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) openCamera();
                        else Toast.makeText(requireContext(),
                                "Permesso fotocamera negato", Toast.LENGTH_SHORT).show();
                    }
            );

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success && photoUri != null) {
                            processReceiptPhoto(photoUri);
                        }
                    }
            );

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
        ocrProcessor = new OcrProcessor(requireContext());

        binding.btnManual.setOnClickListener(v -> {
            dismiss();
            new AddExpenseFormBottomSheet().show(
                    getParentFragmentManager(), "AddExpenseForm");
        });

        binding.btnCamera.setEnabled(true);
        binding.btnCamera.setAlpha(1.0f);
        binding.btnCamera.setOnClickListener(v -> checkCameraPermission());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.progetto_mobile.fileprovider",
                    photoFile
            );
            cameraLauncher.launch(photoUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(),
                    "Errore apertura fotocamera", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        File storageDir = requireContext().getExternalFilesDir(null);
        return File.createTempFile("RECEIPT_" + timestamp, ".jpg", storageDir);
    }

    private void processReceiptPhoto(Uri imageUri) {
        binding.btnCamera.setEnabled(false);
        binding.btnCamera.setText("Analisi in corso...");

        ocrProcessor.processImage(imageUri, new OcrProcessor.OcrCallback() {
            @Override
            public void onSuccess(String extractedText) {
                android.util.Log.d("OCR_RESULT", "Testo estratto:\n" + extractedText);
                ReceiptParser.ParsedReceipt parsed = ReceiptParser.parse(extractedText);
                android.util.Log.d("OCR_PARSED", "Merchant: " + parsed.merchantName);
                android.util.Log.d("OCR_PARSED", "Amount: " + parsed.amount);
                android.util.Log.d("OCR_PARSED", "Date: " + parsed.date);

                requireActivity().runOnUiThread(() -> {
                    AddExpenseFormBottomSheet form =
                            AddExpenseFormBottomSheet.newInstanceFromReceipt(parsed);
                    form.show(getParentFragmentManager(), "AddExpenseForm");
                    dismiss();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                android.util.Log.e("OCR_ERROR", "Errore: " + errorMessage);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                            "OCR non riuscito, compila manualmente",
                            Toast.LENGTH_LONG).show();
                    dismiss();
                    new AddExpenseFormBottomSheet().show(
                            getParentFragmentManager(), "AddExpenseForm");
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ocrProcessor != null) ocrProcessor.shutdown();
        binding = null;
    }
}