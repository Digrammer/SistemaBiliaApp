package com.example.bibliaapp.controller;

import com.example.bibliaapp.model.CarritoItem; // Referencia corregida
import com.example.bibliaapp.model.CarritoSingleton;
import java.util.List;

public class CartManager {

    public CartManager() {
    }

    public void addItem(CarritoItem item) { // Usa CarritoItem
        CarritoSingleton.getInstance().addItem(item);
    }

    public List<CarritoItem> getItems() { // Usa CarritoItem y método getItems (o getCarrito si solo fuera ese)
        // Usamos getCarrito() para mantener la consistencia con las otras Activities
        return CarritoSingleton.getInstance().getCarrito();
    }
}