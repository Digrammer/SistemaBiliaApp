package com.example.bibliaapp.model;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class CarritoSingleton {
    private static CarritoSingleton instance;
    private final List<CarritoItem> items;

    private CarritoSingleton() {
        items = new ArrayList<>();
    }

    public static synchronized CarritoSingleton getInstance() {
        if (instance == null) {
            instance = new CarritoSingleton();
        }
        return instance;
    }

    public List<CarritoItem> getCarrito() {
        return items;
    }

    public void limpiarCarrito() {
        items.clear();
    }

    public int getCantidadProducto(int productoId) {
        for (CarritoItem item : items) {
            if (item.getProductoId() == productoId) {
                return item.getCantidad();
            }
        }
        return 0;
    }

    // Método para obtener el objeto Producto real de la DB (Necesario para CarritoAdapter)
    public Producto getProductoReal(int productoId, Context context) {
        DBHelper dbHelper = new DBHelper(context);
        return dbHelper.getProductoByIdObject(productoId);
    }

    // Método reintegrado para compatibilidad con CartManager (añade CarritoItem)
    public void addItem(CarritoItem newItem) {
        for (CarritoItem item : items) {
            if (item.getProductoId() == newItem.getProductoId()) {
                item.setCantidad(item.getCantidad() + newItem.getCantidad());
                return;
            }
        }
        items.add(newItem);
    }

    // Método principal para añadir desde el catálogo (ProductoAdapter)
    public boolean agregarProducto(Producto producto) {
        if (producto.getStock() <= 0) {
            return false;
        }

        for (CarritoItem item : items) {
            if (item.getProductoId() == producto.getId()) {
                // Validación estricta de stock
                if (item.getCantidad() + 1 > producto.getStock()) {
                    return false;
                }
                item.setCantidad(item.getCantidad() + 1);
                return true;
            }
        }

        // Si no existe, creamos un nuevo CarritoItem (cantidad = 1)
        if (1 <= producto.getStock()) {
            CarritoItem newItem = new CarritoItem(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    1,
                    producto.getImagen()
            );
            items.add(newItem);
            return true;
        }
        return false;
    }
}