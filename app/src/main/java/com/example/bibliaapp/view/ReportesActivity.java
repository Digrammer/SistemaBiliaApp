package com.example.bibliaapp.view;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.DBHelper;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class ReportesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Variables de navegación
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Variables específicas de la Activity
    private ListView listViewReportes;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        // --- 1. CONFIGURACIÓN DEL MENÚ LATERAL ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Generación de Reportes"); // <-- TÍTULO

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // ----------------------------------------

        // --- 2. INICIALIZACIÓN DE VISTAS Y DB ---
        listViewReportes = findViewById(R.id.listViewReportes);
        db = new DBHelper(this);

        // --- 3. LÓGICA DE LA PANTALLA (SIN OPTIMIZACIÓN) ---
        cargarReportes();
    }

    private void cargarReportes() {
        ArrayList<String> lista = new ArrayList<>();
        // Llamada a DB en el hilo principal (pendiente de optimizar)
        Cursor cursor = db.getAllProductos();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
                    int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                    lista.add("Producto: " + nombre + " | Precio: S/ " + precio + " | Stock: " + stock);
                } catch (IllegalArgumentException e) {
                    // Manejo de error si alguna columna no existe.
                    lista.add("Error de columna en DB.");
                    break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "No se encontraron productos.", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);

        listViewReportes.setAdapter(adapter);
    }


    // --- 4. MÉTODOS DE COMPORTAMIENTO DE NAVEGACIÓN ---
    @Override
    protected void onResume() {
        super.onResume();
        MenuUtil.configurarMenuPorRol(this, navigationView);
        // Opcional: Recargar reportes si la DB pudo haber cambiado
        // cargarReportes();
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
}