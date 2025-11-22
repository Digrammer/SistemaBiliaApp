package com.example.bibliaapp.view;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Producto;
import com.example.bibliaapp.view.adapter.ProductoCatalogoAdapter;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class ProductosActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private LinearLayout llCategorias;
    private RecyclerView rvProductos;
    private DBHelper dbHelper;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ProductoCatalogoAdapter adapter;
    private String categoriaActual = "Todas las categorías";
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    // Colores usados
    private final int COLOR_SELECCIONADO_BG = Color.parseColor("#FFD60A");
    private final int COLOR_NO_SELECCIONADO_BG_RES = R.drawable.rounded_yellow;
    private final int COLOR_TEXTO_SELECCIONADO = Color.BLACK;
    private final int COLOR_TEXTO_NO_SELECCIONADO = Color.BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Productos");

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(this);

        dbHelper = new DBHelper(this);

        llCategorias = findViewById(R.id.llCategorias);
        rvProductos = findViewById(R.id.rvProductos);

        rvProductos.setLayoutManager(new GridLayoutManager(this, 2));

        MenuUtil.configurarMenuPorRol(this, navView);

        // --- OPTIMIZACIÓN CRÍTICA DE VELOCIDAD: Iniciar carga en hilo de fondo ---
        iniciarCargaDeDatos();
        // --------------------------------------------------------------------------
    }

    @Override
    protected void onResume() {
        super.onResume();

        // La configuración del menú debe mantenerse para reflejar el estado del login
        MenuUtil.configurarMenuPorRol(this, navView);

        // --- CÓDIGO ELIMINADO: Ya no recargamos productos aquí para evitar el "doble-carga" visible
        // recargarProductosAsincrono(categoriaActual.equals("Todas las categorías") ? null : categoriaActual);
        // --- FIN CÓDIGO ELIMINADO ---

        // Solo resaltamos la categoría, que es una operación ligera
        resaltarCategoria(categoriaActual);
    }

    /**
     * Inicia la carga de categorías y productos en un hilo de fondo.
     */
    private void iniciarCargaDeDatos() {

        new Thread(() -> {
            // --- HILO DE FONDO: Realiza la operación pesada de DB ---
            List<String> categorias = dbHelper.getAllCategorias();
            categorias.add(0, "Todas las categorías");

            String nombreCategoriaCargaInicial = categoriaActual.equals("Todas las categorías") ? null : categoriaActual;
            final List<Producto> listaProductosInicial = obtenerProductosDesdeDB(nombreCategoriaCargaInicial);

            // --- HILO PRINCIPAL: Actualiza la interfaz de usuario ---
            uiHandler.post(() -> {
                // 1. Inicializa los botones de categoría
                inicializarBotonesCategorias(categorias);

                // 2. Inicializa el adaptador de productos
                adapter = new ProductoCatalogoAdapter(this, listaProductosInicial);
                rvProductos.setAdapter(adapter);

                // 3. Resalta la categoría actual
                resaltarCategoria(categoriaActual);

            });
        }).start();
    }

    /**
     * Inicializa los botones de categoría en el LinearLayout.
     */
    private void inicializarBotonesCategorias(List<String> categorias) {
        llCategorias.removeAllViews();
        for (String categoria : categorias) {
            TextView tvCategoria = new TextView(this);
            tvCategoria.setText(categoria);
            tvCategoria.setPadding(20, 10, 20, 10);
            tvCategoria.setTextSize(14);
            tvCategoria.setGravity(android.view.Gravity.CENTER);

            tvCategoria.setBackgroundResource(COLOR_NO_SELECCIONADO_BG_RES);
            tvCategoria.setTextColor(COLOR_TEXTO_NO_SELECCIONADO);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 15, 0);
            tvCategoria.setLayoutParams(params);

            tvCategoria.setOnClickListener(v -> {
                categoriaActual = categoria;
                String filtro = categoria.equals("Todas las categorías") ? null : categoria;
                // La carga de productos debe seguir siendo asíncrona
                recargarProductosAsincrono(filtro);
                resaltarCategoria(categoria);
            });
            llCategorias.addView(tvCategoria);
        }
    }

    /**
     * Maneja la recarga de productos al cambiar de categoría, ejecutándose en segundo plano.
     */
    private void recargarProductosAsincrono(String categoriaNombre) {
        new Thread(() -> {
            final List<Producto> listaProductos = obtenerProductosDesdeDB(categoriaNombre);

            // Volver al hilo principal para actualizar el adaptador
            uiHandler.post(() -> {
                if (adapter != null) {
                    adapter.updateList(listaProductos);
                }
            });
        }).start();
    }

    /**
     * Función que maneja la lógica de la base de datos y mapeo (PESADA, en HILO DE FONDO).
     */
    private List<Producto> obtenerProductosDesdeDB(String categoriaNombre) {
        List<Producto> listaProductos = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (categoriaNombre == null) {
                cursor = dbHelper.getAllProductos();
            } else {
                int idCategoria = dbHelper.getCategoriaIdByNombre(categoriaNombre);
                if (idCategoria != -1) {
                    cursor = dbHelper.getProductosByCategoria(idCategoria);
                }
            }

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    listaProductos.add(cursorToProducto(cursor));
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return listaProductos;
    }

    private void resaltarCategoria(String nombreCategoria) {
        for (int i = 0; i < llCategorias.getChildCount(); i++) {
            TextView tv = (TextView) llCategorias.getChildAt(i);
            String categoria = tv.getText().toString();

            if (categoria.equals(nombreCategoria)) {
                tv.setBackgroundColor(COLOR_SELECCIONADO_BG);
                tv.setTextColor(COLOR_TEXTO_SELECCIONADO);
            } else {
                tv.setBackgroundResource(COLOR_NO_SELECCIONADO_BG_RES);
                tv.setTextColor(COLOR_TEXTO_NO_SELECCIONADO);
            }
        }
    }


    private Producto cursorToProducto(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_producto"));
        String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
        double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
        String imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
        int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));

        int idCategoria = 0;
        try {
            idCategoria = cursor.getInt(cursor.getColumnIndexOrThrow("id_categoria"));
        } catch (IllegalArgumentException e) {
            // Manejo silencioso de la excepción si la columna no existe
        }

        return new Producto(id, nombre, precio, imagen, stock, idCategoria);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean resultado = MenuUtil.manejarNavegacion(this, item.getItemId());
        drawerLayout.closeDrawer(GravityCompat.START);
        return resultado;
    }

    // --- TOOLBAR MENU (CARRITO) ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_productos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            Intent intent = new Intent(ProductosActivity.this, CarritoActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}