package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.CarritoSingleton;
import com.example.bibliaapp.model.Pedido;
import com.example.bibliaapp.model.PedidoSingleton;
import com.example.bibliaapp.model.DBHelper; // <-- IMPORTANTE: Añadir la importación de DBHelper

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etNombre, etTelefono, etDireccion;
    private RadioGroup rgPago;
    private RadioButton rbYape, rbPlin;
    private ImageView ivQrYape, ivQrPlin;
    private TextView tvDatosYape, tvDatosPlin;
    private Button btnRealizarCompra;
    private Toolbar toolbar;

    private DBHelper dbHelper; // <-- Declaración de DBHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DBHelper(this); // <-- Inicialización de DBHelper

        // Configuración del Toolbar
        toolbar = findViewById(R.id.toolbarCheckout);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Checkout");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_flecha_atras_negra);
        }

        // Uso de ContextCompat.getColor() o color estático si el recurso falla
        // toolbar.setBackgroundColor(getResources().getColor(R.color.yourPrimaryColor));
        toolbar.setTitleTextColor(0xFF000000);

        // Vinculación de vistas (código omitido por brevedad, se mantiene igual)
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etDireccion = findViewById(R.id.etDireccion);
        rgPago = findViewById(R.id.rgPago);
        rbYape = findViewById(R.id.rbYape);
        rbPlin = findViewById(R.id.rbPlin);
        ivQrYape = findViewById(R.id.ivQrYape);
        ivQrPlin = findViewById(R.id.ivQrPlin);
        tvDatosYape = findViewById(R.id.tvDatosYape);
        tvDatosPlin = findViewById(R.id.tvDatosPlin);
        btnRealizarCompra = findViewById(R.id.btnRealizarCompra);

        // Filtros (código omitido por brevedad, se mantiene igual)
        etNombre.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        }});
        etTelefono.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});

        // Estado inicial de pagos y Lógica de selección (código omitido por brevedad, se mantiene igual)
        ivQrYape.setVisibility(View.GONE);
        ivQrPlin.setVisibility(View.GONE);
        tvDatosYape.setVisibility(View.GONE);
        tvDatosPlin.setVisibility(View.GONE);

        rgPago.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbYape) {
                ivQrYape.setVisibility(View.VISIBLE);
                tvDatosYape.setVisibility(View.VISIBLE);
                ivQrPlin.setVisibility(View.GONE);
                tvDatosPlin.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbPlin) {
                ivQrPlin.setVisibility(View.VISIBLE);
                tvDatosPlin.setVisibility(View.VISIBLE);
                ivQrYape.setVisibility(View.GONE);
                tvDatosYape.setVisibility(View.GONE);
            }
        });

        // Lógica del botón comprar
        btnRealizarCompra.setOnClickListener(v -> {
            if (validarYGuardar()) {

                List<CarritoItem> productosCopia = new ArrayList<>(CarritoSingleton.getInstance().getCarrito());

                String metodoPago = (rgPago.getCheckedRadioButtonId() == rbYape.getId()) ? "Yape" : "Plin";

                Random random = new Random();
                int idPedido = 100000 + random.nextInt(900000);

                Pedido pedido = new Pedido(
                        idPedido,
                        etNombre.getText().toString().trim(),
                        etTelefono.getText().toString().trim(),
                        etDireccion.getText().toString().trim(),
                        productosCopia
                );

                pedido.setTipoEntrega(metodoPago);

                // 🔥 CORRECCIÓN 2B: Actualizar el stock antes de guardar el pedido y limpiar
                if (!productosCopia.isEmpty()) {
                    for (CarritoItem item : productosCopia) {
                        int idProducto = item.getProducto().getId();
                        int cantidadComprada = item.getCantidad();

                        // Restamos la cantidad comprada del stock disponible en la BD
                        // Usamos un valor negativo para indicar resta, o un método específico en DBHelper
                        boolean exito = dbHelper.actualizarStockPorCompra(idProducto, cantidadComprada);

                        if (!exito) {
                            // En un caso real, esto debería ser un Rollback de la compra
                            // Pero para esta implementación, solo mostramos una advertencia.
                            Toast.makeText(CheckoutActivity.this, "Advertencia: Error al actualizar stock para " + item.getProducto().getNombre(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                PedidoSingleton.getInstance().agregarPedido(pedido);

                Intent intent = new Intent(CheckoutActivity.this, BoletaActivity.class);
                intent.putExtra("idPedido", idPedido);
                startActivity(intent);

                // Limpiamos el carrito original solo después de la actualización de stock y el pase a Boleta
                CarritoSingleton.getInstance().limpiarCarrito();
                finish();
            }
        });
    }

    // ... validarYGuardar() y onSupportNavigateUp() se mantienen iguales

    private boolean validarYGuardar() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        int metodoId = rgPago.getCheckedRadioButtonId();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Nombre inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!telefono.matches("\\d{9}")) {
            Toast.makeText(this, "Teléfono inválido (debe ser 9 dígitos)", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (direccion.isEmpty()) {
            Toast.makeText(this, "Dirección inválida", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (metodoId == -1) {
            Toast.makeText(this, "Seleccione método de pago", Toast.LENGTH_SHORT).show();
            return false;
        }

        Toast.makeText(this, "Compra realizada con éxito", Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}