package com.example.bibliaapp.controller;

import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.CarritoSingleton;

public class CartManager {

    private final CarritoSingleton carrito;

    public CartManager() {
        this.carrito = CarritoSingleton.getInstance();
    }

    public void addItem(CarritoItem item) {
        carrito.addItem(item);
    }

    public int getCantidadProducto(int idProducto) {
        return carrito.getCantidadProducto(idProducto);
    }
}