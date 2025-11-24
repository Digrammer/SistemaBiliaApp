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
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto_catalogo, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        holder.tvNombreProd.setText(producto.getNombre());
        holder.tvPrecioProd.setText("S/ " + String.format("%.2f", producto.getPrecio()));

        // LÓGICA DE CARGA DE IMAGEN MEJORADA (DOBLE VERIFICACIÓN + OPTIMIZACIÓN DE RENDIMIENTO)
        String imageNameOrPath = producto.getImagen();
        boolean loaded = false;

        if (imageNameOrPath != null && !imageNameOrPath.isEmpty()) {

            // 1. INTENTO: Cargar como RECURSO ESTÁTICO (res/drawable)
            int resId = context.getResources().getIdentifier(
                    imageNameOrPath, "drawable", context.getPackageName());

            if (resId != 0) {
                holder.ivProducto.setImageResource(resId);
                loaded = true;
            }

            // 2. INTENTO: Cargar como ARCHIVO LOCAL (desde la galería/ruta de archivo) - OPTIMIZADO
            if (!loaded) {
                try {
                    Bitmap bitmap = decodeSampledBitmapFromFile(imageNameOrPath, 300, 300);

                    if (bitmap != null) {
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
        // FIN DE LÓGICA DE CARGA DE IMAGEN

        // LÓGICA DEL BOTÓN AÑADIR AL CARRITO (ESTA ES LA ÚNICA INTERACCIÓN QUE DEBE EXISTIR)
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

        // 🛑 NO HAY CÓDIGO AQUÍ. ELIMINAMOS LA INTERACCIÓN DE CLIC EN LA TARJETA COMPLETA.

    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    // MÉTODOS AUXILIARES PARA OPTIMIZAR EL RENDIMIENTO DE LA CARGA DE BITMAPS

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProducto;
        TextView tvNombreProd;
        TextView tvPrecioProd;
        Button btnAgregarCarrito;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);

            // IDs CORRECTOS
            ivProducto = itemView.findViewById(R.id.ivProductoCatalogo);
            tvNombreProd = itemView.findViewById(R.id.tvNombre);
            tvPrecioProd = itemView.findViewById(R.id.tvPrecio);
            btnAgregarCarrito = itemView.findViewById(R.id.btnAddCart);
        }
    }
}