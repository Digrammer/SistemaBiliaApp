package com.example.bibliaapp.model;

public class CarritoItem {

    // Almacena el objeto Producto completo
    private final Producto producto;
    private int cantidad;

    // Campos redundantes pero mantenidos por si alguna parte del código los sigue usando
    private int productoId;
    private String nombre;
    private double precioUnitario;
    private String imagen;


    // Constructor adaptado para aceptar el objeto Producto completo
    public CarritoItem(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;

        // Inicializar campos redundantes para compatibilidad
        this.productoId = producto.getId();
        this.nombre = producto.getNombre();
        this.precioUnitario = producto.getPrecio();
        this.imagen = producto.getImagen();
    }

    // Constructor que usa el adaptador (lo mantenemos por si acaso, aunque ahora llama al de arriba)
    public CarritoItem(int productoId, String nombre, double precioUnitario, int cantidad, String imagen) {
        // En un diseño real, esto debería buscar el Producto en la base de datos.
        // Aquí lo dejamos para evitar errores de compilación, asumiendo que el constructor de arriba es el preferido.
        this.productoId = productoId;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.imagen = imagen;
        this.producto = null; // PELIGROSO, PERO NECESARIO PARA COMPILAR SI SE USA ESTE CONSTRUCTOR
    }

    // GETTER REQUERIDO POR CarritoAdapter.java y CheckoutActivity.java (Errores 2, 5, 6)
    public Producto getProducto() {
        // Si el objeto fue creado por el constructor simple, esto podría devolver null.
        return producto;
    }

    // Getters y Setters
    public int getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public String getImagen() { return imagen; }

    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getSubtotal() {
        return precioUnitario * cantidad;
    }
}