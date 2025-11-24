package com.example.bibliaapp.view.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.CarritoSingleton;
import com.example.bibliaapp.model.Producto;

import java.io.File;
import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private final Context context;
    private List<Producto> productos;
    private final boolean esVisitante;
    private final CarritoSingleton carrito;

    public ProductoAdapter(Context context, List<Producto> productos, boolean esVisitante) {
        this.context = context;
        this.productos = productos;
        this.esVisitante = esVisitante;
        this.carrito = CarritoSingleton.getInstance();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto p = productos.get(position);

        holder.tvNombre.setText(p.getNombre());
        holder.tvPrecio.setText(String.format("S/ %.2f", p.getPrecio()));

        // --- LÓGICA DE IMAGEN HÍBRIDA ---
        String imagenStr = p.getImagen();
        boolean imagenCargada = false;

        if (imagenStr != null && !imagenStr.isEmpty()) {
            // 1. Intentar cargar como ruta de archivo (productos nuevos)
            File imgFile = new File(imagenStr);
            if (imgFile.exists()) {
                holder.ivProducto.setImageURI(Uri.fromFile(imgFile));
                imagenCargada = true;
            } else {
                // 2. Intentar cargar como recurso drawable (productos iniciales)
                int resId = context.getResources().getIdentifier(imagenStr, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.ivProducto.setImageResource(resId);
                    imagenCargada = true;
                }
            }
        }

        if (!imagenCargada) {
            holder.ivProducto.setImageResource(R.drawable.placeholder);
        }
        // -----------------------------------------------------

        // Lógica de Visitante y Stock
        if (esVisitante) {
            holder.btnAddCart.setVisibility(View.GONE);
        } else {
            holder.btnAddCart.setVisibility(View.VISIBLE);

            // Control visual del stock
            if (p.getStock() <= 0) {
                holder.btnAddCart.setEnabled(false);
                holder.btnAddCart.setText("Sin Stock");
            } else {
                holder.btnAddCart.setEnabled(true);
                holder.btnAddCart.setText("Añadir");
                // Listener que llama al método de validación
                holder.btnAddCart.setOnClickListener(v -> agregarProductoAlCarrito(p));
            }
        }
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public void updateList(List<Producto> nuevaLista) {
        this.productos = nuevaLista;
        notifyDataSetChanged();
    }

    // --- LÓGICA DE STOCK Y ADICIÓN AL CARRITO (CORREGIDA) ---
    private void agregarProductoAlCarrito(Producto producto) {
        int id = producto.getId();
        int stockDisponible = producto.getStock();

        // Obtiene la cantidad que ya existe en el carrito
        int cantidadEnCarrito = carrito.getCantidadProducto(id);

        // Validación: Solo se permite añadir si la cantidad actual + 1 es menor o igual al stock
        if (cantidadEnCarrito + 1 <= stockDisponible) {
            // Llamamos al método simplificado del Singleton
            boolean exito = carrito.agregarProducto(producto);
            if (exito) {
                Toast.makeText(context, "Añadido: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
            } else {
                // Este caso se daría solo si hay un error lógico inesperado
                Toast.makeText(context, "No se pudo añadir al carrito.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Bloqueo estricto y mensaje de advertencia al usuario
            Toast.makeText(context, "¡Stock insuficiente! Ya tienes el máximo (" + stockDisponible + ") en tu carrito.", Toast.LENGTH_LONG).show();
        }
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvPrecio;
        final ImageView ivProducto;
        final Button btnAddCart;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            ivProducto = itemView.findViewById(R.id.ivProducto);
            btnAddCart = itemView.findViewById(R.id.btnAddCart);
        }
    }
}