package com.example.bibliaapp.model;

public class CarritoItem {
    private int productoId;
    private String nombre;
    private double precioUnitario;
    private int cantidad;
    private String imagen;

    // --- [INICIO] CAMBIOS ESTRICTAMENTE NECESARIOS ---
    // 1. Nueva variable para almacenar el subtotal (necesaria para la persistencia)
    private double subtotal;
    // --- [FIN] CAMBIOS ESTRICTAMENTE NECESARIOS ---

    // CONSTRUCTOR (Usado por CarritoSingleton)
    public CarritoItem(int productoId, String nombre, double precioUnitario, int cantidad, String imagen) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.imagen = imagen;

        // 3. Inicializamos la variable en el constructor (comportamiento original)
        this.subtotal = precioUnitario * cantidad;
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
        // 4. Ahora retorna la variable. Si PedidoController llamó a setSubtotal,
        // este será el valor cargado de la base de datos (histórico).
        return subtotal;
    }

    // --- [INICIO] CAMBIOS ESTRICTAMENTE NECESARIOS ---
    // 2. Nuevo Setter requerido por PedidoController
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    // --- [FIN] CAMBIOS ESTRICTAMENTE NECESARIOS ---
}