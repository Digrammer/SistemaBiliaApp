package com.example.bibliaapp.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.CarritoSingleton;
import com.example.bibliaapp.model.Producto;
import com.example.bibliaapp.view.DetalleProductoActivity;

import java.util.List;

public class ProductoCatalogoAdapter extends RecyclerView.Adapter<ProductoCatalogoAdapter.ProductoViewHolder> {

    private final Context context;
    private List<Producto> listaProductos;
    private final CarritoSingleton carritoSingleton;

    public ProductoCatalogoAdapter(Context context, List<Producto> listaProductos) {
        this.context = context;
        this.listaProductos = listaProductos;
        this.carritoSingleton = CarritoSingleton.getInstance();
    }

    public void updateList(List<Producto> newList) {
        this.listaProductos = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 🚨 CORRECCIÓN 1: Asegura inflar el layout correcto: item_producto_catalogo
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto_catalogo, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        // Línea 49
        holder.tvNombreProd.setText(producto.getNombre());

        // Línea 51
        holder.tvPrecioProd.setText("S/ " + String.format("%.2f", producto.getPrecio()));

// 🚨 ESTE CÓDIGO MANEJA AMBOS CASOS (Drawable y Ruta de Archivo)

        String imageNameOrPath = producto.getImagen();
        boolean loaded = false;

        if (imageNameOrPath != null && !imageNameOrPath.isEmpty()) {

            // 1. INTENTO: Cargar como RECURSO ESTÁTICO (res/drawable)
            int resId = context.getResources().getIdentifier(
                    imageNameOrPath, "drawable", context.getPackageName());

            if (resId != 0) {
                // ÉXITO: Se cargó desde la carpeta drawable
                holder.ivProducto.setImageResource(resId);
                loaded = true;
            }

            // 2. INTENTO: Cargar como ARCHIVO LOCAL (desde la galería/ruta de archivo)
            if (!loaded) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageNameOrPath);

                    if (bitmap != null) {
                        // ÉXITO: Se cargó desde la ruta de archivo local
                        holder.ivProducto.setImageBitmap(bitmap);
                        loaded = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

// 3. FALLBACK: Si no se pudo cargar en ninguno de los dos intentos
        if (!loaded) {
            holder.ivProducto.setImageResource(R.drawable.placeholder);
        }

// 🚨 FIN DEL BLOQUE DE CÓDIGO

        // 🚨 Línea que estaba fallando (Aproximadamente línea 66 en tu código original)
        holder.btnAgregarCarrito.setOnClickListener(v -> {

            boolean exito = carritoSingleton.agregarProducto(producto);

            if (exito) {
                Toast.makeText(context, producto.getNombre() + " añadido al carrito", Toast.LENGTH_SHORT).show();
            } else {
                int stockDisponible = producto.getStock();
                int cantidadEnCarrito = carritoSingleton.getCantidadProducto(producto.getId());

                if (stockDisponible <= 0) {
                    Toast.makeText(context, "Sin stock disponible.", Toast.LENGTH_SHORT).show();
                } else if (cantidadEnCarrito >= stockDisponible) {
                    Toast.makeText(context, "¡Stock insuficiente! Ya tienes el máximo (" + stockDisponible + ") en tu carrito.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "No se pudo añadir al carrito.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleProductoActivity.class);
            intent.putExtra("id_producto", producto.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProducto;
        TextView tvNombreProd;
        TextView tvPrecioProd;
        Button btnAgregarCarrito;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);

            // 🚨 CORRECCIÓN 2: Mapear los nombres de ID de XML a las variables de Java
            ivProducto = itemView.findViewById(R.id.ivProductoCatalogo);
            tvNombreProd = itemView.findViewById(R.id.tvNombre);
            tvPrecioProd = itemView.findViewById(R.id.tvPrecio);

            // Este ID sí coincidía, pero lo incluyo para asegurar
            btnAgregarCarrito = itemView.findViewById(R.id.btnAddCart);
        }
    }
}