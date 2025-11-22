package com.example.bibliaapp.view;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {

    private EditText edtCorreo, edtPassword;
    private Button btnLogin, btnVisitante, btnRegistrar;
    private boolean passwordVisible = false;
    private DBHelper dbHelper; // Mantenemos la referencia global
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public static final String SHARED_PREFS_NAME = "BibliaAppPrefs";
    public static final String KEY_LOGGED_USER_EMAIL = "loggedUserEmail";
    public static final String KEY_LOGGED_USER_ROL = "loggedUserRol";
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

        // 1. Deshabilitar botones inmediatamente para prevenir interacción antes de la carga
        habilitarBotones(false);

        // 2. 🌟 SOLUCIÓN CLAVE: Inicializar DBHelper en un HILO DE FONDO 🌟
        new Thread(() -> {
            try {
                // Esta línea fuerza la creación de la DB (operación pesada) SIN bloquear la UI.
                dbHelper = new DBHelper(this);
            } catch (Exception e) {
                // Captura cualquier error de inicialización de la DB para evitar un CRASH.
                e.printStackTrace();
                uiHandler.post(() -> {
                    Toast.makeText(this, "ERROR CRÍTICO: Fallo al inicializar la base de datos.", Toast.LENGTH_LONG).show();
                });
                return;
            }

            // Volvemos al hilo de la UI para habilitar botones
            uiHandler.post(() -> {
                habilitarBotones(true);

            });
        }).start();


        // 3. Configuración de Listeners (Se mantienen igual)
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

        // Estos listeners ahora verifican si dbHelper es null ANTES de usarse
        btnLogin.setOnClickListener(v -> validarLogin());
        btnVisitante.setOnClickListener(v -> manejarVisitante());
        btnRegistrar.setOnClickListener(v -> manejarRegistro());
    }

    private void manejarVisitante() {
        if (dbHelper != null) {
            guardarSesion(ROL_VISITANTE, ROL_VISITANTE);
            irAProductos();
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


    // Método auxiliar para habilitar/deshabilitar botones
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

        // La validación de login debe ser rápida, pero si es lenta,
        // también debería hacerse en un hilo. Por ahora la dejamos aquí.
        String rolObtenido = dbHelper.validateAndGetRol(correo, pass);

        if (rolObtenido != null) {
            guardarSesion(correo, rolObtenido);
            Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();
            irAProductos();
        } else {
            Toast.makeText(this, "Datos incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarSesion(String correo, String rol) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_LOGGED_USER_EMAIL, correo).putString(KEY_LOGGED_USER_ROL, rol).apply();
    }

    private void irAProductos() {
        Intent intent = new Intent(LoginActivity.this, ProductosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}