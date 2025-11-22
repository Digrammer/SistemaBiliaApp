package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.view.adapter.CarritoAdapter;
import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.CarritoSingleton;
import com.example.bibliaapp.model.Pedido;
import com.example.bibliaapp.model.PedidoSingleton;
// Nota: Para la actualización de stock final, necesitaremos importar DBHelper
// import com.example.bibliaapp.model.DBHelper;

import java.util.List;

public class CarritoActivity extends AppCompatActivity {

    private RecyclerView rvCarrito;
    private TextView tvTotal;
    private Button btnPagar;
    private CarritoAdapter adapter;
    // Nota: Si CarritoActivity requiere DBHelper, debería declararse aquí:
    // private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        // Inicializar DBHelper si fuera necesario
        // dbHelper = new DBHelper(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarCarrito);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mi Carrito");
            // Asumiendo que 0xFF000000 es Negro (es un color ARGB)
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
            if (carrito.isEmpty()) {
                Toast.makeText(CarritoActivity.this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            } else {
                // La lógica del pedido se maneja mejor en Checkout o en la confirmación final.
                // Este pedido temporal se puede omitir si CheckoutActivity maneja la persistencia.

                // Abrir CheckoutActivity (Aquí es donde se DEBERÍA CONFIRMAR LA COMPRA Y ACTUALIZAR STOCK)
                Intent intent = new Intent(CarritoActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
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