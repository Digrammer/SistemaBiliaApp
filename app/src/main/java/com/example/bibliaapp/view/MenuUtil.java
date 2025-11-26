package com.example.bibliaapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.core.view.GravityCompat; // Importación necesaria si usas DrawerLayout/NavigationView
import androidx.drawerlayout.widget.DrawerLayout; // Importación necesaria si usas DrawerLayout/NavigationView

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

    private static final String ROL_VISITANTE = "visitante";
    private static final String ROL_ADMINISTRADOR = "administrador";
    private static final String ROL_VENDEDOR = "vendedor";
    private static final String ROL_CLIENTE = "cliente";


    private static String getRolUsuario(Context context) {
        return SharedPreferencesManager.getInstance(context).getUserRol();
    }

    public static void configurarMenuPorRol(Context context, NavigationView navigationView) {

        Menu menu = navigationView.getMenu();

        // 1. OBTENER ROL Y ESTADO DE SESIÓN USANDO EL GESTOR
        String rol = getRolUsuario(context);
        // Consideramos logueado a cualquiera que no sea 'visitante'
        boolean esLogueado = !rol.equalsIgnoreCase(ROL_VISITANTE);

        // 2. CONFIGURAR ÍTEMS COMUNES (Logout)
        MenuItem navLogout = menu.findItem(R.id.nav_logout);

        // Items siempre visibles para todos
        menu.findItem(R.id.nav_inicio).setVisible(true);
        menu.findItem(R.id.nav_productos).setVisible(true);

        // Lógica de Inicio/Cierre de Sesión
        if (navLogout != null) {
            if (esLogueado) {
                navLogout.setTitle("Cerrar Sesión");
                // Usar R.drawable.ic_flecha_atras_negra o uno similar si existe
                // navLogout.setIcon(R.drawable.ic_flecha_atras_negra);
                // Usando un default de Android si no existe R.drawable.ic_lock_power_off
                navLogout.setIcon(android.R.drawable.ic_lock_power_off);
            } else {
                navLogout.setTitle("Iniciar Sesión");
                // Usando un default de Android si no existe R.drawable.ic_menu_add
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


        // 4. ENCENDER SEGÚN ROL
        switch (rol.toLowerCase()) {
            case ROL_ADMINISTRADOR:
                // Administrador ve TODO
                menu.setGroupVisible(R.id.grupo_gestion, true);
                menu.setGroupVisible(R.id.grupo_reportes, true);
                if (navGestionUsuarios != null) navGestionUsuarios.setVisible(true);
                if (navPedidos != null) navPedidos.setVisible(true);
                break;

            case ROL_VENDEDOR:
                // Vendedor ve Gestión (Ventas/Productos) y Pedidos
                menu.setGroupVisible(R.id.grupo_gestion, true);
                if (navPedidos != null) navPedidos.setVisible(true);
                break;

            case ROL_CLIENTE:
                // Cliente solo ve Pedidos (y los comunes: Inicio, Productos)
                if (navPedidos != null) navPedidos.setVisible(true);
                break;

            case ROL_VISITANTE:
                // Visitante solo ve Inicio y Productos.
                // Aunque el menú tenga el ítem Pedidos, lo ocultamos para el visitante
                // por la validación de rol en el manejador de navegación.
                break;
        }
    }

    /**
     * Maneja la selección de ítems en el menú lateral.
     * NOTA: Este método asume que el drawerLayout ya está cerrado por la actividad que llama.
     */
    public static boolean manejarNavegacion(Activity activity, int id) {
        Intent intent = null;
        String rol = getRolUsuario(activity);

        // --- LOGOUT / LOGIN ---
        if (id == R.id.nav_logout) {
            if (rol.equalsIgnoreCase(ROL_VISITANTE)) {
                // Si es visitante, va a Login para iniciar sesión
                intent = new Intent(activity, LoginActivity.class);
            } else {
                // Si está logueado, cierra sesión
                SharedPreferencesManager.getInstance(activity).clearUserSession(); // *** CORRECCIÓN 1 ***

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
            if (!rol.equalsIgnoreCase(ROL_VISITANTE)) {
                if (!(activity instanceof PedidosActivity)) {
                    intent = new Intent(activity, PedidosActivity.class);
                }
            } else {
                Toast.makeText(activity, "Necesitas iniciar sesión para ver tus pedidos.", Toast.LENGTH_SHORT).show();
            }
        }

        // --- Rutas Administrativas y de Venta ---
        else if (id == R.id.nav_ventas) { // *** CORRECCIÓN 2: nav_ventas ***
            if (rol.equalsIgnoreCase(ROL_VENDEDOR) || rol.equalsIgnoreCase(ROL_ADMINISTRADOR)) {
                if (!(activity instanceof VentasFisicasActivity)) {
                    intent = new Intent(activity, VentasFisicasActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_configuracion) { // Gestión de Productos
            if (rol.equalsIgnoreCase(ROL_VENDEDOR) || rol.equalsIgnoreCase(ROL_ADMINISTRADOR)) {
                if (!(activity instanceof GestionProductosActivity)) {
                    intent = new Intent(activity, GestionProductosActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_crear_usuario) { // Gestión de Usuarios
            if (rol.equalsIgnoreCase(ROL_ADMINISTRADOR)) {
                if (!(activity instanceof GestionUsuariosActivity)) {
                    intent = new Intent(activity, GestionUsuariosActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_reportes) {
            if (rol.equalsIgnoreCase(ROL_ADMINISTRADOR)) {
                if (!(activity instanceof ReportesActivity)) {
                    intent = new Intent(activity, ReportesActivity.class);
                }
            } else {
                Toast.makeText(activity, "Acceso denegado.", Toast.LENGTH_SHORT).show();
            }
        }

        // --- Carrito (Desde el icono de la Toolbar, R.id.action_cart) ---
        // Nota: Asegúrate de que R.id.action_cart exista en tu archivo de menú de la toolbar.
        else if (id == R.id.action_cart) {
            intent = new Intent(activity, CarritoActivity.class);
        }


        if (intent != null) {
            // Usamos CLEAR_TOP, pero permitimos que la actividad que llama
            // decida si necesita terminar (finish) o no.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }

        return true;
    }
}