package com.example.bibliaapp.view;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Pedido;
import com.example.bibliaapp.model.PedidoSingleton;

// ******************************************************
// ** IMPORTACIONES CLAVE PARA EL MANEJO Y FORMATO DE FECHAS **
// ******************************************************
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
// ******************************************************

public class BoletaActivity extends AppCompatActivity {

    private static final String TAG = "BoletaActivity";
    private Button btnEntregaEnTienda, btnEntregaDelivery;
    private DBHelper dbHelper;
    private TextView tvFechaPedido;

    // Constante para el formato de fecha que viene de la DB (Ejemplo: YYYY-MM-DD HH:MM:SS)
    private static final String DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // Constante para el formato de fecha legible que quieres mostrar (Ejemplo: 24 de Noviembre de 2025)
    private static final String DISPLAY_DATE_FORMAT = "dd 'de' MMMM 'de' yyyy";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boleta);

        dbHelper = new DBHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbarBoleta);
        toolbar.setTitle("Boleta");
        toolbar.setBackgroundColor(0xFFFDD835);
        toolbar.setTitleTextColor(0xFF000000);

        btnEntregaEnTienda = findViewById(R.id.btnEntregaEnTienda);
        btnEntregaDelivery = findViewById(R.id.btnEntregaDelivery);
        tvFechaPedido = findViewById(R.id.tvFechaPedido);

        int idPedidoCodigo = getIntent().getIntExtra("idPedido", -1);
        final Pedido pedido = PedidoSingleton.getInstance().getPedidoById(idPedidoCodigo);

        TextView tvBoleta = findViewById(R.id.tvBoleta);
        StringBuilder sb = new StringBuilder();

        if (pedido != null) {

            // --- 1. CABECERA (CON CONSULTA DE FECHA) ---
            sb.append("Boleta #").append(pedido.getIdPedido()).append("\n");

            // 🛑 Lógica para obtener, formatear y mostrar la fecha
            long idPedidoDb = getDbIdFromCodigo(idPedidoCodigo);
            String fechaPedidoTexto = "Fecha no disponible";

            if (idPedidoDb != -1) {
                Cursor cursorInfo = null;
                String dbDateString = null;
                try {
                    cursorInfo = dbHelper.getPedidoInfoById(idPedidoDb);
                    if (cursorInfo != null && cursorInfo.moveToFirst()) {
                        // Obtenemos la fecha tal como está en la DB
                        dbDateString = cursorInfo.getString(cursorInfo.getColumnIndexOrThrow("fecha"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error obteniendo fecha del pedido: " + e.getMessage());
                } finally {
                    if (cursorInfo != null) cursorInfo.close();
                }

                // 🛑 CORRECCIÓN CLAVE: Formatear la fecha obtenida de la DB
                if (dbDateString != null && !dbDateString.isEmpty()) {
                    try {
                        // 1. Definir el formato de entrada (el que está en la DB)
                        SimpleDateFormat dbFormat = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
                        // 2. Parsear la cadena a un objeto Date
                        Date dateObject = dbFormat.parse(dbDateString);

                        // 3. Definir el formato de salida (día, mes, año en español)
                        // Usamos Locale("es", "ES") para asegurar los nombres de los meses en español.
                        SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, new Locale("es", "ES"));

                        // 4. Formatear el objeto Date para mostrarlo
                        fechaPedidoTexto = displayFormat.format(dateObject);

                    } catch (Exception e) {
                        Log.e(TAG, "Error al formatear la fecha: " + e.getMessage() + ". Fecha DB: " + dbDateString);
                        fechaPedidoTexto = "Error al formatear"; // En caso de que el formato de DB cambie.
                    }
                }
            }

            // Muestra la fecha en el TextView dedicado
            tvFechaPedido.setText("Fecha de Compra: " + fechaPedidoTexto);

            // Continúa con la Boleta
            // ... (resto del código de detalle y pie de página)
            sb.append("Nombre: ").append(pedido.getNombreCliente()).append("\n");
            sb.append("Teléfono: ").append(pedido.getTelefono()).append("\n");
            sb.append("Dirección: ").append(pedido.getDireccion()).append("\n\n");
            sb.append("Productos:\n");

            // --- 2. DETALLE (CARGA REAL DE NOMBRES DESDE DB) ---
            Cursor cursorDetalle = null;
            // ... (el resto de la lógica de cursorDetalle permanece igual)

            if (idPedidoDb != -1) {
                try {
                    // 🛑 USAMOS EL NUEVO MÉTODO CON JOIN PARA OBTENER EL NOMBRE
                    cursorDetalle = dbHelper.getDetallePedidoConNombre(idPedidoDb);

                    if (cursorDetalle != null && cursorDetalle.moveToFirst()) {
                        do {
                            // Obtenemos los campos del JOIN (incluyendo el nombre del producto)
                            String nombre = cursorDetalle.getString(cursorDetalle.getColumnIndexOrThrow("nombre"));
                            int cantidad = cursorDetalle.getInt(cursorDetalle.getColumnIndexOrThrow("cantidad"));
                            double subtotal = cursorDetalle.getDouble(cursorDetalle.getColumnIndexOrThrow("subtotal"));

                            sb.append(nombre)
                                    .append(" x").append(cantidad)
                                    .append(" = S/").append(String.format("%.2f", subtotal))
                                    .append("\n");
                        } while (cursorDetalle.moveToNext());
                    } else {
                        sb.append("Error: Detalles del pedido no encontrados.\n");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar detalle del pedido con nombre: " + e.getMessage());
                    sb.append("Error al cargar detalles de la base de datos.\n");
                } finally {
                    if (cursorDetalle != null) cursorDetalle.close();
                }
            } else {
                sb.append("Error: ID interno del pedido no encontrado. Verifique Checkout.\n");
            }

            // --- 3. PIE DE PÁGINA ---
            sb.append("\nTotal Pagado: S/").append(String.format("%.2f", pedido.getTotal())).append("\n");
            sb.append("Método de Pago: ").append(pedido.getTipoEntrega() != null ? pedido.getTipoEntrega() : "N/A").append("\n");


            // 🛑 LÓGICA DE BOTONES (Actualiza el estado del pedido en la DB)
            btnEntregaEnTienda.setOnClickListener(v -> {
                // Actualiza el estado en la base de datos
                dbHelper.updateEstadoPedido(String.valueOf(pedido.getIdPedido()), "Preparando (Recojo)");
                // Navega a PedidosActivity
                startActivity(new Intent(BoletaActivity.this, PedidosActivity.class));
                finish();
            });

            btnEntregaDelivery.setOnClickListener(v -> {
                // Actualiza el estado en la base de datos
                dbHelper.updateEstadoPedido(String.valueOf(pedido.getIdPedido()), "Preparando (Delivery)");
                // Navega a PedidosActivity
                startActivity(new Intent(BoletaActivity.this, PedidosActivity.class));
                finish();
            });

        } else {
            // Manejo del error si el pedido no se encuentra
            sb.append("Error: No se encontró el pedido con código: ").append(idPedidoCodigo).append("\n");
            btnEntregaEnTienda.setVisibility(View.GONE);
            btnEntregaDelivery.setVisibility(View.GONE);
            Toast.makeText(this, "ERROR CRÍTICO: No se pudo cargar la boleta.", Toast.LENGTH_LONG).show();
        }

        tvBoleta.setText(sb.toString());
    }

    /**
     * Helper para encontrar el ID interno de la tabla 'pedidos' usando el 'codigo' (ID de 6 dígitos).
     * @param codigo El ID de 6 dígitos del pedido.
     * @return El ID interno (INTEGER) de la tabla, o -1 si no se encuentra.
     */
    private long getDbIdFromCodigo(int codigo) {
        Cursor cursor = null;
        long idInterno = -1;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // Busca el ID interno de la tabla 'pedidos' usando el campo 'codigo'
            cursor = db.rawQuery("SELECT id_pedido FROM " + DBHelper.TABLE_PEDIDOS + " WHERE codigo = ?", new String[]{String.valueOf(codigo)});
            if (cursor != null && cursor.moveToFirst()) {
                idInterno = cursor.getLong(cursor.getColumnIndexOrThrow("id_pedido"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error buscando ID interno por código: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return idInterno;
    }
}