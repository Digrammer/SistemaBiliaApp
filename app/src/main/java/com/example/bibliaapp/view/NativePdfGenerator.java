package com.example.bibliaapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.bibliaapp.model.DetallePedido; // Importamos el DetallePedido que acabas de crear
import com.example.bibliaapp.model.Pedido;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Clase para generar un recibo de pedido en formato PDF
 * utilizando las APIs nativas de Android (PdfDocument),
 * sin requerir librerías externas ni modificar build.gradle.
 */
public class NativePdfGenerator {

    private static final String TAG = "NativePdfGenerator";
    // ¡IMPORTANTE! Esta AUTHORITY debe coincidir con la que configures en AndroidManifest.xml
    private static final String AUTHORITY = "com.example.bibliaapp.fileprovider";
    private static final int PAGE_WIDTH = 595; // Ancho Aprox. de A4 en points
    private static final int PAGE_HEIGHT = 842; // Alto Aprox. de A4 en points
    private static final int MARGIN = 40;
    private static final int TEXT_SIZE = 10;
    private static final int HEADER_SIZE = 18;
    private static final int LINE_HEIGHT = 18;

    /**
     * Genera el recibo PDF y lo comparte mediante un Intent.
     * @param activity Contexto de la actividad.
     * @param pedido El objeto Pedido con la información principal.
     * @param detallesPedido La lista de DetallePedido (Productos + Cantidad).
     */
    public static void generateAndShareReceipt(Activity activity, Pedido pedido, List<DetallePedido> detallesPedido) {
        new Thread(() -> {
            File pdfFile = createPdf(activity, pedido, detallesPedido);
            activity.runOnUiThread(() -> {
                if (pdfFile != null) {
                    sharePdf(activity, pdfFile);
                } else {
                    Toast.makeText(activity, "Error al generar el PDF. Verifica permisos.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private static File createPdf(Context context, Pedido pedido, List<DetallePedido> detallesPedido) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int x = MARGIN;
        int y = MARGIN;
        int finalY = 0; // Para rastrear la posición vertical

        try {
            // --- CABECERA Y TÍTULO ---
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextSize(HEADER_SIZE);
            paint.setColor(Color.rgb(0, 102, 0)); // Verde oscuro
            String title = "BOLETA DE VENTA";
            float titleWidth = paint.measureText(title);
            // Centra el título
            canvas.drawText(title, (PAGE_WIDTH - titleWidth) / 2, y, paint);

            y += HEADER_SIZE + 10;
            // Dibuja una línea divisora
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint);
            y += 20;

            // --- DETALLES DEL PEDIDO ---
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(TEXT_SIZE);
            paint.setColor(Color.BLACK);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateStr = dateFormat.format(new Date());

            canvas.drawText("Pedido Nro: " + pedido.getIdPedido(), x, y, paint);
            y += LINE_HEIGHT;
            canvas.drawText("Fecha: " + dateStr, x, y, paint);
            y += LINE_HEIGHT;
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Estado: " + pedido.getEstado(), x, y, paint);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            y += LINE_HEIGHT * 2;


            // --- TABLA DE DETALLES ---
            int col1 = MARGIN + 10; // Cantidad
            int col2 = MARGIN + 60; // Descripción
            int col3 = PAGE_WIDTH - MARGIN - 120; // P. Unit.
            int col4 = PAGE_WIDTH - MARGIN - 20; // Total

            // Dibuja el fondo de la cabecera de la tabla
            paint.setColor(Color.rgb(200, 230, 200)); // Verde claro
            canvas.drawRect(MARGIN, y - TEXT_SIZE, PAGE_WIDTH - MARGIN, y + LINE_HEIGHT - TEXT_SIZE, paint);

            // Dibuja la cabecera con texto en negrita
            paint.setColor(Color.BLACK);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Cant.", col1, y, paint);
            canvas.drawText("Descripción", col2, y, paint);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("P. Unit. (S/)", col3, y, paint);
            canvas.drawText("Total (S/)", col4, y, paint);
            paint.setTextAlign(Paint.Align.LEFT); // Reset a LEFT para los detalles
            y += LINE_HEIGHT;

            // Línea separadora debajo de la cabecera
            paint.setColor(Color.BLACK);
            canvas.drawLine(MARGIN, y - TEXT_SIZE, PAGE_WIDTH - MARGIN, y - TEXT_SIZE, paint);

            double subTotal = 0;

            // Dibuja las líneas de detalle
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            for (DetallePedido detalle : detallesPedido) {
                double totalLinea = detalle.getTotalLinea();
                subTotal += totalLinea;

                // Cantidad (alineación LEFT para números)
                canvas.drawText(String.valueOf(detalle.getCantidadComprada()), col1, y, paint);

                // Descripción (alineación LEFT)
                canvas.drawText(detalle.getNombre(), col2, y, paint);

                // P. Unit. (alineación RIGHT)
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(String.format(Locale.getDefault(), "%.2f", detalle.getPrecioUnitario()), col3, y, paint);

                // Total (alineación RIGHT)
                canvas.drawText(String.format(Locale.getDefault(), "%.2f", totalLinea), col4, y, paint);
                paint.setTextAlign(Paint.Align.LEFT); // Reset a LEFT

                y += LINE_HEIGHT;
            }
            finalY = y; // Posición final de la tabla


            // --- SECCIÓN DE TOTALES ---
            y = finalY + 15;
            // Línea sobre totales (para separarla de los productos)
            canvas.drawLine(PAGE_WIDTH - MARGIN - 200, y - 5, PAGE_WIDTH - MARGIN, y - 5, paint);

            double igv = subTotal * 0.18;
            double totalFinal = subTotal + igv;
            int totalX = PAGE_WIDTH - MARGIN - 20; // Columna derecha de totales
            int labelX = PAGE_WIDTH - MARGIN - 130; // Columna izquierda de totales

            // Subtotal
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Subtotal:", labelX, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "S/ %.2f", subTotal), totalX, y, paint);
            y += LINE_HEIGHT;

            // IGV (18%)
            canvas.drawText("IGV (18%):", labelX, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "S/ %.2f", igv), totalX, y, paint);
            y += LINE_HEIGHT;

            // Total Final (Negrita y color)
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setColor(Color.rgb(0, 102, 0)); // Verde oscuro
            canvas.drawText("TOTAL A PAGAR:", labelX, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "S/ %.2f", totalFinal), totalX, y, paint);

            // Restaurar paint para el pie de página
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setColor(Color.BLACK);
            y += LINE_HEIGHT * 2;

            // --- PIE DE PÁGINA ---
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Gracias por su compra en BibliaApp.", PAGE_WIDTH / 2, y, paint);

            document.finishPage(page);

            // 2. Guardar el archivo
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Boleta_Pedido_" + pedido.getIdPedido() + "_" + timeStamp + ".pdf";

            // Guardar en el directorio de Documentos de la App
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) return null;
            File file = new File(dir, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                document.writeTo(fos);
                Log.i(TAG, "PDF nativo creado exitosamente en: " + file.getAbsolutePath());
                return file;
            }

        } catch (IOException e) {
            Log.e(TAG, "Error durante la generación del PDF nativo: " + e.getMessage(), e);
            return null;
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Inicia un Intent para compartir el archivo PDF.
     */
    private static void sharePdf(Context context, File pdfFile) {
        try {
            // Usa el FileProvider para obtener un URI seguro
            Uri pdfUri = FileProvider.getUriForFile(context, AUTHORITY, pdfFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Compartir Boleta de Pedido"));

        } catch (Exception e) {
            Log.e(TAG, "Error al compartir PDF: " + e.getMessage(), e);
            Toast.makeText(context, "No se pudo compartir el archivo. Revise el FileProvider y permisos.", Toast.LENGTH_LONG).show();
        }
    }
}