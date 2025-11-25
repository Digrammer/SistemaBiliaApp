package com.example.bibliaapp.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R; // Asegúrate de que este paquete sea correcto
import com.example.bibliaapp.controller.PedidoController;
import com.example.bibliaapp.model.Pedido;

import java.util.List;

/**
 * Adaptador para mostrar la lista de Pedidos, implementando lógica de visibilidad y acción
 * basada en el Rol del usuario logeado.
 */
public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    // *** PUNTO 1: Número de WhatsApp Fijo del Administrador (PERÚ) ***
    private static final String ADMIN_WHATSAPP_NUMBER = "+51963608538";

    private final Context context;
    private final List<Pedido> pedidosList;
    private final String userRol;
    private final PedidoController pedidoController;
    private final OnPedidoActionListener listener;

    // Interfaz para notificar a la Activity sobre una acción (ej. actualizar la lista)
    public interface OnPedidoActionListener {
        void onEstadoActualizado();
        void onDescargarPdf(Pedido pedido);
    }

    public PedidoAdapter(Context context, List<Pedido> pedidosList, String userRol, OnPedidoActionListener listener) {
        this.context = context;
        this.pedidosList = pedidosList;
        // El rol debe ser siempre minúscula (administrador, vendedor, cliente)
        this.userRol = userRol.toLowerCase();
        this.pedidoController = new PedidoController(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidosList.get(position);
        final String estado = pedido.getEstado().toLowerCase();

        // 1. Cargar datos básicos
        holder.tvCodigo.setText(String.format("Pedido #%s", pedido.getIdPedido()));
        holder.tvCliente.setText(String.format("Cliente: %s", pedido.getNombreCliente()));
        holder.tvTelefono.setText(String.format("Telf: %s", pedido.getTelefono()));
        holder.tvTotal.setText(String.format("Total: S/. %.2f", pedido.getTotal()));
        holder.tvEstado.setText(pedido.getEstado());

        // 2. Colorear el estado (mejora la UX)
        int color;
        switch (estado) {
            case "completado":
                // Utiliza un color más claro si es necesario, si R.color.green no existe.
                color = ContextCompat.getColor(context, android.R.color.holo_green_dark);
                break;
            case "pendiente":
                color = ContextCompat.getColor(context, android.R.color.holo_red_dark);
                break;
            default:
                color = ContextCompat.getColor(context, android.R.color.black);
                break;
        }
        holder.tvEstado.setTextColor(color);


        // *** 3. Lógica de Roles y Botones (Puntos 1, 3 y 4) ***

        // Ocultar todos por defecto
        holder.btnWhatsApp.setVisibility(View.GONE);
        holder.btnAccionDinamica.setVisibility(View.GONE);

        if ("administrador".equals(userRol) || "vendedor".equals(userRol)) {
            // ** ROLES DE GESTIÓN **

            // Mostrar botón de WhatsApp para contactar al cliente (el número guardado en el pedido)
            holder.btnWhatsApp.setVisibility(View.VISIBLE);
            holder.btnWhatsApp.setText("WhatsApp (Cliente)"); // Etiqueta clara para Admin/Vendedor
            holder.btnWhatsApp.setOnClickListener(v -> contactar(pedido.getTelefono()));

            // Lógica del botón de Acción Dinámica (Completado o PDF)
            holder.btnAccionDinamica.setVisibility(View.VISIBLE);

            if ("pendiente".equals(estado)) {
                // Si está pendiente, el botón es para GESTIONAR (Cambiar a Completado)
                holder.btnAccionDinamica.setText("Marcar Completado");
                holder.btnAccionDinamica.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                holder.btnAccionDinamica.setOnClickListener(v -> confirmarCambioEstado(pedido));

            } else if ("completado".equals(estado)) {
                // Si está completado (Venta Online finalizada o Venta Física - Punto 4), el botón es para PDF
                holder.btnAccionDinamica.setText("Descargar Boleta/PDF");
                holder.btnAccionDinamica.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
                holder.btnAccionDinamica.setOnClickListener(v -> listener.onDescargarPdf(pedido));
            }

        } else if ("cliente".equals(userRol)) {
            // ** ROL CLIENTE (Venta Online) **

            // Mostrar botón de WhatsApp para contactar al ADMINISTRADOR (número fijo)
            holder.btnWhatsApp.setVisibility(View.VISIBLE);
            holder.btnWhatsApp.setText("WhatsApp (Admin)"); // Etiqueta clara para el Cliente
            holder.btnWhatsApp.setOnClickListener(v -> contactar(ADMIN_WHATSAPP_NUMBER)); // Usa el número fijo (Punto 1)


            // El cliente solo puede descargar el PDF si el pedido está COMPLETADO
            if ("completado".equals(estado)) {
                holder.btnAccionDinamica.setVisibility(View.VISIBLE);
                holder.btnAccionDinamica.setText("Descargar Boleta/PDF");
                holder.btnAccionDinamica.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
                holder.btnAccionDinamica.setOnClickListener(v -> listener.onDescargarPdf(pedido));
            }
        }
    }

    @Override
    public int getItemCount() {
        return pedidosList.size();
    }

    /**
     * Confirma el cambio de estado del pedido a "Completado" (Punto 3).
     * El cambio es irreversible.
     */
    private void confirmarCambioEstado(Pedido pedido) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmar Finalización de Pedido")
                .setMessage("¿Estás seguro de marcar el Pedido #" + pedido.getIdPedido() + " como COMPLETADO? Esta acción es irreversible y finaliza la venta.")
                .setPositiveButton("Sí, Completar", (dialog, which) -> {
                    // Ejecutar la actualización en un hilo secundario para no bloquear la UI
                    new Thread(() -> {
                        // El PedidoController debe manejar la actualización a "Completado"
                        boolean success = pedidoController.actualizarEstadoPedido(String.valueOf(pedido.getIdPedido()), "Completado");
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(context, "Pedido #" + pedido.getIdPedido() + " actualizado a COMPLETADO.", Toast.LENGTH_SHORT).show();
                                // Notificar a la Activity para recargar la lista y actualizar la vista
                                listener.onEstadoActualizado();
                            } else {
                                Toast.makeText(context, "Error al actualizar el estado del pedido.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Abre WhatsApp para contactar usando el número proporcionado (Punto 1).
     * Puede ser el cliente (para Admin/Vendedor) o el Admin (para Cliente).
     */
    private void contactar(String telefono) {
        if (telefono == null || telefono.isEmpty()) {
            Toast.makeText(context, "Número de teléfono no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Limpiar el número de formato, asegurando que el '+' al inicio se mantenga si existe
        String phoneNumber = telefono.startsWith("+")
                ? telefono.replaceAll("[^0-9+]", "")
                : telefono.replaceAll("[^0-9]", "");

        // La URL de WhatsApp usa el número sin el '+' si el país está incluido (como +51)
        String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Error abriendo WhatsApp. ¿Está instalado?", Toast.LENGTH_SHORT).show();
            Log.e("PedidoAdapter", "Error al abrir WhatsApp: " + e.getMessage());
        }
    }


    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvCodigo, tvCliente, tvTelefono, tvTotal, tvEstado;
        Button btnWhatsApp, btnAccionDinamica; // Usamos btnAccionDinamica para PDF o Completado

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asegúrate que estos IDs existan en list_item_pedido.xml
            tvCodigo = itemView.findViewById(R.id.tv_pedido_codigo);
            tvCliente = itemView.findViewById(R.id.tv_pedido_cliente);
            tvTelefono = itemView.findViewById(R.id.tv_pedido_telefono);
            tvTotal = itemView.findViewById(R.id.tv_pedido_total);
            tvEstado = itemView.findViewById(R.id.tv_pedido_estado);
            btnWhatsApp = itemView.findViewById(R.id.btn_contactar_whatsapp);
            btnAccionDinamica = itemView.findViewById(R.id.btn_accion_dinamica);
        }
    }
}