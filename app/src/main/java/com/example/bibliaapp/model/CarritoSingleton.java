package com.example.bibliaapp.model;

import java.util.ArrayList;
import java.util.List;

public class CarritoSingleton {
    private static CarritoSingleton instance;
    // La lista ahora contiene CarritoItem
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

    // ❌ Error 3, 4, 5 Corregido: Método renombrado para coincidir con CarritoActivity/CheckoutActivity
    public List<CarritoItem> getCarrito() {
        return items;
    }

    // ❌ Error 6 Corregido: Método para vaciar el carrito
    public void limpiarCarrito() {
        items.clear();
    }

    // ❌ Error 1 Corregido: Método para obtener la cantidad de un producto por ID
    public int getCantidadProducto(int productoId) {
        for (CarritoItem item : items) {
            if (item.getProductoId() == productoId) {
                return item.getCantidad();
            }
        }
        return 0;
    }

    // ❌ Error 2 Corregido: Método agregarProducto para compatibilidad con ProductoAdapter
    // Se asume que este método debe manejar la lógica de añadir/actualizar cantidad.
    public boolean agregarProducto(Producto producto, int cantidad) {
        if (producto.getStock() <= 0) {
            return false; // No hay stock
        }

        for (CarritoItem item : items) {
            if (item.getProductoId() == producto.getId()) {
                // Verificar si la adición excede el stock
                if (item.getCantidad() + cantidad > producto.getStock()) {
                    return false; // Excede stock
                }
                item.setCantidad(item.getCantidad() + cantidad);
                return true;
            }
        }

        // Si no existe, creamos un nuevo CarritoItem
        if (cantidad > 0 && cantidad <= producto.getStock()) {
            CarritoItem newItem = new CarritoItem(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    cantidad,
                    producto.getImagen()
            );
            items.add(newItem);
            return true;
        }
        return false;
    }

    // Se mantiene el método addItem (usado por CartManager) para mayor robustez
    public void addItem(CarritoItem newItem) {
        for (CarritoItem item : items) {
            if (item.getProductoId() == newItem.getProductoId()) {
                item.setCantidad(item.getCantidad() + newItem.getCantidad());
                return;
            }
        }
        items.add(newItem);
    }
}