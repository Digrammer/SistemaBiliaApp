package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bibliaapp.R;
import com.google.android.material.navigation.NavigationView;

public class VentasFisicasActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvResumenCaja;
    private Button btnCobrarRapido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventas_fisicas);

        // 1. CONFIGURACIÓN DEL TOOLBAR Y EL TÍTULO
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Establecer el título de esta Activity
        getSupportActionBar().setTitle("Caja Rápida / Venta Física");

        // 2. CONFIGURACIÓN DEL DRAWERLAYOUT
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 3. CONFIGURACIÓN DEL ÍCONO DE MENÚ (HAMBURGUESA)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 4. LÓGICA ESPECÍFICA DE LA PANTALLA (Ajusta los IDs según tu layout)
        // Ejemplo de inicialización de vistas específicas de VentasFisicasActivity
        // tvResumenCaja = findViewById(R.id.tvResumenCaja);
        // btnCobrarRapido = findViewById(R.id.btnCobrarRapido);
        // ... (Tu código de botones, listeners, etc. va aquí) ...
    }

    // 5. COMPORTAMIENTO DE ROLES Y NAVEGACIÓN
    @Override
    protected void onResume() {
        super.onResume();
        MenuUtil.configurarMenuPorRol(this, navigationView);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean resultado = MenuUtil.manejarNavegacion(this, item.getItemId());
        drawerLayout.closeDrawer(GravityCompat.START);
        return resultado;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Opcional: Si necesitas el carrito en el Toolbar, agrega onCreateOptionsMenu y onOptionsItemSelected
    // (Igual que en InicioActivity, si aplica)
}