package com.example.bibliaapp.model;

import java.util.List;

public class Pedido {

    private int idPedido;
    private int idUsuario; // <--- NUEVO CAMPO NECESARIO
    private String nombreCliente;
    private String telefono;
    private String direccion;
    private String estado;
    private String tipoEntrega;
    private double total;
    private final List<CarritoItem> items;

    // Constructor 1: Básico (usado por el cliente online)
    public Pedido(int idPedido, String nombreCliente, String telefono, String direccion, List<CarritoItem> items) {
        this.idPedido = idPedido;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.direccion = direccion;
        this.items = items;
        this.estado = "Pendiente";
        calcularTotal();
    }

    // Constructor 2: Completo (base de datos)
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

    // --- CONSTRUCTOR 3: NUEVO (PARA VENTAS FÍSICAS) ---
    // Este es el que te daba error. Ahora ya existe.
    public Pedido(int idPedido, int idUsuario, double total, String estado, String tipoEntrega,
                  String telefono, String nombreCliente, List<CarritoItem> items) {
        this.idPedido = idPedido;
        this.idUsuario = idUsuario; // Guardamos el ID del vendedor
        this.total = total;
        this.estado = estado;
        this.tipoEntrega = tipoEntrega;
        this.telefono = telefono;
        this.nombreCliente = nombreCliente;
        this.items = items;
        this.direccion = "Tienda Física"; // Valor por defecto
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

    // Getter nuevo para el ID de Usuario
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

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