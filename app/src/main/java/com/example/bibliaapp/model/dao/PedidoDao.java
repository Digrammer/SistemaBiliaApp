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

    public long insertPedido(String codigo, int id_usuario, double total, String estado, String metodo_pago, String telefonoContacto) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("codigo", codigo);
        cv.put("id_usuario", id_usuario);
        cv.put("total", total);
        cv.put("estado", estado);
        cv.put("metodo_pago", metodo_pago);
        cv.put("telefono_contacto", telefonoContacto);
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
        return db.update(DBHelper.TABLE_PEDIDOS, cv, "codigo = ?", new String[]{codigo});
    }

    public long insertBoleta(int id_pedido, double total, String numero_boleta) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_pedido", id_pedido);
        cv.put("total", total);
        cv.put("numero_boleta", numero_boleta);
        return db.insert(DBHelper.TABLE_BOLETAS, null, cv);
    }

    public Cursor getBoletaByPedido(int id_pedido) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_BOLETAS + " WHERE id_pedido = ?", new String[]{String.valueOf(id_pedido)});
    }

    public Cursor getPedidoInfoById(long id_pedido) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT codigo, fecha, id_usuario, total, estado, metodo_pago, telefono_contacto, id_pedido, nombre_cliente FROM "
                + DBHelper.TABLE_PEDIDOS + " WHERE id_pedido = ?";
        return db.rawQuery(query, new String[]{String.valueOf(id_pedido)});
    }

    public long guardarPedidoCompleto(Pedido pedido, List<CarritoItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        long idPedidoInsertado = -1;
        double totalCalculado = pedido.getTotal();

        try {
            int id_usuario_anonimo = 3; // idéntico a tu implementación anterior

            ContentValues cvPedido = new ContentValues();
            cvPedido.put("codigo", String.valueOf(pedido.getIdPedido()));
            cvPedido.put("id_usuario", id_usuario_anonimo);
            cvPedido.put("estado", pedido.getEstado());
            cvPedido.put("metodo_pago", pedido.getTipoEntrega());
            cvPedido.put("telefono_contacto", pedido.getTelefono());
            cvPedido.put("total", totalCalculado);

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
                    boolean stockReducido = productoDao.actualizarStockPorCompra(item.getProductoId(), item.getCantidad());

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
            codigo = 100000 + (int) (Math.random() * 900000);
            String query = "SELECT codigo FROM " + DBHelper.TABLE_PEDIDOS + " WHERE codigo = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(codigo)});
            exists = cursor.getCount() > 0;
            cursor.close();
        } while (exists);
        return codigo;
    }

    // Métodos para ventas físicas (similar a guardarPedidoCompleto pero con nombre_cliente)
    public boolean insertPedidoFisico(Pedido pedido, int idUsuarioVendedor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        long pedidoIdDb = -1;

        try {
            ContentValues pedidoValues = new ContentValues();
            pedidoValues.put("codigo", pedido.getIdPedido());
            pedidoValues.put("id_usuario", idUsuarioVendedor);
            pedidoValues.put("total", pedido.getTotal());
            pedidoValues.put("estado", pedido.getEstado());
            pedidoValues.put("metodo_pago", pedido.getTipoEntrega());
            pedidoValues.put("telefono_contacto", pedido.getTelefono());
            pedidoValues.put("nombre_cliente", pedido.getNombreCliente());

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
                stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
            }
        } catch (Exception e) {
            Log.e("PedidoDao", "getStockByIdVentasFisicas: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return stock;
    }
}
