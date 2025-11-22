package com.example.bibliaapp.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
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

public class GestionUsuariosActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Variables de navegación
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Variables específicas de la Activity
    private EditText edtNombre, edtApellido, edtCorreo, edtPass, edtDni, edtTelefono, edtDireccion, edtRol;
    private Button btnCrear;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        // --- 1. CONFIGURACIÓN DEL MENÚ LATERAL ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Administración de Usuarios"); // <-- TÍTULO

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
        edtNombre = findViewById(R.id.edtNombre);
        edtApellido = findViewById(R.id.edtApellido);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtPass = findViewById(R.id.edtPass);
        edtDni = findViewById(R.id.edtDni);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtDireccion = findViewById(R.id.edtDireccion);
        edtRol = findViewById(R.id.edtRol);
        btnCrear = findViewById(R.id.btnCrear);

        db = new DBHelper(this);

        // --- 3. LÓGICA DE LA PANTALLA (SIN OPTIMIZACIÓN) ---
        btnCrear.setOnClickListener(v -> {
            String n = edtNombre.getText().toString().trim();
            String a = edtApellido.getText().toString().trim();
            String c = edtCorreo.getText().toString().trim();
            String p = edtPass.getText().toString().trim();
            String dni = edtDni.getText().toString().trim();
            String tel = edtTelefono.getText().toString().trim();
            String dir = edtDireccion.getText().toString().trim();
            String rol = edtRol.getText().toString().trim();

            if (TextUtils.isEmpty(n) || TextUtils.isEmpty(c) || TextUtils.isEmpty(p) || TextUtils.isEmpty(rol)) {
                Toast.makeText(this, "Complete campos obligatorios.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Llamada a DB en el hilo principal (pendiente de optimizar)
            long r = db.insertUsuario(n, a, c, p, dni, tel, dir, rol);

            if (r > 0) {
                Toast.makeText(this, "Usuario creado exitosamente.", Toast.LENGTH_SHORT).show();
                edtNombre.setText(""); edtApellido.setText(""); edtCorreo.setText(""); edtPass.setText("");
                edtDni.setText(""); edtTelefono.setText(""); edtDireccion.setText(""); edtRol.setText("");
            } else {
                Toast.makeText(this, "Error al crear usuario.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- 4. MÉTODOS DE COMPORTAMIENTO DE NAVEGACIÓN ---
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
}