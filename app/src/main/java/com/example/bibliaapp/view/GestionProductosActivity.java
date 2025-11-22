package com.example.bibliaapp.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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
import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Producto;
import com.example.bibliaapp.view.adapter.GestionProductoAdapter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class GestionProductosActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Variable declarada usando el nombre del ID real en el XML: rvProductosGestion
    private RecyclerView rvProductosGestion;
    private GestionProductoAdapter adapter;
    private List<Producto> listaProductos;
    private DBHelper dbHelper;
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_productos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Gestión de Productos");

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(this);

        dbHelper = new DBHelper(this);
        // Asignación de la vista usando el ID correcto del XML (R.id.rvProductosGestion)
        rvProductosGestion = findViewById(R.id.rvProductosGestion);
        rvProductosGestion.setLayoutManager(new LinearLayoutManager(this));

        cargarProductos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuUtil.configurarMenuPorRol(this, navView);
        cargarProductos();
    }

    private void cargarProductos() {
        listaProductos = new ArrayList<>();
        Cursor cursor = dbHelper.getAllProductos();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Producto p = cursorToProducto(cursor);
                if (p != null) {
                    listaProductos.add(p);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (adapter == null) {
            adapter = new GestionProductoAdapter(this, listaProductos);
            rvProductosGestion.setAdapter(adapter);
        } else {
            adapter.updateList(listaProductos);
        }
    }

    private Producto cursorToProducto(Cursor cursor) {
        try {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_producto"));
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
            String imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
            int idCategoria = cursor.getInt(cursor.getColumnIndexOrThrow("id_categoria"));
            int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
            return new Producto(id, nombre, precio, imagen, idCategoria, stock);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean resultado = MenuUtil.manejarNavegacion(this, item.getItemId());
        drawerLayout.closeDrawer(GravityCompat.START);
        return resultado;
    }
}