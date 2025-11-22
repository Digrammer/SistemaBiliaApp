package com.example.bibliaapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.bibliaapp.R;
import com.google.android.material.navigation.NavigationView;

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

    private static final String SHARED_PREFS_NAME = "BibliaAppPrefs";
    private static final String KEY_LOGGED_USER_EMAIL = "loggedUserEmail";
    private static final String KEY_LOGGED_USER_ROL = "loggedUserRol";
    private static final String ROL_VISITANTE = "visitante";

    private static String getRolUsuario(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_LOGGED_USER_ROL, ROL_VISITANTE);
    }

    public static void configurarMenuPorRol(Context context, NavigationView navigationView) {

        Menu menu = navigationView.getMenu();

        // 1. OBTENER ROL
        String rol = getRolUsuario(context);
        boolean esLogueado = !rol.equals(ROL_VISITANTE);

        // 2. CONFIGURAR ÍTEMS COMUNES
        MenuItem navLogout = menu.findItem(R.id.nav_logout);
        MenuItem navPedidos = menu.findItem(R.id.nav_pedidos);

        // Items siempre visibles para todos
        menu.findItem(R.id.nav_inicio).setVisible(true);
        menu.findItem(R.id.nav_productos).setVisible(true);

        // Lógica de Inicio/Cierre de Sesión
        if (esLogueado) {
            if (navLogout != null) {
                navLogout.setTitle("Cerrar Sesión");
                navLogout.setIcon(android.R.drawable.ic_lock_power_off);
            }
            // Los pedidos solo son visibles si el usuario está logueado
            if (navPedidos != null) navPedidos.setVisible(true);
        } else {
            if (navLogout != null) {
                navLogout.setTitle("Iniciar Sesión");
                navLogout.setIcon(android.R.drawable.ic_menu_add);
            }
            // Los pedidos deben estar ocultos para visitantes
            if (navPedidos != null) navPedidos.setVisible(false);
        }

        // 3. APAGAR GRUPOS POR DEFECTO
        menu.setGroupVisible(R.id.grupo_gestion, false);
        menu.setGroupVisible(R.id.grupo_reportes, false);

        MenuItem navGestionUsuarios = menu.findItem(R.id.nav_crear_usuario);
        if (navGestionUsuarios != null) navGestionUsuarios.setVisible(false);

        // 4. ENCENDER SEGÚN ROL
        switch (rol) {
            case "vendedor":
                menu.setGroupVisible(R.id.grupo_gestion, true);
                break;

            case "administrador":
                menu.setGroupVisible(R.id.grupo_gestion, true);
                menu.setGroupVisible(R.id.grupo_reportes, true);
                if (navGestionUsuarios != null) navGestionUsuarios.setVisible(true);
                break;

            case "cliente":
                // Solo tiene acceso a los ítems del grupo_general
                break;

            case ROL_VISITANTE:
                // Solo tiene acceso a Inicio, Productos e Iniciar Sesión
                break;
        }
    }

    public static boolean manejarNavegacion(Activity activity, int id) {
        Intent intent = null;
        String rol = getRolUsuario(activity);

        // --- LOGOUT / LOGIN ---
        if (id == R.id.nav_logout) {
            if (rol.equals(ROL_VISITANTE)) {
                intent = new Intent(activity, LoginActivity.class);
            } else {
                // Cerrar sesión
                SharedPreferences prefs = activity.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString(KEY_LOGGED_USER_EMAIL, null).putString(KEY_LOGGED_USER_ROL, ROL_VISITANTE).apply();

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
            // Solo logueados (cliente, vendedor, administrador) pueden ver pedidos
            if (!rol.equals(ROL_VISITANTE)) {
                if (!(activity instanceof PedidosActivity)) {
                    intent = new Intent(activity, PedidosActivity.class);
                }
            } else {
                Toast.makeText(activity, "Necesitas iniciar sesión para ver tus pedidos.", Toast.LENGTH_SHORT).show();
            }
        }

        // --- Rutas Administrativas y de Venta ---
        else if (id == R.id.nav_ventas_fisicas) {
            if (rol.equals("vendedor") || rol.equals("administrador")) {
                if (!(activity instanceof VentasFisicasActivity)) {
                    intent = new Intent(activity, VentasFisicasActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_configuracion) { // Gestión de Productos
            if (rol.equals("vendedor") || rol.equals("administrador")) {
                if (!(activity instanceof GestionProductosActivity)) {
                    intent = new Intent(activity, GestionProductosActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_crear_usuario) { // Gestión de Usuarios
            if (rol.equals("administrador")) {
                if (!(activity instanceof GestionUsuariosActivity)) {
                    intent = new Intent(activity, GestionUsuariosActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_reportes) {
            if (rol.equals("administrador")) {
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