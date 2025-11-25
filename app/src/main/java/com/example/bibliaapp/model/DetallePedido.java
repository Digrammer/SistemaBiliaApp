package com.example.bibliaapp.model;

/**
 * Representa una línea en un pedido (una entrada en la boleta).
 * Esta clase es CRÍTICA porque combina el Producto (precio, nombre)
 * con la Cantidad comprada en la transacción.
 */
public class DetallePedido {
    private Producto producto;
    private int cantidadComprada;

    /**
     * Constructor para crear una línea de pedido.
     * @param producto El objeto Producto que se compró.
     * @param cantidadComprada La cantidad de unidades compradas de ese producto.
     */
    public DetallePedido(Producto producto, int cantidadComprada) {
        this.producto = producto;
        this.cantidadComprada = cantidadComprada;
    }

    // --- Getters del Objeto ---

    /** Obtiene el objeto Producto asociado a esta línea. */
    public Producto getProducto() {
        return producto;
    }

    /** Obtiene la cantidad de unidades compradas en esta línea. */
    public int getCantidadComprada() {
        return cantidadComprada;
    }

    // --- Métodos de Conveniencia (para el PDF) ---

    /** Obtiene el nombre del producto directamente. */
    public String getNombre() {
        return producto.getNombre();
    }

    /** Obtiene el precio unitario del producto. */
    public double getPrecioUnitario() {
        return producto.getPrecio();
    }

    /** Calcula el total de la línea (Precio * Cantidad). */
    public double getTotalLinea() {
        return getPrecioUnitario() * cantidadComprada;
    }
}