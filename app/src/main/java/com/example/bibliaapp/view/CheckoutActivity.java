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
import com.example.bibliaapp.model.DBHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etNombre, etTelefono, etDireccion, etRuc, etRazonSocial;
    private RadioGroup rgPago, rgComprobante;
    private RadioButton rbYape, rbPlin, rbBoleta, rbFactura;
    private ImageView ivQrYape, ivQrPlin;
    private TextView tvDatosYape, tvDatosPlin;
    private Button btnRealizarCompra;
    private Toolbar toolbar;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DBHelper(this);

        toolbar = findViewById(R.id.toolbarCheckout);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Checkout");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_flecha_atras_negra);
        }

        toolbar.setTitleTextColor(0xFF000000);

        // Campos de Cliente
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etDireccion = findViewById(R.id.etDireccion);

        // Comprobantes (NUEVOS)
        rgComprobante = findViewById(R.id.rgComprobante);
        rbBoleta = findViewById(R.id.rbBoleta);
        rbFactura = findViewById(R.id.rbFactura);
        etRuc = findViewById(R.id.etRuc);
        etRazonSocial = findViewById(R.id.etRazonSocial);

        // Pagos
        rgPago = findViewById(R.id.rgPago);
        rbYape = findViewById(R.id.rbYape);
        rbPlin = findViewById(R.id.rbPlin);
        ivQrYape = findViewById(R.id.ivQrYape);
        ivQrPlin = findViewById(R.id.ivQrPlin);
        tvDatosYape = findViewById(R.id.tvDatosYape);
        tvDatosPlin = findViewById(R.id.tvDatosPlin);
        btnRealizarCompra = findViewById(R.id.btnRealizarCompra);


        // Filtros de Input (existentes)
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


        // Listener para Comprobante (NUEVO)
        rgComprobante.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbFactura) {
                // Mostrar campos de Factura
                etRuc.setVisibility(View.VISIBLE);
                etRazonSocial.setVisibility(View.VISIBLE);
            } else {
                // Ocultar campos de Factura (Boleta)
                etRuc.setVisibility(View.GONE);
                etRazonSocial.setVisibility(View.GONE);
                etRuc.setText(""); // Limpiar campos si cambia a Boleta
                etRazonSocial.setText("");
            }
        });

        // Listener para Pago (existente)
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

        btnRealizarCompra.setOnClickListener(v -> {
            if (validarYGuardar()) {

                List<CarritoItem> productosCopia = new ArrayList<>(CarritoSingleton.getInstance().getCarrito());

                if (productosCopia.isEmpty()) {
                    Toast.makeText(CheckoutActivity.this, "El carrito está vacío.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String nombreCliente = etNombre.getText().toString().trim();
                String telefonoCliente = etTelefono.getText().toString().trim();
                String direccionCliente = etDireccion.getText().toString().trim();
                String metodoPago = (rgPago.getCheckedRadioButtonId() == rbYape.getId()) ? "Yape" : "Plin";
                String estadoPedido = "Pendiente";

                // *** OBTENER TIPO DE COMPROBANTE ***
                String tipoComprobante = (rgComprobante.getCheckedRadioButtonId() == rbFactura.getId()) ? "Factura" : "Boleta";
                String ruc = etRuc.getText().toString().trim();
                String razonSocial = etRazonSocial.getText().toString().trim();
                // **********************************

                Random random = new Random();
                int idPedido = 100000 + random.nextInt(900000);

                Pedido pedido = new Pedido(
                        idPedido,
                        nombreCliente,
                        telefonoCliente,
                        direccionCliente,
                        productosCopia,
                        tipoComprobante
                );
                pedido.setTipoEntrega(metodoPago);
                pedido.setEstado(estadoPedido);

                long idPedidoGuardado = dbHelper.guardarPedidoCompleto(pedido, productosCopia);

                if (idPedidoGuardado > 0) {

                    // LÓGICA DE FACTURA/BOLETA
                    if (tipoComprobante.equals("Factura")) {
                        dbHelper.insertFactura(
                                (int) idPedidoGuardado,
                                pedido.getTotal(),
                                String.valueOf(idPedido), // Usamos el código del pedido como N° de Factura temporal
                                ruc,
                                razonSocial
                        );
                    } else {
                        dbHelper.insertBoleta(
                                (int) idPedidoGuardado,
                                pedido.getTotal(),
                                String.valueOf(idPedido) // Usamos el código del pedido como N° de Boleta temporal
                        );
                    }

                    dbHelper.guardarTelefonoDeCliente(telefonoCliente);
                    PedidoSingleton.getInstance().agregarPedido(pedido);

                    Toast.makeText(CheckoutActivity.this, "Compra N° " + pedido.getIdPedido() + " registrada con éxito.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(CheckoutActivity.this, BoletaActivity.class);
                    // IMPORTANTE: Debemos pasar el ID real de la base de datos para la consulta.
                    // Aquí asumimos que BoletaActivity también puede manejar Factura.
                    intent.putExtra("idPedidoDB", idPedidoGuardado);
                    intent.putExtra("tipoComprobante", tipoComprobante);
                    startActivity(intent);

                    CarritoSingleton.getInstance().limpiarCarrito();
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this, "ERROR: No se pudo registrar la compra. Falla en la base de datos o stock insuficiente.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Validación de todos los campos, incluyendo RUC/Razón Social si es Factura
    private boolean validarYGuardar() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        int metodoId = rgPago.getCheckedRadioButtonId();
        int comprobanteId = rgComprobante.getCheckedRadioButtonId();

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

        // NUEVA VALIDACIÓN PARA COMPROBANTE
        if (comprobanteId == R.id.rbFactura) {
            String ruc = etRuc.getText().toString().trim();
            String razonSocial = etRazonSocial.getText().toString().trim();

            if (!ruc.matches("\\d{11}")) {
                Toast.makeText(this, "RUC inválido (debe ser 11 dígitos)", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (razonSocial.isEmpty()) {
                Toast.makeText(this, "Razón Social es obligatoria para Factura", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }
}