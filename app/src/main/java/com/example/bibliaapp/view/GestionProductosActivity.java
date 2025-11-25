package com.example.bibliaapp.view;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.bibliaapp.model.SharedPreferencesManager;
import com.example.bibliaapp.view.adapter.GestionProductoAdapter;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GestionProductosActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView rvProductosGestion;
    private GestionProductoAdapter adapter;
    private List<Producto> listaProductos;
    private DBHelper dbHelper;
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    // Elementos del Formulario
    private EditText edtNombre, edtPrecio, edtStock;
    private ImageView ivProductoPreview;
    private Button btnSeleccionarImagen, btnAgregar;
    private LinearLayout llCategoriasContainer;
    private RadioGroup radioGroupCategorias;

    private Uri imagenUriSeleccionada = null;
    private String rutaImagenFinal = null;

    private final ActivityResultLauncher<Intent> launcherGaleria = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uriOrigen = result.getData().getData();
                    if (uriOrigen != null) {
                        ivProductoPreview.setImageURI(uriOrigen);
                        ivProductoPreview.setVisibility(View.VISIBLE);
                        rutaImagenFinal = guardarImagenEnInterna(uriOrigen);

                        if (rutaImagenFinal != null) {
                            imagenUriSeleccionada = uriOrigen;
                        } else {
                            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- INICIO: BLINDAJE DE SEGURIDAD POR ROL ---
        String rol = SharedPreferencesManager.getInstance(this).getUserRol();
        if (!rol.equals("administrador") && !rol.equals("vendedor")) {
            Toast.makeText(this, "Acceso no autorizado.", Toast.LENGTH_LONG).show();
            // Redirigir a la pantalla principal (ProductosActivity)
            Intent intent = new Intent(this, ProductosActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return; // Termina la ejecución de onCreate
        }
        // --- FIN: BLINDAJE DE SEGURIDAD ---


        setContentView(R.layout.activity_gestion_productos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Gestión de Productos");

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // ** CORRECCIÓN CLAVE 1: Configurar el menú AHORA **
        MenuUtil.configurarMenuPorRol(this, navView);

        navView.setNavigationItemSelectedListener(this);

        dbHelper = new DBHelper(this);

        // Aquí se llama al método
        inicializarVistas();

        rvProductosGestion = findViewById(R.id.rvProductosGestion);
        rvProductosGestion.setLayoutManager(new LinearLayoutManager(this));

        cargarCategoriasEnFormulario();
        cargarProductos();

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnAgregar.setOnClickListener(v -> agregarProducto());
    }

    // *** MÉTODO DE INICIALIZACIÓN RESTAURADO ***
    private void inicializarVistas() {
        edtNombre = findViewById(R.id.edtNombre);
        // edtDescripcion ELIMINADO
        edtPrecio = findViewById(R.id.edtPrecio);
        edtStock = findViewById(R.id.edtStock);
        ivProductoPreview = findViewById(R.id.ivProductoPreview);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnAgregar = findViewById(R.id.btnAgregar);
        llCategoriasContainer = findViewById(R.id.llCategoriasContainer);

        radioGroupCategorias = new RadioGroup(this);
        llCategoriasContainer.addView(radioGroupCategorias);
    }
    // ********************************************

    @Override
    protected void onResume() {
        super.onResume();

        // Vuelve a chequear permisos en onResume por si algo cambió fuera.
        String rol = SharedPreferencesManager.getInstance(this).getUserRol();
        if (!rol.equals("administrador") && !rol.equals("vendedor")) {
            // Ya se maneja en onCreate, pero este check es extra seguro.
            finish(); // Cierra si el rol cambió mientras la app estaba en segundo plano.
            return;
        }

        cargarProductos();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcherGaleria.launch(intent);
    }

    private String guardarImagenEnInterna(Uri uriOrigen) {
        try {
            String nombreArchivo = "img_" + UUID.randomUUID().toString() + ".jpg";
            InputStream inputStream = getContentResolver().openInputStream(uriOrigen);
            File archivoDestino = new File(getFilesDir(), nombreArchivo);
            OutputStream outputStream = new FileOutputStream(archivoDestino);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return archivoDestino.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void cargarCategoriasEnFormulario() {
        List<String> categorias = dbHelper.getAllCategorias();
        radioGroupCategorias.removeAllViews();
        for (String cat : categorias) {
            RadioButton rb = new RadioButton(this);
            rb.setText(cat);
            rb.setTextColor(getResources().getColor(R.color.black));
            radioGroupCategorias.addView(rb);
        }
        if (radioGroupCategorias.getChildCount() > 0) {
            ((RadioButton) radioGroupCategorias.getChildAt(0)).setChecked(true);
        }
    }

    private void agregarProducto() {
        String nombre = edtNombre.getText().toString().trim();
        String precioStr = edtPrecio.getText().toString().trim();
        String stockStr = edtStock.getText().toString().trim();

        if (nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rutaImagenFinal == null) {
            Toast.makeText(this, "Seleccione una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = radioGroupCategorias.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Seleccione una categoría.", Toast.LENGTH_SHORT).show();
            return;
        }

        // CORRECCIÓN: Usar la vista para obtener el texto, no el ID de grupo
        RadioButton rbSelected = findViewById(radioGroupCategorias.getCheckedRadioButtonId());
        if (rbSelected == null) {
            Toast.makeText(this, "Error al obtener la categoría.", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoriaNombre = rbSelected.getText().toString();
        int idCategoria = dbHelper.getCategoriaIdByNombre(categoriaNombre);

        try {
            double precio = Double.parseDouble(precioStr);
            int stock = Integer.parseInt(stockStr);

            long id = dbHelper.insertProducto(nombre, precio, rutaImagenFinal, idCategoria, stock);

            if (id > 0) {
                Toast.makeText(this, "Producto agregado.", Toast.LENGTH_SHORT).show();
                limpiarFormulario();
                cargarProductos();
            } else {
                Toast.makeText(this, "Error al guardar.", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valores numéricos inválidos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarFormulario() {
        edtNombre.setText("");
        edtPrecio.setText("");
        edtStock.setText("");
        ivProductoPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        rutaImagenFinal = null;
        imagenUriSeleccionada = null;
        if (radioGroupCategorias.getChildCount() > 0) {
            ((RadioButton) radioGroupCategorias.getChildAt(0)).setChecked(true);
        }
    }

    private void cargarProductos() {
        listaProductos = new ArrayList<>();
        Cursor cursor = dbHelper.getAllProductos();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Producto p = cursorToProducto(cursor);
                if (p != null) listaProductos.add(p);
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
            return new Producto(id, nombre, precio, imagen, stock, idCategoria);
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