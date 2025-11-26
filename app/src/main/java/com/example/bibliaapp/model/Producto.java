package com.example.bibliaapp.model;

public class Producto {
    private int id;
    private String nombre;
    private double precio;
    private String imagen;
    private int stock; // ¡CRÍTICO para la lógica actual!
    private int idCategoria;

    // CONSTRUCTOR COMPLETO (Usado por DBHelper para getProductoByIdObject)
    public Producto(int id, String nombre, double precio, String imagen, int stock, int idCategoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
        this.stock = stock;
        this.idCategoria = idCategoria;
    }

    // CONSTRUCTOR BÁSICO (Para inserción o manejo simple)
    public Producto(String nombre, double precio, String imagen, int stock, int idCategoria) {
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
        this.stock = stock;
        this.idCategoria = idCategoria;
    }
    // Dentro de la clase Producto.java

    // Constructor vacío/por defecto necesario para inicializar el objeto antes de asignar propiedades.
    public Producto() {
        // Inicializa campos si es necesario, o déjalo vacío.
    }
// ... [otros constructores] ...

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }
}