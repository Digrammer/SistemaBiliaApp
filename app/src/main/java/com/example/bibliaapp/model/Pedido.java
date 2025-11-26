package com.example.bibliaapp.model;

import java.util.List;

public class Pedido {

    private int idPedido;
    private int idUsuario;
    private int idVendedor; // Nuevo: ID del vendedor/admin que gestiona
    private String nombreCliente;
    private String telefono;
    private String direccion;
    private String estado;
    private String tipoEntrega; // Usado como método de pago en algunos casos (Ej: "Yape", "Plin")
    private String tipoComprobante; // Nuevo: "Boleta" o "Factura"
    private String ruc; // Nuevo: RUC para facturas
    private String razonSocial; // Nuevo: Razón Social para facturas
    private double total;
    private final List<CarritoItem> items;

    // Constructor 1: Básico (usado por el cliente online al iniciar checkout)
    public Pedido(int idPedido, String nombreCliente, String telefono, String direccion, List<CarritoItem> items, String tipoComprobante) {
        this.idPedido = idPedido;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.direccion = direccion;
        this.items = items;
        this.estado = "Pendiente";
        this.tipoComprobante = tipoComprobante; // Nuevo campo
        this.idVendedor = 0; // Por defecto 0 para venta online
        calcularTotal();
    }

    // Constructor 2: Completo (base de datos o para Ventas Físicas con más detalle)
    public Pedido(int idPedido, int idUsuario, int idVendedor, String nombreCliente, String telefono, String direccion,
                  String estado, String tipoEntrega, String tipoComprobante, String ruc, String razonSocial,
                  double total, List<CarritoItem> items) {
        this.idPedido = idPedido;
        this.idUsuario = idUsuario;
        this.idVendedor = idVendedor;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.direccion = direccion;
        this.estado = estado;
        this.tipoEntrega = tipoEntrega;
        this.tipoComprobante = tipoComprobante;
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.total = total;
        this.items = items;
    }

    // Constructor 3: (Para ventas físicas simplificado)
    public Pedido(int idPedido, int idUsuario, int idVendedor, double total, String estado, String tipoEntrega,
                  String telefono, String nombreCliente, String tipoComprobante, List<CarritoItem> items) {
        this.idPedido = idPedido;
        this.idUsuario = idUsuario; // ID del cliente (si es conocido)
        this.idVendedor = idVendedor; // ID del vendedor/admin
        this.total = total;
        this.estado = estado;
        this.tipoEntrega = tipoEntrega;
        this.telefono = telefono;
        this.nombreCliente = nombreCliente;
        this.tipoComprobante = tipoComprobante;
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

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    // *** NUEVOS GETTERS/SETTERS ***
    public int getIdVendedor() { return idVendedor; }
    public void setIdVendedor(int idVendedor) { this.idVendedor = idVendedor; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    // ******************************

    public String getNombreCliente() { return nombreCliente; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }
    public double getTotal() { return total; }
    public List<CarritoItem> getItems() { return items; }
}