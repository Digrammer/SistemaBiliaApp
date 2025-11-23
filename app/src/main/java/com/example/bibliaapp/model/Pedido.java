package com.example.bibliaapp.model;

import java.util.List;

public class Pedido {

    private int idPedido; // El ID generado aleatoriamente
    private String nombreCliente;
    private String telefono;
    private String direccion;
    private String estado; // Pendiente, Entregado, Cancelado
    private String tipoEntrega; // Usado para el método de pago (Yape/Plin)
    private double total;
    private final List<CarritoItem> items; // La lista de productos comprados

    public Pedido(int idPedido, String nombreCliente, String telefono, String direccion, List<CarritoItem> items) {
        this.idPedido = idPedido;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.direccion = direccion;
        this.items = items;
        this.estado = "Pendiente";
        calcularTotal(); // Llamar para calcular el total al crear el pedido
    }

    // Constructor completo para cuando se carga desde la base de datos (para futuros módulos)
    public Pedido(int idPedido, String nombreCliente, String telefono, String direccion, String estado, String tipoEntrega, double total, List<CarritoItem> items) {
        this.idPedido = idPedido;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.direccion = direccion;
        this.estado = estado;
        this.tipoEntrega = tipoEntrega;
        this.total = total;
        this.items = items;
    }

    private void calcularTotal() {
        this.total = 0;
        if (items != null) {
            for (CarritoItem item : items) {
                this.total += item.getSubtotal();
            }
        }
    }

    // Getters y Setters
    public int getIdPedido() { return idPedido; }
    public String getNombreCliente() { return nombreCliente; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }
    public double getTotal() { return total; }
    public List<CarritoItem> getItems() { return items; }
}