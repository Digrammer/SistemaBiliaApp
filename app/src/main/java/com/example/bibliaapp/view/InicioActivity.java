package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bibliaapp.R;
import com.google.android.material.navigation.NavigationView;

public class InicioActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // --- CÓDIGO CLAVE PARA TÍTULO EN NEGRO Y "INICIO" ---
        getSupportActionBar().setTitle("Inicio");

        // Establece el color del texto del título a Negro (usando tu recurso de colors.xml)
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        // Si la línea anterior falla, puedes usar la forma hexadecimal: toolbar.setTitleTextColor(0xFF000000);
        // --------------------------------------------------

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
        // Llamamos al archivo MenuUtil (que debe estar separado)
        MenuUtil.configurarMenuPorRol(this, navigationView);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Llamamos al archivo MenuUtil para la navegación
        boolean resultado = MenuUtil.manejarNavegacion(this, item.getItemId());
        drawerLayout.closeDrawer(GravityCompat.START);
        return resultado;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Muestra el menú opcional (ej. ícono de carrito si aplica)
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