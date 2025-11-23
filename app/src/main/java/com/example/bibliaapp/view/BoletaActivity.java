package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Añadido para el manejo de errores

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.Pedido;
import com.example.bibliaapp.model.PedidoSingleton;

public class BoletaActivity extends AppCompatActivity {

    private Button btnEntregaEnTienda, btnEntregaDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boleta);

        Toolbar toolbar = findViewById(R.id.toolbarBoleta);
        toolbar.setTitle("Boleta");

        // Usando colores estáticos por seguridad
        toolbar.setBackgroundColor(0xFFFDD835); // Amarillo
        toolbar.setTitleTextColor(0xFF000000); // Negro


        btnEntregaEnTienda = findViewById(R.id.btnEntregaEnTienda);
        btnEntregaDelivery = findViewById(R.id.btnEntregaDelivery);

        int idPedido = getIntent().getIntExtra("idPedido", -1);
        final Pedido pedido = PedidoSingleton.getInstance().getPedidoById(idPedido);

        TextView tvBoleta = findViewById(R.id.tvBoleta);
        StringBuilder sb = new StringBuilder();

        if (pedido != null) {

            // Lógica de construcción de la boleta (que ya es correcta)
            sb.append("Boleta #").append(pedido.getIdPedido()).append("\n");
            sb.append("Nombre: ").append(pedido.getNombreCliente()).append("\n");
            sb.append("Teléfono: ").append(pedido.getTelefono()).append("\n");
            sb.append("Dirección: ").append(pedido.getDireccion()).append("\n\n");
            sb.append("Productos:\n");

// BLOQUE CORREGIDO: Añade una comprobación si el Producto existe
            for (CarritoItem item : pedido.getItems()) {
                // 🛑 CORRECCIÓN: Asegurar que item.getProducto() no sea null
                if (item.getProducto() != null) {
                    sb.append(item.getProducto().getNombre())
                            .append(" x").append(item.getCantidad())
                            .append(" = S/").append(String.format("%.2f", item.getSubtotal()))
                            .append("\n");
                } else {
                    // Manejo si el producto no se cargó correctamente (debería ser raro, pero protege contra el crash)
                    sb.append("Producto Desconocido")
                            .append(" x").append(item.getCantidad())
                            .append(" = S/").append(String.format("%.2f", item.getSubtotal()))
                            .append("\n");
                }
            }
            sb.append("\nTotal Pagado: S/").append(String.format("%.2f", pedido.getTotal())).append("\n");
            sb.append("Método de Pago: ").append(pedido.getTipoEntrega() != null ? pedido.getTipoEntrega() : "N/A").append("\n");


            // 🛑 SOLUCIÓN: Los Click Listeners deben estar dentro de este bloque
            btnEntregaEnTienda.setOnClickListener(v -> {
                // Aquí 'pedido' es seguro que NO es null
                pedido.setTipoEntrega("En tienda");
                startActivity(new Intent(BoletaActivity.this, PedidosActivity.class));
                finish();
            });

            btnEntregaDelivery.setOnClickListener(v -> {
                // Aquí 'pedido' es seguro que NO es null
                pedido.setTipoEntrega("Delivery");
                startActivity(new Intent(BoletaActivity.this, PedidosActivity.class));
                finish();
            });

        } else {
            // Manejo del error si el pedido no se encuentra
            sb.append("Error: No se encontró el pedido con ID: ").append(idPedido).append("\n");
            sb.append("Intente volver a la pantalla de inicio.");

            // Opcional: Desactivar los botones si no hay pedido para evitar confusión
            btnEntregaEnTienda.setEnabled(false);
            btnEntregaDelivery.setEnabled(false);

            Toast.makeText(this, "ERROR CRÍTICO: No se pudo cargar la boleta.", Toast.LENGTH_LONG).show();
        }

        tvBoleta.setText(sb.toString());
    }
}