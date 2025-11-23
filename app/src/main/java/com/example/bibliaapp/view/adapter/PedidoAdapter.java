package com.example.bibliaapp.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.Pedido;
import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private final Context context;
    private final List<Pedido> listaPedidos;

    public PedidoAdapter(Context context, List<Pedido> listaPedidos) {
        this.context = context;
        this.listaPedidos = listaPedidos;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asume que tienes un layout llamado list_item_pedido.xml
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = listaPedidos.get(position);

        // Muestra la información del pedido
        holder.tvPedidoCodigo.setText(String.format("Pedido #%d", pedido.getIdPedido()));
        holder.tvPedidoTotal.setText(String.format("Total: S/%.2f", pedido.getTotal()));
        holder.tvPedidoEstado.setText(String.format("Estado: %s", pedido.getEstado()));
        holder.tvPedidoMetodo.setText(String.format("Método: %s", pedido.getTipoEntrega()));

        // Manejar el click en el item si se necesita ver el detalle
        holder.itemView.setOnClickListener(v -> {
            // Ejemplo: Abrir una nueva actividad para ver los detalles del pedido
            // Intent intent = new Intent(context, DetallePedidoActivity.class);
            // intent.putExtra("pedidoId", pedido.getIdPedido());
            // context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvPedidoCodigo, tvPedidoTotal, tvPedidoEstado, tvPedidoMetodo;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asume que tienes estos IDs en tu list_item_pedido.xml
            tvPedidoCodigo = itemView.findViewById(R.id.tvPedidoCodigo);
            tvPedidoTotal = itemView.findViewById(R.id.tvPedidoTotal);
            tvPedidoEstado = itemView.findViewById(R.id.tvPedidoEstado);
            tvPedidoMetodo = itemView.findViewById(R.id.tvPedidoMetodo);
        }
    }
}