package com.example.bibliaapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.SharedPreferencesManager; // CLAVE: Importamos el gestor centralizado
import com.google.android.material.navigation.NavigationView;

// Importaciones de Activities (se mantienen igual)
import com.example.bibliaapp.view.GestionProductosActivity;
import com.example.bibliaapp.view.CarritoActivity;
import com.example.bibliaapp.view.GestionUsuariosActivity;
import com.example.bibliaapp.view.InicioActivity;
import com.example.bibliaapp.view.LoginActivity;
import com.example.bibliaapp.view.PedidosActivity;
import com.example.bibliaapp.view.ReportesActivity;
import com.example.bibliaapp.view.VentasFisicasActivity;
import com.example.bibliaapp.view.ProductosActivity;

public class MenuUtil {

    // Solo mantenemos la constante del rol de visitante
    private static final String ROL_VISITANTE = "visitante";

    // Reemplaza la lógica de lectura manual de sesión por el gestor
    private static String getRolUsuario(Context context) {
        return SharedPreferencesManager.getInstance(context).getUserRol();
    }

    public static void configurarMenuPorRol(Context context, NavigationView navigationView) {

        Menu menu = navigationView.getMenu();

        // 1. OBTENER ROL Y ESTADO DE SESIÓN USANDO EL GESTOR
        String rol = getRolUsuario(context);
        // Consideramos logueado a cualquiera que no sea 'visitante'
        boolean esLogueado = !rol.equalsIgnoreCase(ROL_VISITANTE);

        // 2. CONFIGURAR ÍTEMS COMUNES
        MenuItem navLogout = menu.findItem(R.id.nav_logout);

        // Items siempre visibles para todos
        menu.findItem(R.id.nav_inicio).setVisible(true);
        menu.findItem(R.id.nav_productos).setVisible(true);

        // Lógica de Inicio/Cierre de Sesión
        if (esLogueado) {
            if (navLogout != null) {
                navLogout.setTitle("Cerrar Sesión");
                navLogout.setIcon(android.R.drawable.ic_lock_power_off);
            }
        } else {
            if (navLogout != null) {
                navLogout.setTitle("Iniciar Sesión");
                navLogout.setIcon(android.R.drawable.ic_menu_add);
            }
        }

        // 3. OCULTAR TODOS LOS GRUPOS POR DEFECTO
        menu.setGroupVisible(R.id.grupo_gestion, false);
        menu.setGroupVisible(R.id.grupo_reportes, false);

        // Elementos que deben manejarse individualmente
        MenuItem navGestionUsuarios = menu.findItem(R.id.nav_crear_usuario);
        if (navGestionUsuarios != null) navGestionUsuarios.setVisible(false);
        MenuItem navPedidos = menu.findItem(R.id.nav_pedidos);
        if (navPedidos != null) navPedidos.setVisible(false);


        // 4. ENCENDER SEGÚN ROL Y NUEVO REQUISITO DE PEDIDOS
        switch (rol.toLowerCase()) {
            case "administrador":
                // Administrador ve TODO
                menu.setGroupVisible(R.id.grupo_gestion, true);
                menu.setGroupVisible(R.id.grupo_reportes, true);
                if (navGestionUsuarios != null) navGestionUsuarios.setVisible(true);
                if (navPedidos != null) navPedidos.setVisible(true);
                break;

            case "vendedor":
                // Vendedor ve Gestión (Ventas/Productos) y Pedidos
                menu.setGroupVisible(R.id.grupo_gestion, true);
                if (navPedidos != null) navPedidos.setVisible(true);
                break;

            case "cliente":
                // Cliente solo ve Pedidos (y los comunes: Inicio, Productos)
                if (navPedidos != null) navPedidos.setVisible(true);
                break;

            case ROL_VISITANTE:
                // Visitante solo ve Inicio, Productos y Pedidos (Según tu requisito)
                if (navPedidos != null) navPedidos.setVisible(true);
                break;
        }
    }

    public static boolean manejarNavegacion(Activity activity, int id) {
        Intent intent = null;
        String rol = getRolUsuario(activity);

        // --- LOGOUT / LOGIN ---
        if (id == R.id.nav_logout) {
            if (rol.equalsIgnoreCase(ROL_VISITANTE)) {
                // Si es visitante, va a Login
                intent = new Intent(activity, LoginActivity.class);
            } else {
                // Si está logueado, cierra sesión usando el gestor centralizado
                SharedPreferencesManager.getInstance(activity).clearSession();

                intent = new Intent(activity, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
                return true;
            }
        }

        // --- Rutas Comunes ---
        else if (id == R.id.nav_inicio) {
            if (!(activity instanceof InicioActivity)) {
                intent = new Intent(activity, InicioActivity.class);
            }
        }
        else if (id == R.id.nav_productos) {
            if (!(activity instanceof ProductosActivity)) {
                intent = new Intent(activity, ProductosActivity.class);
            }
        }

        // --- Rutas Restringidas (Pedidos) ---
        else if (id == R.id.nav_pedidos) {
            // Permitimos ver pedidos si es cualquier rol logueado (cliente, vendedor, administrador)
            // Ya que el visitante no tiene ID, lo dejamos restringido a logueados por seguridad,
            // aunque el menú esté visible.
            if (!rol.equalsIgnoreCase(ROL_VISITANTE)) {
                if (!(activity instanceof PedidosActivity)) {
                    intent = new Intent(activity, PedidosActivity.class);
                }
            } else {
                Toast.makeText(activity, "Necesitas iniciar sesión para ver tus pedidos.", Toast.LENGTH_SHORT).show();
            }
        }

        // --- Rutas Administrativas y de Venta ---
        else if (id == R.id.nav_ventas_fisicas) {
            if (rol.equalsIgnoreCase("vendedor") || rol.equalsIgnoreCase("administrador")) {
                if (!(activity instanceof VentasFisicasActivity)) {
                    intent = new Intent(activity, VentasFisicasActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_configuracion) { // Gestión de Productos
            if (rol.equalsIgnoreCase("vendedor") || rol.equalsIgnoreCase("administrador")) {
                if (!(activity instanceof GestionProductosActivity)) {
                    intent = new Intent(activity, GestionProductosActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_crear_usuario) { // Gestión de Usuarios
            if (rol.equalsIgnoreCase("administrador")) {
                if (!(activity instanceof GestionUsuariosActivity)) {
                    intent = new Intent(activity, GestionUsuariosActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_reportes) {
            if (rol.equalsIgnoreCase("administrador")) {
                if (!(activity instanceof ReportesActivity)) {
                    intent = new Intent(activity, ReportesActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }

        // --- Carrito (Desde el icono de la Toolbar, R.id.action_cart) ---
        else if (id == R.id.action_cart) {
            intent = new Intent(activity, CarritoActivity.class);
        }


        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }

        return true;
    }
}