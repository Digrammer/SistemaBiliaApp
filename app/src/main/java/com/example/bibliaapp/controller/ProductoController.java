package com.example.bibliaapp.controller;

import android.content.Context;
import android.database.Cursor;

import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductoController {

    private final DBHelper dbHelper;

    public ProductoController(Context context) {
        dbHelper = new DBHelper(context);
    }

    public int editarProducto(Producto producto) {
        return dbHelper.updateProducto(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getImagen(),
                producto.getIdCategoria(),
                producto.getStock()
        );
    }

    public long insertarProducto(Producto producto) {
        return dbHelper.insertProducto(
                producto.getNombre(),
                producto.getPrecio(),
                producto.getImagen(),
                producto.getIdCategoria(),
                producto.getStock()
        );
    }

    public Cursor getProductoById(int idProducto) {
        return dbHelper.getProductoById(idProducto);
    }

    public Cursor mostrarProductos() {
        return dbHelper.getAllProductos();
    }

    public int eliminarProducto(int idProducto) {
        return dbHelper.deleteProducto(idProducto);
    }

    public boolean actualizarStock(int idProducto, int cantidadAñadir) {
        return dbHelper.actualizarStock(idProducto, cantidadAñadir);
    }
}