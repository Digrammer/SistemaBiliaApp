package com.example.bibliaapp.model.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bibliaapp.model.DBHelper;

public class DetallePedidoDao {
    private final DBHelper dbHelper;

    public DetallePedidoDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertDetallePedido(int id_pedido, int id_producto, int cantidad, double subtotal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_pedido", id_pedido);
        cv.put("id_producto", id_producto);
        cv.put("cantidad", cantidad);
        cv.put("subtotal", subtotal);
        return db.insert(DBHelper.TABLE_DETALLE_PEDIDO, null, cv);
    }

    public Cursor getDetalleByPedido(int id_pedido) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_DETALLE_PEDIDO + " WHERE id_pedido = ?", new String[]{String.valueOf(id_pedido)});
    }

    public Cursor getDetallePedidoConNombre(long id_pedido) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " +
                "dp.id_producto, dp.cantidad, dp.subtotal, p.nombre, p.precio " +
                "FROM " + DBHelper.TABLE_DETALLE_PEDIDO + " dp " +
                "JOIN " + DBHelper.TABLE_PRODUCTOS + " p ON dp.id_producto = p.id_producto " +
                "WHERE dp.id_pedido = ?";
        return db.rawQuery(query, new String[]{String.valueOf(id_pedido)});
    }
}
