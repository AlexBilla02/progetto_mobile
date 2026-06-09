package com.example.progetto_mobile.ml;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class OcrProcessor {

    public interface OcrCallback {
        void onSuccess(String extractedText);
        void onFailure(String errorMessage);
    }

    private final TextRecognizer recognizer;
    private final Context context;

    public OcrProcessor(Context context) {
        this.context = context;
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void processImage(Uri imageUri, OcrCallback callback) {
        InputImage image;
        try {
            image = InputImage.fromFilePath(context, imageUri);
        } catch (IOException e) {
            callback.onFailure("Impossibile leggere l'immagine: " + e.getMessage());
            return;
        }

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    callback.onSuccess(extractSortedText(visionText));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("OCR fallito: " + e.getMessage())
                );
    }

    private String extractSortedText(Text visionText) {
        List<Text.TextBlock> blocks = new ArrayList<>(visionText.getTextBlocks());


        blocks.sort((a, b) -> {
            if (a.getBoundingBox() == null || b.getBoundingBox() == null) return 0;
            return Integer.compare(a.getBoundingBox().top, b.getBoundingBox().top);
        });

        StringBuilder sb = new StringBuilder();
        for (Text.TextBlock block : blocks) {
            sb.append(block.getText()).append("\n");
        }
        return sb.toString().trim();
    }

    private String extractFullText(Text visionText) {
        StringBuilder sb = new StringBuilder();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            sb.append(block.getText()).append("\n");
        }
        return sb.toString().trim();
    }

    public void shutdown() {
        recognizer.close();
    }
}