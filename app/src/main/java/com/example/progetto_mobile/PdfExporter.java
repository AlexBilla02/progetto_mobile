package com.example.progetto_mobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import androidx.core.content.FileProvider;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.progetto_mobile.data.Expense;

public class PdfExporter {

    private static final int PAGE_WIDTH  = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN      = 40;
    private static final int LINE_HEIGHT = 22;

    // genera pdf e restituisce uri
    public static Uri export(Context context, List<Expense> expenses,
                             String periodLabel) throws IOException {

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();


        Paint paintTitle   = makePaint(20, true,  Color.parseColor("#1976D2"));
        Paint paintHeader  = makePaint(11, true,  Color.parseColor("#424242"));
        Paint paintBody    = makePaint(10, false, Color.parseColor("#212121"));
        Paint paintSub     = makePaint(9,  false, Color.parseColor("#9E9E9E"));
        Paint paintLine    = new Paint();
        paintLine.setColor(Color.parseColor("#EEEEEE"));
        paintLine.setStrokeWidth(1);

        int y = MARGIN + 10;

        // Titolo
        canvas.drawText("Report spese", MARGIN, y, paintTitle);
        y += 28;

        // Periodo e data generazione
        canvas.drawText("Periodo: " + periodLabel, MARGIN, y, paintSub);
        y += 16;
        String generated = "Generato il " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText(generated, MARGIN, y, paintSub);
        y += 20;

        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paintLine);
        y += 16;

        // Intestazioni colonne
        canvas.drawText("Data e ora",  MARGIN,       y, paintHeader);
        canvas.drawText("Nome",        MARGIN + 110, y, paintHeader);
        canvas.drawText("Categoria",   MARGIN + 270, y, paintHeader);
        canvas.drawText("Importo",     MARGIN + 390, y, paintHeader);
        y += 6;
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paintLine);
        y += 14;

        // Righe spese
        double total = 0;
        for (Expense expense : expenses) {
            // Se la pagina è piena, ne crea una nuova
            if (y > PAGE_HEIGHT - 60) {
                document.finishPage(page);
                PdfDocument.PageInfo nextPageInfo =
                        new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT,
                                document.getPages().size() + 1).create();
                page   = document.startPage(nextPageInfo);
                canvas = page.getCanvas();
                y      = MARGIN;
            }

            canvas.drawText(expense.getFormattedDateTime(), MARGIN,       y, paintBody);
            canvas.drawText(truncate(expense.getName(), 20), MARGIN + 110, y, paintBody);
            canvas.drawText(expense.getCategory(),           MARGIN + 270, y, paintBody);
            canvas.drawText(expense.getFormattedAmount(),    MARGIN + 390, y, paintBody);
            total += expense.getAmount();
            y += LINE_HEIGHT;
        }

        y += 4;
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paintLine);
        y += 16;
        String totalStr = String.format(Locale.getDefault(), "Totale: € %.2f", total);
        canvas.drawText(totalStr, PAGE_WIDTH - MARGIN - 100, y, paintHeader);

        document.finishPage(page);

        // Salva il file
        File dir = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), "");
        if (!dir.exists()) dir.mkdirs();

        String filename = "MyWallet_" +
                new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                        .format(new Date()) + ".pdf";
        File file = new File(dir, filename);

        FileOutputStream fos = new FileOutputStream(file);
        document.writeTo(fos);
        document.close();
        fos.close();

        return FileProvider.getUriForFile(
                context,
                "com.example.progetto_mobile.fileprovider",
                file
        );
    }

    // Apre il PDF con un'app esterna (lettore PDF)
    public static void share(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Apri con"));
    }

    private static Paint makePaint(int size, boolean bold, int color) {
        Paint p = new Paint();
        p.setTextSize(size);
        p.setColor(color);
        if (bold) p.setFakeBoldText(true);
        return p;
    }

    private static String truncate(String text, int maxChars) {
        if (text == null) return "";
        return text.length() > maxChars ? text.substring(0, maxChars - 1) + "…" : text;
    }
}