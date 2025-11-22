package com.example.bibliaapp.view.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Producto;

import java.util.List;

public class GestionProductoAdapter extends RecyclerView.Adapter<GestionProductoAdapter.ProductoViewHolder> {

    private final Context context;
    private final List<Producto> listaProductos;
    private final DBHelper dbHelper;

    public GestionProductoAdapter(Context context, List<Producto> listaProductos) {
        this.context = context;
        this.listaProductos = listaProductos;
        this.dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto_gestion, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.bind(producto);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public void updateList(List<Producto> nuevaLista) {
        listaProductos.clear();
        listaProductos.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProducto;
        TextView tvNombrePrecio;
        TextView tvStock;
        Button btnEditar;
        Button btnStockMas;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProducto = itemView.findViewById(R.id.ivProducto);
            tvNombrePrecio = itemView.findViewById(R.id.tvNombrePrecio);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnStockMas = itemView.findViewById(R.id.btnStockMas);

            btnEditar.setOnClickListener(v -> mostrarDialogoEdicion(getAdapterPosition()));
            btnStockMas.setOnClickListener(v -> actualizarStock(getAdapterPosition()));
        }

        public void bind(Producto producto) {
            tvNombrePrecio.setText(String.format("%s | S/ %.2f", producto.getNombre(), producto.getPrecio()));
            tvStock.setText(String.format("Stock: %d", producto.getStock()));
            ivProducto.setImageResource(R.drawable.placeholder); // Usamos el placeholder por defecto
        }

        private void mostrarDialogoEdicion(int position) {
            if (position == RecyclerView.NO_POSITION) return;
            Producto producto = listaProductos.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);

            // Usamos un layout simple de edición
            View dialogView = inflater.inflate(R.layout.dialog_editar_simple, null);
            builder.setView(dialogView);

            // Nota: Se asume que estos IDs existen en dialog_editar_simple.xml
            final EditText etDialogNombre = dialogView.findViewById(R.id.etDialogNombre);
            final EditText etDialogPrecio = dialogView.findViewById(R.id.etDialogPrecio);
            Button btnDialogGuardar = dialogView.findViewById(R.id.btnDialogGuardar);

            etDialogNombre.setText(producto.getNombre());
            etDialogPrecio.setText(String.valueOf(producto.getPrecio()));

            final AlertDialog dialog = builder.create();

            btnDialogGuardar.setOnClickListener(v -> {
                String nuevoNombre = etDialogNombre.getText().toString();
                String nuevoPrecioStr = etDialogPrecio.getText().toString();

                if (nuevoNombre.isEmpty() || nuevoPrecioStr.isEmpty()) {
                    Toast.makeText(context, "Todos los campos son obligatorios.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double nuevoPrecio = Double.parseDouble(nuevoPrecioStr);

                    // CORRECCIÓN DE ERROR (PASAR LOS 6 ARGUMENTOS)
                    int filasAfectadas = dbHelper.updateProducto(
                            producto.getId(),
                            nuevoNombre,
                            nuevoPrecio,
                            producto.getImagen(),
                            producto.getIdCategoria(),
                            producto.getStock()
                    );

                    if (filasAfectadas > 0) {
                        producto.setNombre(nuevoNombre);
                        producto.setPrecio(nuevoPrecio);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Producto actualizado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error al actualizar.", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Precio debe ser numérico.", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }

        private void actualizarStock(int position) {
            if (position == RecyclerView.NO_POSITION) return;
            Producto producto = listaProductos.get(position);
            int cantidadAñadir = 1;

            boolean actualizado = dbHelper.actualizarStock(producto.getId(), cantidadAñadir);

            if (actualizado) {
                producto.setStock(producto.getStock() + cantidadAñadir);
                notifyItemChanged(position);
                Toast.makeText(context, "Stock de " + producto.getNombre() + " incrementado.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al actualizar stock.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}