package com.example.bibliaapp.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast; // Importar Toast
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bibliaapp.R;
import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.CarritoSingleton; // Importar CarritoSingleton
import com.example.bibliaapp.model.Producto; // Importar Producto
import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.ViewHolder> {

    private final Context context;
    private final List<CarritoItem> carritoList;
    private final OnCarritoListener listener;
    private final CarritoSingleton carritoSingleton; // Instancia del Singleton

    public interface OnCarritoListener {
        void onCantidadCambiada();
    }

    public CarritoAdapter(Context context, List<CarritoItem> carritoList, OnCarritoListener listener) {
        this.context = context;
        this.carritoList = carritoList;
        this.listener = listener;
        this.carritoSingleton = CarritoSingleton.getInstance(); // Inicializar el Singleton
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carrito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarritoItem item = carritoList.get(position);
        holder.tvNombre.setText(item.getNombre());
        holder.tvCantidad.setText(String.valueOf(item.getCantidad()));
        holder.tvSubtotal.setText("S/" + String.format("%.2f", item.getSubtotal()));

        // === CANDADO DE STOCK EN EL BOTÓN AUMENTAR ===
        holder.btnAumentar.setOnClickListener(v -> {
            // 1. Obtener el producto real de la DB para saber su stock
            Producto productoReal = carritoSingleton.getProductoReal(item.getProductoId(), context);

            if (productoReal != null) {
                int stockDisponible = productoReal.getStock();

                // 2. Verificar el límite
                if (item.getCantidad() + 1 <= stockDisponible) {
                    // Si hay stock, se aumenta
                    item.setCantidad(item.getCantidad() + 1);
                    holder.tvCantidad.setText(String.valueOf(item.getCantidad()));
                    holder.tvSubtotal.setText("S/" + String.format("%.2f", item.getSubtotal()));
                    listener.onCantidadCambiada();
                } else {
                    // Si se alcanza el límite, se muestra una advertencia
                    Toast.makeText(context, "Stock máximo alcanzado para " + item.getNombre() + " (" + stockDisponible + " unidades).", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Error: Producto no encontrado.", Toast.LENGTH_SHORT).show();
            }
        });
        // ===========================================

        holder.btnDisminuir.setOnClickListener(v -> {
            if(item.getCantidad() > 1){
                item.setCantidad(item.getCantidad() - 1);
                holder.tvCantidad.setText(String.valueOf(item.getCantidad()));
                holder.tvSubtotal.setText("S/" + String.format("%.2f", item.getSubtotal()));
                listener.onCantidadCambiada();
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            carritoList.remove(position);
            notifyDataSetChanged();
            listener.onCantidadCambiada();
        });
    }

    @Override
    public int getItemCount() {
        return carritoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCantidad, tvSubtotal;
        ImageButton btnAumentar, btnDisminuir, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreCarrito);
            tvCantidad = itemView.findViewById(R.id.tvCantidadCarrito);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotalCarrito);
            btnAumentar = itemView.findViewById(R.id.btnAumentar);
            btnDisminuir = itemView.findViewById(R.id.btnDisminuir);
            btnEliminar = itemView.findViewById(R.id.btnEliminarCarrito);
        }
    }
}