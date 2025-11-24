package com.example.bibliaapp.view.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
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
        // Asumiendo que item_producto_gestion.xml existe y contiene los elementos
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
        Button btnEliminar; // Nuevo botón de eliminar

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asumiendo que item_producto_gestion.xml ya tiene estos IDs:
            ivProducto = itemView.findViewById(R.id.ivProducto);
            tvNombrePrecio = itemView.findViewById(R.id.tvNombrePrecio);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnStockMas = itemView.findViewById(R.id.btnStockMas);
            btnEliminar = itemView.findViewById(R.id.btnEliminar); // Conectar el botón de eliminar

            btnEditar.setOnClickListener(v -> mostrarDialogoEdicion(getAdapterPosition()));
            btnStockMas.setOnClickListener(v -> mostrarDialogoAñadirStock(getAdapterPosition())); // Cambio aquí
            btnEliminar.setOnClickListener(v -> mostrarDialogoConfirmarEliminar(getAdapterPosition())); // Nuevo Listener
        }

        // === BLOQUE DE REEMPLAZO (Carga Asíncrona y Optimizada) ===
        public void bind(Producto producto) {
            tvNombrePrecio.setText(String.format("%s | S/ %.2f", producto.getNombre(), producto.getPrecio()));
            tvStock.setText(String.format("Stock: %d", producto.getStock()));

            // LÓGICA DE IMAGEN OPTIMIZADA (FIX LAG)
            String imagenStr = producto.getImagen();
            ivProducto.setImageResource(R.drawable.placeholder); // Default inmediato (Placeholder)

            if (imagenStr != null && !imagenStr.isEmpty()) {
                // Usamos un hilo secundario para evitar bloquear la UI
                new Thread(() -> {
                    try {
                        // 1. Intentar cargar como archivo (Productos nuevos)
                        java.io.File imgFile = new java.io.File(imagenStr);
                        if (imgFile.exists()) {
                            // Carga optimizada: reduce la imagen para que sea ligera
                            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                            options.inSampleSize = 4; // Reduce la imagen a 1/4 de su tamaño
                            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

                            // Vuelve al hilo principal para actualizar la vista (UI)
                            ivProducto.post(() -> ivProducto.setImageBitmap(bitmap));
                        } else {
                            // 2. Intentar cargar como recurso (Productos iniciales/por defecto)
                            int resId = context.getResources().getIdentifier(imagenStr, "drawable", context.getPackageName());
                            if (resId != 0) {
                                // Vuelve al hilo principal para actualizar la vista (UI)
                                ivProducto.post(() -> ivProducto.setImageResource(resId));
                            }
                        }
                    } catch (Exception e) {
                        // En caso de cualquier error de carga (ej. archivo no encontrado)
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        // ===================================
        // --- DIÁLOGO PARA EDITAR (Nombre y Precio) ---
        private void mostrarDialogoEdicion(int position) {
            if (position == RecyclerView.NO_POSITION) return;
            Producto producto = listaProductos.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);

            // Se asume que dialog_editar_simple.xml existe
            View dialogView = inflater.inflate(R.layout.dialog_editar_simple, null);
            builder.setView(dialogView);

            final EditText etDialogNombre = dialogView.findViewById(R.id.etDialogNombre);
            final EditText etDialogPrecio = dialogView.findViewById(R.id.etDialogPrecio);
            Button btnDialogGuardar = dialogView.findViewById(R.id.btnDialogGuardar);

            etDialogNombre.setText(producto.getNombre());
            etDialogPrecio.setText(String.valueOf(producto.getPrecio()));

            final AlertDialog dialog = builder.create();

            btnDialogGuardar.setOnClickListener(v -> {
                String nuevoNombre = etDialogNombre.getText().toString().trim();
                String nuevoPrecioStr = etDialogPrecio.getText().toString().trim();

                if (nuevoNombre.isEmpty() || nuevoPrecioStr.isEmpty()) {
                    Toast.makeText(context, "Todos los campos son obligatorios.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validación para que el precio sea numérico
                if (!nuevoPrecioStr.matches("^[0-9]+(\\.[0-9]+)?$")) {
                    Toast.makeText(context, "El precio debe ser un número válido.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double nuevoPrecio = Double.parseDouble(nuevoPrecioStr);

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
                    // Esta excepción fue cubierta por el regex, pero la mantenemos por seguridad
                    Toast.makeText(context, "Error interno en el formato de precio.", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }

        // --- DIÁLOGO PARA AÑADIR STOCK (SOLUCIÓN AL PROBLEMA 4) ---
        private void mostrarDialogoAñadirStock(int position) {
            if (position == RecyclerView.NO_POSITION) return;
            Producto producto = listaProductos.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);

            // Usamos un layout simple (puedes crear dialog_añadir_stock.xml)
            View dialogView = inflater.inflate(R.layout.dialog_anadir_stock, null);
            builder.setView(dialogView);

            final EditText etCantidad = dialogView.findViewById(R.id.etCantidadStock);
            Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarStock);

            TextView tvProductoStock = dialogView.findViewById(R.id.tvProductoStockActual);
            tvProductoStock.setText(String.format("Producto: %s\nStock Actual: %d", producto.getNombre(), producto.getStock()));

            final AlertDialog dialog = builder.create();

            btnConfirmar.setOnClickListener(v -> {
                String cantidadStr = etCantidad.getText().toString().trim();

                if (cantidadStr.isEmpty()) {
                    Toast.makeText(context, "Ingrese una cantidad a añadir.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int cantidadAñadir = Integer.parseInt(cantidadStr);

                    if (cantidadAñadir <= 0) {
                        Toast.makeText(context, "La cantidad debe ser mayor a cero.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Llamamos al método de sumar stock en DBHelper
                    boolean actualizado = dbHelper.actualizarStock(producto.getId(), cantidadAñadir);

                    if (actualizado) {
                        // Actualizamos la lista en memoria (lo que ya hace tu código)
                        producto.setStock(producto.getStock() + cantidadAñadir);
                        notifyItemChanged(position);
                        Toast.makeText(context, String.format("Se añadieron %d unidades a %s.", cantidadAñadir, producto.getNombre()), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error al actualizar stock.", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();

                } catch (NumberFormatException e) {
                    Toast.makeText(context, "La cantidad debe ser un número entero.", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }

        // --- DIÁLOGO PARA ELIMINAR (Punto 5) ---
        private void mostrarDialogoConfirmarEliminar(int position) {
            if (position == RecyclerView.NO_POSITION) return;
            Producto producto = listaProductos.get(position);

            new AlertDialog.Builder(context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que deseas eliminar el producto '" + producto.getNombre() + "'? Esta acción es irreversible.")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        int filasAfectadas = dbHelper.deleteProducto(producto.getId());

                        if (filasAfectadas > 0) {
                            // Eliminamos de la lista en memoria y notificamos al adaptador
                            listaProductos.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Producto eliminado correctamente.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error al intentar eliminar el producto.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }
}