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

public class BoletaActivity extends AppCompatActivity {

    private static final String TAG = "BoletaActivity";
    private Button btnEntregaEnTienda, btnEntregaDelivery;
    private DBHelper dbHelper;

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

        // El ID que pasa el checkout es el CÓDIGO de 6 dígitos
        int idPedidoCodigo = getIntent().getIntExtra("idPedido", -1);
        final Pedido pedido = PedidoSingleton.getInstance().getPedidoById(idPedidoCodigo);

        TextView tvBoleta = findViewById(R.id.tvBoleta);
        StringBuilder sb = new StringBuilder();

        if (pedido != null) {
            // --- 1. CABECERA ---
            sb.append("Boleta #").append(pedido.getIdPedido()).append("\n");
            sb.append("Nombre: ").append(pedido.getNombreCliente()).append("\n");
            sb.append("Teléfono: ").append(pedido.getTelefono()).append("\n");
            sb.append("Dirección: ").append(pedido.getDireccion()).append("\n\n");
            sb.append("Productos:\n");

            // --- 2. DETALLE (CARGA REAL DE NOMBRES DESDE DB) ---
            long idPedidoDb = getDbIdFromCodigo(idPedidoCodigo); // Buscar el ID interno de la DB
            Cursor cursorDetalle = null;

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