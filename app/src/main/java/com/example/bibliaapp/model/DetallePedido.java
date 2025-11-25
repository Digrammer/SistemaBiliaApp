package com.example.bibliaapp.model;

public class DetallePedido {

    private int idDetalle;
    private int idPedido;   // Clave foránea al pedido principal
    private int idProducto; // Clave foránea al producto vendido
    private int cantidad;
    private double subtotal; // Cantidad * Precio

    // Constructor completo (usado generalmente al cargar desde la DB)
    public DetallePedido(int idDetalle, int idPedido, int idProducto, int cantidad, double subtotal) {
        this.idDetalle = idDetalle;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    // Constructor simple (usado para crear un nuevo detalle antes de insertarlo)
    public DetallePedido(int idPedido, int idProducto, int cantidad, double subtotal) {
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    // Getters necesarios para usar la información
    public int getIdDetalle() { return idDetalle; }
    public int getIdPedido() { return idPedido; }
    public int getIdProducto() { return idProducto; }
    public int getCantidad() { return cantidad; }
    public double getSubtotal() { return subtotal; }

    // Setters (Opcionales, pero es buena práctica tenerlos si los datos cambian)
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}