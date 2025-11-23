package com.example.bibliaapp.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bibliaapp.R;
import com.example.bibliaapp.model.CarritoItem;
import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.ViewHolder> {

    private Context context;
    private List<CarritoItem> carritoList;
    private OnCarritoListener listener;

    public interface OnCarritoListener {
        void onCantidadCambiada();
    }

    public CarritoAdapter(Context context, List<CarritoItem> carritoList, OnCarritoListener listener) {
        this.context = context;
        this.carritoList = carritoList;
        this.listener = listener;
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

        holder.btnAumentar.setOnClickListener(v -> {
            item.setCantidad(item.getCantidad() + 1);
            holder.tvCantidad.setText(String.valueOf(item.getCantidad()));
            holder.tvSubtotal.setText("S/" + String.format("%.2f", item.getSubtotal()));
            listener.onCantidadCambiada();
        });

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
