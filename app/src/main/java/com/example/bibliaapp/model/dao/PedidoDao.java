package com.example.bibliaapp.model.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.Pedido;

import java.util.List;

public class PedidoDao {
    private final DBHelper dbHelper;

    public PedidoDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Inserta un nuevo pedido en la base de datos (Online o Físico).
     *
     * @param codigo Código único del pedido.
     * @param id_usuario ID del cliente que realiza el pedido.
     * @param total Monto total del pedido.
     * @param estado Estado del pedido (e.g., "Pendiente", "Completado").
     * @param metodo_pago Método de pago.
     * @param telefonoContacto Teléfono del cliente.
     * @param tipoComprobante El tipo de comprobante asociado ("Boleta" o "Factura").
     * @param idVendedor ID del usuario administrador/vendedor (puede ser 0 o -1 si es online).
     * @return El ID de la fila insertada o -1 en caso de error.
     */
    public long insertPedido(String codigo, int id_usuario, double total, String estado,
                             String metodo_pago, String telefonoContacto, String tipoComprobante, int idVendedor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("codigo", codigo);
        cv.put("id_usuario", id_usuario);
        cv.put("total", total);
        cv.put("estado", estado);
        cv.put("metodo_pago", metodo_pago);
        cv.put("telefono_contacto", telefonoContacto);

        // *** NUEVOS CAMPOS AÑADIDOS ***
        cv.put(DBHelper.COL_PEDIDO_TIPO_COMPROBANTE, tipoComprobante);

        // Solo añade el idVendedor si es válido, si no, se deja NULL en la base de datos (según el esquema)
        if (idVendedor > 0) {
            cv.put(DBHelper.COL_PEDIDO_ID_VENDEDOR, idVendedor);
        } else {
            cv.putNull(DBHelper.COL_PEDIDO_ID_VENDEDOR);
        }
        // *******************************

        return db.insert(DBHelper.TABLE_PEDIDOS, null, cv);
    }

    public Cursor getPedidosByUsuario(int id_usuario) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_PEDIDOS + " WHERE id_usuario = ?", new String[]{String.valueOf(id_usuario)});
    }

    public int updateEstadoPedido(String codigo, String estado) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", estado);

        // Establecemos que el cambio de estado es irreversible
        // Solo permitimos actualizar de 'Pendiente' a 'Completado' si el estado actual no es 'Completado'
        return db.update(DBHelper.TABLE_PEDIDOS, cv, "codigo = ? AND estado != 'Completado'", new String[]{codigo});
    }

    // --- LÓGICA DE COMPROBANTES ---

    // Método para insertar Boleta (Existente)
    public long insertBoleta(int id_pedido, double total, String numero_boleta) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_pedido", id_pedido);
        cv.put("total", total);
        cv.put("numero_boleta", numero_boleta);
        return db.insert(DBHelper.TABLE_BOLETAS, null, cv);
    }

    // Método para insertar Factura (NUEVO)
    public long insertFactura(int id_pedido, double total, String numero_factura, String ruc, String razonSocial) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_pedido", id_pedido);
        cv.put("total", total);
        cv.put("numero_factura", numero_factura);
        cv.put("ruc", ruc);
        cv.put("razon_social", razonSocial);
        return db.insert(DBHelper.TABLE_FACTURAS, null, cv);
    }

    // Método para obtener Boleta (Existente)
    public Cursor getBoletaByPedido(int id_pedido) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_BOLETAS + " WHERE id_pedido = ?", new String[]{String.valueOf(id_pedido)});
    }

    // Método para obtener Factura (NUEVO)
    public Cursor getFacturaByPedido(int id_pedido) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_FACTURAS + " WHERE id_pedido = ?", new String[]{String.valueOf(id_pedido)});
    }

    // --- FIN LÓGICA DE COMPROBANTES ---

    public Cursor getPedidoInfoById(long id_pedido) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Incluimos las nuevas columnas en el SELECT para ser consistentes, aunque no siempre se usen
        String query = "SELECT codigo, fecha, id_usuario, total, estado, metodo_pago, telefono_contacto, id_pedido, nombre_cliente, "
                + DBHelper.COL_PEDIDO_TIPO_COMPROBANTE + ", "
                + DBHelper.COL_PEDIDO_ID_VENDEDOR
                + " FROM " + DBHelper.TABLE_PEDIDOS + " WHERE id_pedido = ?";
        return db.rawQuery(query, new String[]{String.valueOf(id_pedido)});
    }

    // Este método está diseñado para ventas online donde el usuario es el cliente logueado o un anonimo.
    public long guardarPedidoCompleto(Pedido pedido, List<CarritoItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        long idPedidoInsertado = -1;
        double totalCalculado = pedido.getTotal();

        try {
            int id_usuario_anonimo = 3;

            ContentValues cvPedido = new ContentValues();
            cvPedido.put("codigo", String.valueOf(pedido.getIdPedido()));
            cvPedido.put("id_usuario", id_usuario_anonimo); // Asumiendo que el ID 3 es el cliente anonimo/web
            cvPedido.put("estado", pedido.getEstado()); // Estado inicial: "Pendiente"
            cvPedido.put("metodo_pago", pedido.getTipoEntrega()); // Aqui usas getTipoEntrega para el método de pago (ej: "Yape", "Plin")
            cvPedido.put("telefono_contacto", pedido.getTelefono());
            cvPedido.put("total", totalCalculado);

            // CAMBIO: Necesitas establecer el tipo_comprobante.
            String tipoComprobante = "Boleta"; // Valor temporal por defecto.
            if (pedido.getTipoComprobante() != null) {
                tipoComprobante = pedido.getTipoComprobante();
            }

            cvPedido.put(DBHelper.COL_PEDIDO_TIPO_COMPROBANTE, tipoComprobante);
            cvPedido.putNull(DBHelper.COL_PEDIDO_ID_VENDEDOR); // Venta online, no hay vendedor inicial

            idPedidoInsertado = db.insert(DBHelper.TABLE_PEDIDOS, null, cvPedido);

            if (idPedidoInsertado > 0) {
                boolean detallesOk = true;
                ProductoDao productoDao = new ProductoDao(dbHelper);

                for (CarritoItem item : items) {
                    ContentValues cvDetalle = new ContentValues();
                    cvDetalle.put("id_pedido", idPedidoInsertado);
                    cvDetalle.put("id_producto", item.getProductoId());
                    cvDetalle.put("cantidad", item.getCantidad());
                    cvDetalle.put("subtotal", item.getSubtotal());

                    long resultDetalle = db.insert(DBHelper.TABLE_DETALLE_PEDIDO, null, cvDetalle);

                    // Solo actualizamos stock si la inserción del detalle fue exitosa
                    boolean stockReducido = false;
                    if(resultDetalle > 0) {
                        stockReducido = productoDao.actualizarStockPorCompra(item.getProductoId(), item.getCantidad());
                    }

                    if (resultDetalle <= 0 || !stockReducido) {
                        detallesOk = false;
                        break;
                    }
                }

                if (detallesOk) {
                    db.setTransactionSuccessful();
                } else {
                    idPedidoInsertado = -1;
                }
            }
        } catch (Exception e) {
            Log.e("PedidoDao", "guardarPedidoCompleto: " + e.getMessage());
            idPedidoInsertado = -1;
        } finally {
            db.endTransaction();
        }

        return idPedidoInsertado;
    }

    public int generateCodigoPedido() {
        int codigo;
        boolean exists;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        do {
            // Genera un código de 6 dígitos
            codigo = 100000 + (int) (Math.random() * 900000);
            String query = "SELECT codigo FROM " + DBHelper.TABLE_PEDIDOS + " WHERE codigo = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(codigo)});
            exists = cursor != null && cursor.getCount() > 0;
            if (cursor != null) cursor.close();
        } while (exists);
        return codigo;
    }

    // Métodos para ventas físicas (similar a guardarPedidoCompleto pero con nombre_cliente y id de vendedor)
    public boolean insertPedidoFisico(Pedido pedido, int idUsuarioVendedor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        long pedidoIdDb = -1;

        try {
            ContentValues pedidoValues = new ContentValues();
            pedidoValues.put("codigo", pedido.getIdPedido());
            pedidoValues.put("id_usuario", pedido.getIdUsuario()); // ID del usuario cliente (si está registrado)
            pedidoValues.put("total", pedido.getTotal());
            pedidoValues.put("estado", pedido.getEstado()); // Estado inicial: "Completado" (por ser venta física)
            pedidoValues.put("metodo_pago", pedido.getTipoEntrega());
            pedidoValues.put("telefono_contacto", pedido.getTelefono());
            pedidoValues.put("nombre_cliente", pedido.getNombreCliente());

            // *** NUEVOS CAMPOS REQUERIDOS ***
            pedidoValues.put(DBHelper.COL_PEDIDO_TIPO_COMPROBANTE, pedido.getTipoComprobante()); // Asumiendo que el objeto Pedido tiene este método
            pedidoValues.put(DBHelper.COL_PEDIDO_ID_VENDEDOR, idUsuarioVendedor);
            // *********************************

            pedidoIdDb = db.insert(DBHelper.TABLE_PEDIDOS, null, pedidoValues);

            if (pedidoIdDb != -1) {
                for (CarritoItem item : pedido.getItems()) {
                    ContentValues detalleValues = new ContentValues();
                    detalleValues.put("id_pedido", pedidoIdDb);
                    detalleValues.put("id_producto", item.getProductoId());
                    detalleValues.put("cantidad", item.getCantidad());
                    detalleValues.put("subtotal", item.getSubtotal());

                    long detalleId = db.insert(DBHelper.TABLE_DETALLE_PEDIDO, null, detalleValues);

                    if (detalleId == -1) {
                        throw new Exception("Fallo al insertar detalle.");
                    }

                    // No usamos el DAO de Producto aquí para manejar la misma transacción
                    updateStockVentasFisicas(db, item.getProductoId(), item.getCantidad());
                }

                db.setTransactionSuccessful();
                success = true;
            }
        } catch (Exception e) {
            Log.e("PedidoDao", "insertPedidoFisico: " + e.getMessage());
            success = false;
        } finally {
            db.endTransaction();
        }
        return success;
    }

    // Métodos auxiliares de stock, están bien implementados para ser usados dentro de la transacción.
    private void updateStockVentasFisicas(SQLiteDatabase db, int idProducto, int cantidadVendida) {
        int stockActual = getStockByIdVentasFisicas(db, idProducto);
        int nuevoStock = stockActual - cantidadVendida;
        ContentValues values = new ContentValues();
        values.put("stock", nuevoStock);
        db.update(DBHelper.TABLE_PRODUCTOS, values, "id_producto = ?", new String[]{String.valueOf(idProducto)});
    }

    private int getStockByIdVentasFisicas(SQLiteDatabase db, int idProducto) {
        Cursor cursor = null;
        int stock = 0;
        try {
            String query = "SELECT stock FROM " + DBHelper.TABLE_PRODUCTOS + " WHERE id_producto = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(idProducto)});
            if (cursor != null && cursor.moveToFirst()) {
                // CORRECCIÓN para evitar advertencia de Lint (getColumnIndex puede ser -1)
                int columnIndex = cursor.getColumnIndex("stock");
                if (columnIndex >= 0) {
                    stock = cursor.getInt(columnIndex);
                } else {
                    // Esto indica un error grave en la base de datos (columna 'stock' no existe)
                    Log.e("PedidoDao", "ERROR: La columna 'stock' no fue encontrada en la tabla PRODUCTOS.");
                }
            }
        } catch (Exception e) {
            Log.e("PedidoDao", "getStockByIdVentasFisicas: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return stock;
    }
}