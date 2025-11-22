package com.example.bibliaapp.model;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String imagen;
    private int idCategoria;
    private int stock;

    public Producto() {}

    public Producto(int id, String nombre, String descripcion, double precio, String imagen, int idCategoria, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagen = imagen;
        this.idCategoria = idCategoria;
        this.stock = stock;
    }

    public Producto(int id, String nombre, double precio, String imagen, int stock, int idCategoria) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = "";
        this.precio = precio;
        this.imagen = imagen;
        this.idCategoria = idCategoria;
        this.stock = stock;
    }


    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public String getImagen() { return imagen; }
    public int getIdCategoria() { return idCategoria; }
    public int getStock() { return stock; }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }
    public void setStock(int stock) { this.stock = stock; }
}