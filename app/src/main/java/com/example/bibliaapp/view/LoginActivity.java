package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.SharedPreferencesManager;

// *************************************************************
// ** IMPORTACIONES CLAVE PARA LA REDIRECCIÓN POR ROL (CRASH FIX) **
// *************************************************************
import com.example.bibliaapp.view.ProductosActivity;
import com.example.bibliaapp.view.GestionProductosActivity; // <<-- IMPORTACIÓN REQUERIDA
import com.example.bibliaapp.view.VentasFisicasActivity;     // <<-- IMPORTACIÓN REQUERIDA
// *************************************************************


public class LoginActivity extends AppCompatActivity {

    private EditText edtCorreo, edtPassword;
    private Button btnLogin, btnVisitante, btnRegistrar;
    private boolean passwordVisible = false;
    private DBHelper dbHelper;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public static final String ROL_VISITANTE = "visitante";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtCorreo = findViewById(R.id.edtCorreo);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnVisitante = findViewById(R.id.btnVisitante);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // 1. Deshabilitar botones inmediatamente
        habilitarBotones(false);

        // 2. Inicializar DBHelper en un HILO DE FONDO
        new Thread(() -> {
            try {
                dbHelper = new DBHelper(this);
            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> {
                    Toast.makeText(this, "ERROR CRÍTICO: Fallo al inicializar la base de datos.", Toast.LENGTH_LONG).show();
                });
                return;
            }
            uiHandler.post(() -> habilitarBotones(true));
        }).start();


        // 3. Configuración de Listeners
        edtPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (edtPassword.getCompoundDrawables()[DRAWABLE_END] != null) {
                    if (event.getRawX() >= (edtPassword.getRight() - edtPassword.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                        passwordVisible = !passwordVisible;
                        int tipo = passwordVisible ?
                                (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) :
                                (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        edtPassword.setInputType(tipo);
                        edtPassword.setSelection(edtPassword.getText().length());
                        return true;
                    }
                }
            }
            return false;
        });

        btnLogin.setOnClickListener(v -> validarLogin());
        btnVisitante.setOnClickListener(v -> manejarVisitante());
        btnRegistrar.setOnClickListener(v -> manejarRegistro());
    }

    private void manejarVisitante() {
        if (dbHelper != null) {
            // Visitante tiene ID -1
            guardarSesionCompleta(-1, ROL_VISITANTE, ROL_VISITANTE);
            irAActividad(ProductosActivity.class); // Visitante va directo al catálogo
        } else {
            Toast.makeText(this, "Sistema no inicializado. Espere un momento.", Toast.LENGTH_SHORT).show();
        }
    }

    private void manejarRegistro() {
        if (dbHelper != null) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        } else {
            Toast.makeText(this, "Sistema no inicializado. Espere un momento.", Toast.LENGTH_SHORT).show();
        }
    }

    private void habilitarBotones(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnVisitante.setEnabled(enabled);
        btnRegistrar.setEnabled(enabled);
    }

    private void validarLogin() {
        if (dbHelper == null) {
            Toast.makeText(this, "Sistema no inicializado. Espere un momento.", Toast.LENGTH_SHORT).show();
            return;
        }

        String correo = edtCorreo.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) { edtCorreo.setError("Requerido"); return; }
        if (TextUtils.isEmpty(pass)) { edtPassword.setError("Requerido"); return; }

        String rolObtenido = dbHelper.validateAndGetRol(correo, pass);

        if (rolObtenido != null) {
            int userId = dbHelper.getUserIdByEmail(correo);
            String rolLower = rolObtenido.toLowerCase();

            if (userId != -1) {
                // 1. Guardamos ID, Correo y Rol
                guardarSesionCompleta(userId, correo, rolLower);
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();

                // 2. Redirección basada en el Rol
                if (rolLower.equals("administrador")) {
                    // Administrador va a Gestión de Productos (Dashboard principal de gestión)
                    irAActividad(GestionProductosActivity.class);
                } else if (rolLower.equals("vendedor")) {
                    // Vendedor va a Ventas (Dashboard principal de ventas)
                    irAActividad(VentasFisicasActivity.class);
                } else {
                    // Cliente va a Productos
                    irAActividad(ProductosActivity.class);
                }

            } else {
                Toast.makeText(this, "Error de sistema: ID no encontrado", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Datos incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarSesionCompleta(int id, String correo, String rol) {
        SharedPreferencesManager.getInstance(this).saveUserSession(id, correo, rol);
    }

    private void irAActividad(Class<?> activityClass) {
        Intent intent = new Intent(LoginActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}