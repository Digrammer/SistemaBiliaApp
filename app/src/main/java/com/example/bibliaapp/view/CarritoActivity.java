package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.view.adapter.CarritoAdapter;
import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.CarritoSingleton;
// 🛑 CORRECCIÓN DE IMPORTACIÓN: Ahora usa "SharedPreferencesManager" (con 's' en Preferences)
import com.example.bibliaapp.model.SharedPreferencesManager;

import java.util.List;

public class CarritoActivity extends AppCompatActivity {

    private RecyclerView rvCarrito;
    private TextView tvTotal;
    private Button btnPagar;
    private CarritoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarCarrito);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mi Carrito");
            toolbar.setTitleTextColor(0xFF000000);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvCarrito = findViewById(R.id.rvCarrito);
        tvTotal = findViewById(R.id.tvTotal);
        btnPagar = findViewById(R.id.btnPagar);

        // Configurar RecyclerView
        rvCarrito.setLayoutManager(new LinearLayoutManager(this));
        List<CarritoItem> carrito = CarritoSingleton.getInstance().getCarrito();

        adapter = new CarritoAdapter(this, carrito, new CarritoAdapter.OnCarritoListener() {
            @Override
            public void onCantidadCambiada() {
                actualizarTotal();
            }
        });

        rvCarrito.setAdapter(adapter);

        actualizarTotal();

        // Botón Realizar Pago
        btnPagar.setOnClickListener(v -> {
            // 1. Verificar si está vacío
            if (carrito.isEmpty()) {
                Toast.makeText(CarritoActivity.this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. CORRECCIÓN DE VALIDACIÓN DE ROL: VISITANTE NO PUEDE COMPRAR

            // Usamos el nombre de la clase corregido: SharedPreferencesManager
            SharedPreferencesManager sessionManager = SharedPreferencesManager.getInstance(this);
            String rol = sessionManager.getUserRol(); // El rol por defecto es "visitante" si no hay sesión.

            if ("visitante".equalsIgnoreCase(rol)) {
                // Mostrar alerta y bloquear
                new AlertDialog.Builder(this)
                        .setTitle("Acceso Restringido")
                        .setMessage("Los visitantes solo pueden ver el catálogo. Para realizar una compra, debes iniciar sesión o registrarte.")
                        .setPositiveButton("Iniciar Sesión", (dialog, which) -> {
                            // Ir al Login y borrar pila para no volver atrás
                            Intent intent = new Intent(CarritoActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return; // Detener flujo aquí
            }

            // 3. Si es un usuario válido (Admin, Cliente, Vendedor), ir al Checkout
            Intent intent = new Intent(CarritoActivity.this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    // Al regresar del Checkout (o al reanudar), si el carrito se vació, actualiza la UI
    @Override
    protected void onResume() {
        super.onResume();
        actualizarTotal();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void actualizarTotal() {
        double total = 0;
        for (CarritoItem item : CarritoSingleton.getInstance().getCarrito()) {
            total += item.getSubtotal();
        }
        tvTotal.setText(String.format("Total: S/ %.2f", total));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}