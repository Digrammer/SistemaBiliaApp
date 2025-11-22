package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bibliaapp.R;
import com.example.bibliaapp.controller.UsuarioController;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtNombre, edtApellido, edtCorreo, edtContrasena, edtDni, edtTelefono, edtDireccion;
    private ImageView imgOjoContrasena;
    private Button btnRegistrar;
    private boolean passwordVisible = false;
    private UsuarioController usuarioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // references
        edtNombre = findViewById(R.id.edtNombre);
        edtApellido = findViewById(R.id.edtApellido);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtContrasena = findViewById(R.id.edtContrasena);
        edtDni = findViewById(R.id.edtDni);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtDireccion = findViewById(R.id.edtDireccion);
        imgOjoContrasena = findViewById(R.id.imgOjoContrasena);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        usuarioController = new UsuarioController(this);

        // filtro para permitir solo letras y espacios (Unicode) en nombre y apellido
        InputFilter letrasYEspacios = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char c = src.charAt(i);
                    if (!Character.isLetter(c) && c != ' ' && c != '\u00A0') {
                        return "";
                    }
                }
                return null;
            }
        };
        edtNombre.setFilters(new InputFilter[]{letrasYEspacios});
        edtApellido.setFilters(new InputFilter[]{letrasYEspacios});

        // ojo funcional (usa tu drawable ic_ojito)
        imgOjoContrasena.setOnClickListener(v -> {
            if (passwordVisible) {
                // ocultar
                edtContrasena.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                // mostrar
                edtContrasena.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            passwordVisible = !passwordVisible;
            edtContrasena.setSelection(edtContrasena.getText().length());
        });

        // registrar
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = edtNombre.getText().toString().trim();
        String apellido = edtApellido.getText().toString().trim();
        String correo = edtCorreo.getText().toString().trim();
        String contrasena = edtContrasena.getText().toString();
        String dni = edtDni.getText().toString().trim();
        String telefono = edtTelefono.getText().toString().trim();
        String direccion = edtDireccion.getText().toString().trim();

        // ---- VALIDACIONES ----

        // Nombre: requerido + solo letras y espacios
        if (TextUtils.isEmpty(nombre) || !nombre.matches("^[\\p{L} ]+$")) {
            edtNombre.setError("Nombre inválido (solo letras)");
            edtNombre.requestFocus();
            return;
        }

        // Apellido: requerido + solo letras y espacios
        if (TextUtils.isEmpty(apellido) || !apellido.matches("^[\\p{L} ]+$")) {
            edtApellido.setError("Apellido inválido (solo letras)");
            edtApellido.requestFocus();
            return;
        }

        // Correo: formato válido y dominio gmail.com obligatorio
        if (TextUtils.isEmpty(correo) || !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ||
                !correo.toLowerCase().endsWith("@gmail.com")) {
            edtCorreo.setError("Ingrese un correo Gmail válido (terminado en @gmail.com)");
            edtCorreo.requestFocus();
            return;
        }

        // Contraseña: letras + dígito + carácter especial (incluye '@'), mínimo 6
        if (TextUtils.isEmpty(contrasena) || !contrasena.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$")) {
            edtContrasena.setError("Contraseña: letras, números y un carácter especial (min 6)");
            edtContrasena.requestFocus();
            return;
        }

        // DNI: exactamente 8 dígitos numéricos
        if (TextUtils.isEmpty(dni) || !dni.matches("^\\d{8}$")) {
            edtDni.setError("DNI inválido (8 dígitos)");
            edtDni.requestFocus();
            return;
        }

        // Teléfono: exactamente 9 dígitos numéricos
        if (TextUtils.isEmpty(telefono) || !telefono.matches("^\\d{9}$")) {
            edtTelefono.setError("Teléfono inválido (9 dígitos)");
            edtTelefono.requestFocus();
            return;
        }

        // Dirección: opcional (no validamos)

        // ---- intento de registro ----
        boolean registrado = usuarioController.registrar(nombre, apellido, correo, contrasena, dni, telefono, direccion);

        if (registrado) {
            Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
            // opcional: regresar a Login (ya pueden loguearse)
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            edtCorreo.setError("El correo ya está registrado");
            edtCorreo.requestFocus();
            Toast.makeText(this, "No se pudo registrar: correo ya existe", Toast.LENGTH_SHORT).show();
        }
    }
}
