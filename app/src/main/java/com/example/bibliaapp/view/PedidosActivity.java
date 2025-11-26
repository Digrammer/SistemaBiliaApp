package com.example.bibliaapp.view;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.view.adapter.PedidoAdapter;
import com.example.bibliaapp.controller.PedidoController;
import com.example.bibliaapp.model.Pedido;
import com.example.bibliaapp.model.SharedPreferencesManager;
import com.example.bibliaapp.model.Producto;
import com.example.bibliaapp.model.DetallePedido;
import com.example.bibliaapp.model.DBHelper;
// REQUIERE NativePdfGenerator.java para compilar
import com.example.bibliaapp.model.NativePdfGenerator;

import java.util.ArrayList;
import java.util.List;

public class PedidosActivity extends AppCompatActivity implements PedidoAdapter.OnPedidoActionListener {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private Toolbar toolbar;

    private SharedPreferencesManager sessionManager;
    private PedidoController pedidoController;
    private DBHelper dbHelper;
    private String userRol;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        toolbar = findViewById(R.id.toolbar_pedidos);
        recyclerView = findViewById(R.id.recycler_pedidos);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pedidos");
        }
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));

        // SOLUCIÓN PARA EL BOTÓN DE RETROCESO
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pedidoController = new PedidoController(this);
        sessionManager = SharedPreferencesManager.getInstance(this);
        dbHelper = new DBHelper(this);

        userId = sessionManager.getUserId();
        userRol = sessionManager.getUserRol();

        loadPedidos();
    }

    private void loadPedidos() {
        new Thread(() -> {
            List<Pedido> pedidos;

            if ("administrador".equals(userRol)) {
                pedidos = pedidoController.getPedidosByRol(-1, userRol);
                Log.d("PedidosActivity", "Cargando TODOS los pedidos para Admin.");
            } else if ("vendedor".equals(userRol) || "cliente".equals(userRol)) {
                pedidos = pedidoController.getPedidosByRol(userId, userRol);
                Log.d("PedidosActivity", String.format("Cargando pedidos para ID %d y rol %s.", userId, userRol));
            } else {
                pedidos = new java.util.ArrayList<>();
                Log.w("PedidosActivity", "Rol desconocido o visitante. Lista vacía.");
            }

            runOnUiThread(() -> {
                if (pedidos.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("No hay pedidos para mostrar en este momento.");
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmptyState.setVisibility(View.GONE);
                    PedidoAdapter adapter = new PedidoAdapter(this, pedidos, userRol, this);
                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }

    @Override
    public void onEstadoActualizado() {
        loadPedidos();
    }

    @Override
    public void onDescargarPdf(Pedido pedido) {
        Toast.makeText(this, "Preparando " + pedido.getTipoComprobante() + " PDF...", Toast.LENGTH_SHORT).show();
        new Thread(() -> generateAndSharePdf(pedido)).start();
    }


    private void generateAndSharePdf(Pedido pedido) {
        Log.i("PDF_GENERATOR", "Generando documento para Pedido #" + pedido.getIdPedido());

        long idPedidoDB = dbHelper.getDbIdFromCodigo(pedido.getIdPedido());

        if (idPedidoDB == -1) {
            runOnUiThread(() -> Toast.makeText(this, "Error: No se encontró el pedido en la base de datos.", Toast.LENGTH_LONG).show());
            return;
        }

        List<DetallePedido> detallesReales = loadDetallesFromDB(idPedidoDB);

        if (detallesReales.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Error: No se encontraron productos en el pedido.", Toast.LENGTH_LONG).show());
            return;
        }

        // Cargar datos de FACTURA (RUC y Razón Social) si aplica
        if ("Factura".equalsIgnoreCase(pedido.getTipoComprobante())) {
            Cursor cursorFactura = null;
            try {
                // Obtenemos los datos de la tabla FACTURAS (asumiendo que DBHelper tiene el método getFacturaByPedido)
                cursorFactura = dbHelper.getFacturaByPedido((int) idPedidoDB);
                if (cursorFactura != null && cursorFactura.moveToFirst()) {
                    String ruc = cursorFactura.getString(cursorFactura.getColumnIndexOrThrow("ruc"));
                    String razonSocial = cursorFactura.getString(cursorFactura.getColumnIndexOrThrow("razon_social"));

                    pedido.setRuc(ruc);
                    pedido.setRazonSocial(razonSocial);
                }
            } catch (Exception e) {
                Log.e("PDF_GENERATOR", "Error al cargar datos de Factura: " + e.getMessage());
            } finally {
                if (cursorFactura != null) cursorFactura.close();
            }
        }

        // Llamar al generador nativo con los datos REALES
        NativePdfGenerator.generateAndShareReceipt(this, pedido, detallesReales);
    }

    private List<DetallePedido> loadDetallesFromDB(long idPedidoDB) {
        List<DetallePedido> detalles = new ArrayList<>();
        Cursor cursor = null;
        try {
            // Usamos el método de DetallePedidoDao (delegado en DBHelper)
            cursor = dbHelper.getDetallePedidoConNombre(idPedidoDB);

            if (cursor != null && cursor.moveToFirst()) {
                int idProductoIndex = cursor.getColumnIndexOrThrow("id_producto");
                int nombreIndex = cursor.getColumnIndexOrThrow("nombre");
                int precioIndex = cursor.getColumnIndexOrThrow("precio");
                int cantidadIndex = cursor.getColumnIndexOrThrow("cantidad");

                do {
                    // Usamos el constructor vacío de Producto() que ya implementaste
                    Producto producto = new Producto();
                    producto.setId(cursor.getInt(idProductoIndex));
                    producto.setNombre(cursor.getString(nombreIndex));
                    producto.setPrecio(cursor.getDouble(precioIndex));

                    DetallePedido detalle = new DetallePedido(
                            producto,
                            cursor.getInt(cantidadIndex)
                    );
                    detalles.add(detalle);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PedidosActivity", "Error al cargar detalles del pedido: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return detalles;
    }
}