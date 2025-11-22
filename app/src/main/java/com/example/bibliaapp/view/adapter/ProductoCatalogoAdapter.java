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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.controller.CartManager;
import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.Producto;
import com.example.bibliaapp.view.DetalleProductoActivity;

import java.util.List;

public class ProductoCatalogoAdapter extends RecyclerView.Adapter<ProductoCatalogoAdapter.ProductoViewHolder> {

    private final Context context;
    private List<Producto> listaProductos;
    private final CartManager cartManager;

    public ProductoCatalogoAdapter(Context context, List<Producto> listaProductos) {
        this.context = context;
        this.listaProductos = listaProductos;
        this.cartManager = new CartManager();
    }

    public void updateList(List<Producto> newList) {
        this.listaProductos = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        holder.tvNombreProd.setText(producto.getNombre());
        holder.tvPrecioProd.setText("S/ " + String.format("%.2f", producto.getPrecio()));

        int resId = context.getResources().getIdentifier(
                producto.getImagen(), "drawable", context.getPackageName());

        if (resId != 0) {
            holder.ivProducto.setImageResource(resId);
        } else {
            holder.ivProducto.setImageResource(R.drawable.placeholder);
        }

        holder.btnAgregarCarrito.setOnClickListener(v -> {
            // <--- CORRECCIÓN A CarritoItem
            CarritoItem newItem = new CarritoItem(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    1,
                    producto.getImagen()
            );
            cartManager.addItem(newItem);
            Toast.makeText(context, producto.getNombre() + " añadido al carrito", Toast.LENGTH_SHORT).show();
        });

        //holder.itemView.setOnClickListener(v -> {
        //Intent intent = new Intent(context, DetalleProductoActivity.class);
        // intent.putExtra("id_producto", producto.getId());
        //  context.startActivity(intent);
        //});
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
            ivProducto = itemView.findViewById(R.id.ivProducto);
            tvNombreProd = itemView.findViewById(R.id.tvNombreProd);
            tvPrecioProd = itemView.findViewById(R.id.tvPrecioProd);
            btnAgregarCarrito = itemView.findViewById(R.id.btnAgregarCarrito);
        }
    }
}