package com.example.bibliaapp.model;

import java.util.ArrayList;
import java.util.List;

public class PedidoSingleton {
    private static PedidoSingleton instance;
    private List<Pedido> pedidos;
    private int ultimoId; // Contador interno para IDs consecutivos

    private PedidoSingleton() {
        pedidos = new ArrayList<>();
        ultimoId = 0; // Empieza en 0
    }

    public static PedidoSingleton getInstance() {
        if (instance == null) {
            instance = new PedidoSingleton();
        }
        return instance;
    }

    // Genera un nuevo ID consecutivo
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
            if (p.getId() == id) return p;
        }
        return null;
    }
}
