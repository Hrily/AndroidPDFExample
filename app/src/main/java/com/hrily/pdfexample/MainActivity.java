package com.hrily.pdfexample;

import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    Runnable pdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdf = new Runnable() {
            @Override
            public void run() {

                // Create a shiny new (but blank) PDF document in memory
                // We want it to optionally be printable, so add PrintAttributes
                // and use a PrintedPdfDocument. Simpler: new PdfDocument().
                PrintAttributes printAttrs = new PrintAttributes.Builder().
                        setColorMode(PrintAttributes.COLOR_MODE_COLOR).
                        setMediaSize(PrintAttributes.MediaSize.NA_LETTER).
                        setResolution(new PrintAttributes.Resolution("zooey", PRINT_SERVICE, 300, 300)).
                        setMinMargins(PrintAttributes.Margins.NO_MARGINS).
                        build();
                PdfDocument document = new PrintedPdfDocument(MainActivity.this, printAttrs);

                // crate a page description
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 300, 1).create();

                // create a new page from the PageInfo
                PdfDocument.Page page = document.startPage(pageInfo);

                // repaint the user's text into the page
                View content = findViewById(R.id.hello);
                content.draw(page.getCanvas());

                // do final processing of the page
                document.finishPage(page);

                // Here you could add more pages in a longer doc app, but you'd have
                // to handle page-breaking yourself in e.g., write your own word processor...

                // Now write the PDF document to a file; it actually needs to be a file
                // since the Share mechanism can't accept a byte[]. though it can
                // accept a String/CharSequence. Meh.
                FileOutputStream os;
                try {
                    File pdfDirPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "pdfs");
                    pdfDirPath.mkdirs();
                    File file = new File(pdfDirPath, "pdfsend.pdf");
                    os = new FileOutputStream(file);
                    document.writeTo(os);
                    document.close();
                    os.close();
                    Uri contentUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        contentUri = FileProvider.getUriForFile(MainActivity.this, "com.hrily.pdfexample.provider", file);
                    else
                        contentUri = Uri.fromFile(file);

                    shareDocument(contentUri);
                } catch (Exception e) {
                    throw new RuntimeException("Error generating file", e);
                }
            }
        };

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(pdf).start();
            }
        });
    }

    private void shareDocument(Uri uri) {
        Intent mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("application/pdf");
        // Assuming it may go via eMail:
        mShareIntent.putExtra(Intent.EXTRA_SUBJECT, "Here is a PDF from PdfSend");
        // Attach the PDf as a Uri, since Android can't take it as bytes yet.
        mShareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(mShareIntent);
        return;
    }

}
