package com.example.bibliaapp.model;

import java.util.List;

public class Pedido {
    private int id;
    private String nombreCliente;
    private String telefono;
    private String direccion;
    private String tipoEntrega;
    private List<CarritoItem> productos;

    public Pedido(int id, String nombreCliente, String telefono, String direccion, List<CarritoItem> productos) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.direccion = direccion;
        this.productos = productos;
    }

    public int getId() { return id; }
    public String getNombreCliente() { return nombreCliente; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getTipoEntrega() { return tipoEntrega; }
    public List<CarritoItem> getProductos() { return productos; }

    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }
}
