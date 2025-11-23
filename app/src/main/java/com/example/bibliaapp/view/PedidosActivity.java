package com.example.bibliaapp.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.view.adapter.PedidoAdapter;
import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Pedido;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class PedidosActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "PedidosActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerViewPedidos;
    private TextView tvNoPedidos;
    private PedidoAdapter pedidoAdapter;
    private DBHelper dbHelper;

    // 🛑 CONSTANTES CORREGIDAS (Coinciden con LoginActivity)
    public static final String SHARED_PREFS_NAME = "BibliaAppPrefs";
    public static final String KEY_LOGGED_USER_EMAIL = "loggedUserEmail";
    public static final String KEY_LOGGED_USER_ROL = "loggedUserRol";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        dbHelper = new DBHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Pedidos");
        }

        // Inicializar vistas
        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        tvNoPedidos = findViewById(R.id.tvNoPedidos);
        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));

        // Configuración del Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cargar los pedidos cada vez que la actividad es visible
        cargarPedidos();
        MenuUtil.configurarMenuPorRol(this, navigationView);
    }

    private void cargarPedidos() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String correoUsuario = prefs.getString(KEY_LOGGED_USER_EMAIL, null);
        String rolUsuario = prefs.getString(KEY_LOGGED_USER_ROL, "visitante");

        // 1. Validación: Si es visitante o no hay correo, no mostrar pedidos
        if (correoUsuario == null || "visitante".equalsIgnoreCase(rolUsuario)) {
            tvNoPedidos.setText("Debe iniciar sesión con una cuenta para ver el historial.");
            tvNoPedidos.setVisibility(View.VISIBLE);
            recyclerViewPedidos.setVisibility(View.GONE);
            return;
        }

        // 2. Obtener el ID real del usuario desde la BD usando el correo
        int idUsuario = -1;
        Cursor cursorUser = dbHelper.getUsuarioByCorreo(correoUsuario);
        if (cursorUser != null && cursorUser.moveToFirst()) {
            idUsuario = cursorUser.getInt(cursorUser.getColumnIndexOrThrow("id_usuario"));
            cursorUser.close();
        }

        if (idUsuario == -1) {
            Toast.makeText(this, "Error: Usuario no identificado.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Pedido> listaPedidos = new ArrayList<>();
        Cursor cursor = null;

        try {
            // 3. Lógica de Roles
            if ("administrador".equalsIgnoreCase(rolUsuario)) {
                // El Administrador ve TODOS los pedidos (ordenados por ID descendente)
                cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM pedidos ORDER BY id_pedido DESC", null);
            } else {
                // Cliente o Vendedor ven solo SUS pedidos
                cursor = dbHelper.getPedidosByUsuario(idUsuario);
            }

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Mapear datos de la BD
                    int id_pedido_bd = cursor.getInt(cursor.getColumnIndexOrThrow("id_pedido"));
                    String codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo")); // ID de 6 dígitos
                    int id_usuario_db = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario"));
                    double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                    String estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"));
                    String metodoPago = cursor.getString(cursor.getColumnIndexOrThrow("metodo_pago"));
                    String telefonoContacto = cursor.getString(cursor.getColumnIndexOrThrow("telefono_contacto"));

                    // Usamos el constructor COMPLETO de Pedido para pasarle el TOTAL directamente desde la BD
                    Pedido pedido = new Pedido(
                            Integer.parseInt(codigo),       // ID Pedido (código)
                            "Cliente ID: " + id_usuario_db, // Nombre Cliente
                            telefonoContacto,               // Teléfono
                            "N/A",                          // Dirección
                            estado,                         // Estado
                            metodoPago,                     // Tipo Entrega / Método Pago
                            total,                          // Total (importante pasarlo aquí)
                            new ArrayList<>()               // Lista vacía para la vista de lista
                    );

                    listaPedidos.add(pedido);

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al cargar pedidos: " + e.getMessage());
            Toast.makeText(this, "Error al cargar pedidos.", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Actualizar UI
        if (listaPedidos.isEmpty()) {
            tvNoPedidos.setText(rolUsuario.equalsIgnoreCase("administrador") ? "No hay pedidos en el sistema." : "Aún no tienes pedidos.");
            tvNoPedidos.setVisibility(View.VISIBLE);
            recyclerViewPedidos.setVisibility(View.GONE);
        } else {
            tvNoPedidos.setVisibility(View.GONE);
            recyclerViewPedidos.setVisibility(View.VISIBLE);

            // Inicializar adaptador
            pedidoAdapter = new PedidoAdapter(this, listaPedidos);
            recyclerViewPedidos.setAdapter(pedidoAdapter);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean resultado = MenuUtil.manejarNavegacion(this, item.getItemId());
        drawerLayout.closeDrawer(GravityCompat.START);
        return resultado;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            startActivity(new Intent(this, CarritoActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}