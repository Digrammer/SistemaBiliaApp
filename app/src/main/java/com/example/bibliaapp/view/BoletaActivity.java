package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

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
        toolbar.setBackgroundColor(getResources().getColor(R.color.yourPrimaryColor));
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));

        btnEntregaEnTienda = findViewById(R.id.btnEntregaEnTienda);
        btnEntregaDelivery = findViewById(R.id.btnEntregaDelivery);

        int idPedido = getIntent().getIntExtra("idPedido", -1);
        final Pedido pedido = PedidoSingleton.getInstance().getPedidoById(idPedido);

        TextView tvBoleta = findViewById(R.id.tvBoleta);
        StringBuilder sb = new StringBuilder();
        sb.append("Boleta #").append(pedido.getId()).append("\n");
        sb.append("Nombre: ").append(pedido.getNombreCliente()).append("\n");
        sb.append("Teléfono: ").append(pedido.getTelefono()).append("\n");
        sb.append("Dirección: ").append(pedido.getDireccion()).append("\n\n");
        sb.append("Productos:\n");

        for (CarritoItem item : pedido.getProductos()) {
            sb.append(item.getNombre())
                    .append(" x").append(item.getCantidad())
                    .append(" = S/").append(String.format("%.2f", item.getSubtotal()))
                    .append("\n");
        }

        tvBoleta.setText(sb.toString());

        btnEntregaEnTienda.setOnClickListener(v -> {
            pedido.setTipoEntrega("En tienda");
            startActivity(new Intent(BoletaActivity.this, PedidosActivity.class));
            finish();
        });

        btnEntregaDelivery.setOnClickListener(v -> {
            pedido.setTipoEntrega("Delivery");
            startActivity(new Intent(BoletaActivity.this, PedidosActivity.class));
            finish();
        });
    }
}
