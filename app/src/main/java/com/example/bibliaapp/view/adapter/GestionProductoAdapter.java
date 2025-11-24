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
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

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

    // Métodos de optimización de imagen (copiados de ProductoCatalogoAdapter para ser consistentes)
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

    public class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProducto;
        TextView tvNombrePrecio;
        TextView tvStock;
        Button btnEditar;
        Button btnStockMas;
        Button btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);

            // 🛑 CORRECCIÓN CLAVE: ANULAR CUALQUIER CLICK EN EL ITEM COMPLETO (TARJETA)
            // Esto previene que se disparen listeners residuales o de navegación.
            itemView.setOnClickListener(null);
            itemView.setClickable(false);

            ivProducto = itemView.findViewById(R.id.ivProducto);
            tvNombrePrecio = itemView.findViewById(R.id.tvNombrePrecio);
            tvStock = itemView.findViewById(R.id.tvStock);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnStockMas = itemView.findViewById(R.id.btnStockMas);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);

            // Listeners de los botones internos (los únicos que deben funcionar)
            btnEditar.setOnClickListener(v -> mostrarDialogoEdicion(getAdapterPosition()));
            btnStockMas.setOnClickListener(v -> mostrarDialogoAñadirStock(getAdapterPosition()));
            btnEliminar.setOnClickListener(v -> mostrarDialogoConfirmarEliminar(getAdapterPosition()));
        }

        // === BLOQUE DE REEMPLAZO (Carga de Imagen OPTIMIZADA) ===
        // Reemplazo la lógica de hilos por la función optimizada para consistencia.
        public void bind(Producto producto) {
            tvNombrePrecio.setText(String.format("%s | S/ %.2f", producto.getNombre(), producto.getPrecio()));
            tvStock.setText(String.format("Stock: %d", producto.getStock()));

            String imagenStr = producto.getImagen();
            boolean loaded = false;

            if (imagenStr != null && !imagenStr.isEmpty()) {
                // 1. Intentar cargar como recurso (Productos iniciales/por defecto)
                int resId = context.getResources().getIdentifier(imagenStr, "drawable", context.getPackageName());
                if (resId != 0) {
                    ivProducto.setImageResource(resId);
                    loaded = true;
                }

                // 2. Intentar cargar como archivo local (optimizado)
                if (!loaded) {
                    try {
                        Bitmap bitmap = decodeSampledBitmapFromFile(imagenStr, 100, 100);

                        if (bitmap != null) {
                            ivProducto.setImageBitmap(bitmap);
                            loaded = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!loaded) {
                ivProducto.setImageResource(R.drawable.placeholder);
            }
        }
        // ===================================

        // --- DIÁLOGO PARA EDITAR (Nombre y Precio) ---
        private void mostrarDialogoEdicion(int position) {
            if (position == RecyclerView.NO_POSITION) return;
            Producto producto = listaProductos.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);

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
                    Toast.makeText(context, "Error interno en el formato de precio.", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }

        // --- DIÁLOGO PARA AÑADIR STOCK ---
        private void mostrarDialogoAñadirStock(int position) {
            if (position == RecyclerView.NO_POSITION) return;
            Producto producto = listaProductos.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);

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

                    boolean actualizado = dbHelper.actualizarStock(producto.getId(), cantidadAñadir);

                    if (actualizado) {
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

        // --- DIÁLOGO PARA ELIMINAR (CORREGIDO: MANEJO DE ERROR DE LLAVE FORÁNEA) ---
        private void mostrarDialogoConfirmarEliminar(int position) {
            if (position == RecyclerView.NO_POSITION) return;
            Producto producto = listaProductos.get(position);

            new AlertDialog.Builder(context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que deseas eliminar el producto '" + producto.getNombre() + "'? Esta acción es irreversible.")
                    .setPositiveButton("Eliminar", (dialog, which) -> {

                        try {
                            // Intentamos la eliminación
                            int filasAfectadas = dbHelper.deleteProducto(producto.getId());

                            if (filasAfectadas > 0) {
                                // 1. Éxito: El producto no tenía ventas registradas.
                                listaProductos.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Producto eliminado correctamente.", Toast.LENGTH_SHORT).show();
                            } else {
                                // 2. Falla Genérica: Producto no encontrado.
                                Toast.makeText(context, "Error al intentar eliminar el producto. No se encontró en la DB.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (android.database.sqlite.SQLiteConstraintException e) {
                            // 3. FALLA CLAVE: Capturamos la restricción de llave foránea (el producto fue vendido)
                            Toast.makeText(context, "⚠️ No se puede eliminar: Ha sido registrado en una o más ventas.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            // 4. Cualquier otro error inesperado.
                            Toast.makeText(context, "Error inesperado al intentar eliminar.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }
}