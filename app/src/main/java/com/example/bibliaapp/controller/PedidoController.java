package com.example.bibliaapp.controller;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Pedido;
import com.example.bibliaapp.model.Producto;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para manejar la lógica de negocio de los Pedidos.
 * Es la capa intermedia entre la Activity y el DBHelper.
 */
public class PedidoController {

    private final DBHelper dbHelper;
    private final Context context;

    private static final String COL_ID_USUARIO_PEDIDO = "id_usuario";

    public PedidoController(Context context) {
        this.context = context;
        this.dbHelper = new DBHelper(context);
    }

    /**
     * Obtiene una lista de pedidos filtrada por rol.
     * @param idUsuario El ID del usuario logeado. Si es Admin, el llamador debe pasar -1.
     * @param rol El rol del usuario (administrador, vendedor, cliente).
     * @return Lista de objetos Pedido.
     */
    public List<Pedido> getPedidosByRol(int idUsuario, String rol) {
        Cursor cursor = null;
        try {
            // Lógica para el Administrador: Obtener TODOS los pedidos.
            if ("administrador".equalsIgnoreCase(rol)) {
                // Consulta SQL directa para obtener todos los pedidos
                cursor = dbHelper.getReadableDatabase().rawQuery(
                        "SELECT * FROM " + DBHelper.TABLE_PEDIDOS, null
                );
            }
            // Lógica para Vendedor y Cliente: Obtener SOLO sus pedidos.
            else if ("vendedor".equalsIgnoreCase(rol) || "cliente".equalsIgnoreCase(rol)) {
                // Usamos el método que ya existe en tu DBHelper (getPedidosByUsuario)
                cursor = dbHelper.getPedidosByUsuario(idUsuario);
            } else {
                return new ArrayList<>();
            }

            return convertCursorToPedidoList(cursor);

        } catch (Exception e) {
            Log.e("PedidoController", "Error al obtener pedidos por rol: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * Convierte el Cursor de la tabla PEDIDOS en una lista de objetos Pedido.
     */
    private List<Pedido> convertCursorToPedidoList(Cursor cursor) {
        List<Pedido> pedidos = new ArrayList<>();
        if (cursor == null || cursor.getCount() == 0) {
            return pedidos;
        }

        try {
            // Obtener los índices de las columnas
            int idPedidoIndex = cursor.getColumnIndexOrThrow("id_pedido"); // ID interno
            int codigoIndex = cursor.getColumnIndexOrThrow("codigo");
            int idUsuarioIndex = cursor.getColumnIndexOrThrow(COL_ID_USUARIO_PEDIDO);
            int estadoIndex = cursor.getColumnIndexOrThrow("estado");
            int tipoEntregaIndex = cursor.getColumnIndexOrThrow("metodo_pago");
            int telefonoIndex = cursor.getColumnIndexOrThrow("telefono_contacto");
            int totalIndex = cursor.getColumnIndexOrThrow("total");
            int nombreClienteIndex = cursor.getColumnIndexOrThrow("nombre_cliente");

            // *** NUEVAS COLUMNAS ***
            int idVendedorIndex = cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_ID_VENDEDOR);
            int tipoComprobanteIndex = cursor.getColumnIndexOrThrow(DBHelper.COL_PEDIDO_TIPO_COMPROBANTE);
            // ***********************

            while (cursor.moveToNext()) {
                int idPedidoInterno = cursor.getInt(idPedidoIndex);
                String codigo = cursor.getString(codigoIndex);
                int idUsuario = cursor.getInt(idUsuarioIndex);
                String estado = cursor.getString(estadoIndex);
                String tipoEntrega = cursor.getString(tipoEntregaIndex);
                String telefono = cursor.getString(telefonoIndex);
                double total = cursor.getDouble(totalIndex);
                String nombreCliente = cursor.getString(nombreClienteIndex);

                // *** LECTURA DE NUEVOS VALORES ***
                int idVendedor = cursor.isNull(idVendedorIndex) ? 0 : cursor.getInt(idVendedorIndex);
                // Si el valor es nulo (pedidos viejos), asumimos 'Boleta'
                String tipoComprobante = cursor.isNull(tipoComprobanteIndex) ? "Boleta" : cursor.getString(tipoComprobanteIndex);
                // **********************************

                // 1. Obtener la Dirección y Nombre registrado del usuario
                String direccionCliente = "N/A";
                // Usar el ID de la FK
                Cursor userCursor = dbHelper.getUsuarioById(idUsuario);
                if (userCursor != null && userCursor.moveToFirst()) {
                    int direccionIndex = userCursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_DIRECCION);
                    int nombreUsuarioIndex = userCursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_NOMBRE);

                    direccionCliente = userCursor.getString(direccionIndex);

                    if (nombreCliente == null || nombreCliente.isEmpty() || nombreCliente.equalsIgnoreCase("Tienda Física")) {
                        nombreCliente = userCursor.getString(nombreUsuarioIndex);
                    }
                    userCursor.close();
                }

                // 2. Obtener los ítems del detalle del pedido
                List<CarritoItem> items = getDetallePedidoItems(idPedidoInterno);

                // Construir el objeto Pedido usando el CONSTRUCTOR 3 (9 ARGUMENTOS)
                Pedido pedido = new Pedido(
                        Integer.parseInt(codigo),
                        idUsuario,
                        idVendedor, // <<-- Nuevo Argumento
                        total,
                        estado,
                        tipoEntrega,
                        telefono,
                        nombreCliente,
                        tipoComprobante, // <<-- Nuevo Argumento
                        items
                );
                // 3. Establecer la dirección usando el SETTER
                pedido.setDireccion(direccionCliente);

                pedidos.add(pedido);
            }
        } catch (Exception e) {
            Log.e("PedidoController", "Error al convertir Cursor a Pedido: " + e.getMessage());
        }
        return pedidos;
    }

    /**
     * Obtiene los items del detalle de un pedido específico.
     */
    public List<CarritoItem> getDetallePedidoItems(int idPedidoInterno) {
        // ... (El resto de la función es idéntica a tu código original y no necesita cambios)
        List<CarritoItem> items = new ArrayList<>();
        // ASUMO que dbHelper.getDetallePedidoConNombre existe y trae las columnas necesarias
        Cursor detalleCursor = dbHelper.getDetallePedidoConNombre(idPedidoInterno);
        if (detalleCursor == null || detalleCursor.getCount() == 0) {
            return items;
        }

        try {
            int cantidadIndex = detalleCursor.getColumnIndexOrThrow("cantidad");
            int subtotalIndex = detalleCursor.getColumnIndexOrThrow("subtotal");
            int nombreProductoIndex = detalleCursor.getColumnIndexOrThrow("nombre");
            int precioIndex = detalleCursor.getColumnIndexOrThrow("precio");
            int idProductoIndex = detalleCursor.getColumnIndexOrThrow("id_producto");

            while (detalleCursor.moveToNext()) {
                int cantidad = detalleCursor.getInt(cantidadIndex);
                double subtotal = detalleCursor.getDouble(subtotalIndex);
                String nombreProducto = detalleCursor.getString(nombreProductoIndex);
                double precioUnitario = detalleCursor.getDouble(precioIndex);
                int idProducto = detalleCursor.getInt(idProductoIndex);

                // Creamos el CarritoItem
                // (Usamos el constructor que toma Producto y cantidad, si existe)
                CarritoItem item = new CarritoItem(idProducto, nombreProducto, precioUnitario, cantidad, null);

                // 4. Asignamos el subtotal real (histórico) usando el SETTER (ya corregido en CarritoItem.java)
                item.setSubtotal(subtotal);
                items.add(item);
            }
        } catch (Exception e) {
            Log.e("PedidoController", "Error al obtener detalle del pedido: " + e.getMessage());
        } finally {
            if (detalleCursor != null) {
                detalleCursor.close();
            }
        }
        return items;
    }

    /**
     * Llama al DBHelper para actualizar el estado del pedido.
     */
    public boolean actualizarEstadoPedido(String codigoPedido, String nuevoEstado) {
        int filasAfectadas = dbHelper.updateEstadoPedido(codigoPedido, nuevoEstado);
        return filasAfectadas > 0;
    }
}