package com.example.bibliaapp.model;

public class CarritoItem {
    private int productoId;
    private String nombre;
    private double precioUnitario;
    private int cantidad;
    private String imagen;

    // CONSTRUCTOR (Usado por CarritoSingleton)
    public CarritoItem(int productoId, String nombre, double precioUnitario, int cantidad, String imagen) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.imagen = imagen;
    }

    // --- Getters y Setters ---

    public int getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getImagen() {
        return imagen;
    }

    public double getSubtotal() {
        return precioUnitario * cantidad;
    }

    // Nota: El subtotal es un getter calculado y no una variable, lo cual es correcto.
}