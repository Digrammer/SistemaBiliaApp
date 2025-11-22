package com.example.bibliaapp.view.adapter;

import android.content.Context;
import android.database.Cursor;
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
import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Producto;

import java.util.List;
import java.util.Map;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private final DBHelper dbHelper;
    private final CarritoSingleton carrito;
    private final Context context;
    private List<Map<String, Object>> productos;
    private final boolean esVisitante;

    public ProductoAdapter(Context context, List<Map<String, Object>> productos, boolean esVisitante) {
        this.context = context;
        this.productos = productos;
        this.esVisitante = esVisitante;
        dbHelper = new DBHelper(context);
        carrito = CarritoSingleton.getInstance();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Map<String, Object> p = productos.get(position);

        int idProducto = (int) p.get("id_producto");
        String nombre = (String) p.get("nombre");
        String imagen = (String) p.get("imagen");
        double precio = (double) p.get("precio");

        holder.tvNombre.setText(nombre);
        holder.tvPrecio.setText(String.format("S/ %.2f", precio));

        if (imagen != null && !imagen.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imagen);
                holder.ivProducto.setImageURI(imageUri);
            } catch (Exception e) {
                int resId = context.getResources().getIdentifier(imagen, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.ivProducto.setImageResource(resId);
                } else {
                    holder.ivProducto.setImageResource(R.drawable.placeholder);
                }
            }
        } else {
            holder.ivProducto.setImageResource(R.drawable.placeholder);
        }

        if (esVisitante) {
            holder.btnAddCart.setVisibility(View.GONE);
        } else {
            holder.btnAddCart.setVisibility(View.VISIBLE);
            holder.btnAddCart.setOnClickListener(v -> agregarProductoAlCarrito(idProducto));
        }
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public void updateList(List<Map<String, Object>> nuevaLista) {
        this.productos = nuevaLista;
        notifyDataSetChanged();
    }

    private Producto cursorToProducto(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_producto"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
                String imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
                int idCategoria = cursor.getInt(cursor.getColumnIndexOrThrow("id_categoria"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                cursor.close();
                return new Producto(id, nombre, precio, imagen, idCategoria, stock);
            } catch (IllegalArgumentException e) {
                if (cursor != null) cursor.close();
                return null;
            }
        }
        if (cursor != null) cursor.close();
        return null;
    }


    public boolean agregarProductoAlCarrito(int id) {
        // CORRECCIÓN: Obtenemos el Cursor y lo convertimos a Producto
        Cursor cursor = dbHelper.getProductoById(id);
        Producto producto = cursorToProducto(cursor);

        if (producto != null) {
            int stockDisponible = producto.getStock();
            int cantidadEnCarrito = carrito.getCantidadProducto(id);

            if (stockDisponible > cantidadEnCarrito) {
                boolean exito = carrito.agregarProducto(producto, stockDisponible);
                if (exito) {
                    Toast.makeText(context, "Producto añadido al carrito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error interno al añadir al carrito", Toast.LENGTH_SHORT).show();
                }
                return exito;
            } else {
                Toast.makeText(context, "Stock insuficiente o límite de stock alcanzado en carrito", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        Toast.makeText(context, "Error: Producto no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
        return false;
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