package com.example.bibliaapp.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bibliaapp.R;

public class DetalleProductoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Puedes crear un layout específico (activity_detalle_producto.xml)
        // Por ahora usamos uno existente para evitar errores
        setContentView(R.layout.activity_main);

        int idProducto = getIntent().getIntExtra("id_producto", -1);

        if (idProducto != -1) {
            Toast.makeText(this, "Mostrando detalle del producto ID: " + idProducto, Toast.LENGTH_LONG).show();
            // Lógica para cargar detalles del producto aquí
        } else {
            Toast.makeText(this, "Error: Producto no especificado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}