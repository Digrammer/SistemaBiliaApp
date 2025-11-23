package com.example.bibliaapp.model;

import java.util.ArrayList;
import java.util.List;

public class PedidoSingleton {
    private static PedidoSingleton instance;
    private List<Pedido> pedidos;
    private int ultimoId; // Contador interno para IDs consecutivos

    private PedidoSingleton() {
        pedidos = new ArrayList<>();
        // Nota: Este singleton no se usa para generar IDs de BD, sino solo para manejo en memoria.
        // El ID real del pedido se genera aleatoriamente en CheckoutActivity.
        ultimoId = 0;
    }

    public static PedidoSingleton getInstance() {
        if (instance == null) {
            instance = new PedidoSingleton();
        }
        return instance;
    }

    // Nota: Esta función ya no es relevante si usas el ID aleatorio de CheckoutActivity
    public int generarNuevoId() {
        ultimoId += 1;
        return ultimoId;
    }

    public void agregarPedido(Pedido pedido) {
        pedidos.add(pedido);
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public Pedido getPedidoById(int id) {
        for (Pedido p : pedidos) {
            // CORRECCIÓN: Usar getIdPedido() en lugar de getId()
            if (p.getIdPedido() == id) return p;
        }
        return null;
    }
}