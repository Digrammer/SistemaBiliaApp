package com.example.bibliaapp.view;

import android.content.Intent;
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

        // Inicialización de DBHelper con el Contexto
        dbHelper = new DBHelper(this);

        llCategorias = findViewById(R.id.llCategorias);
        rvProductos = findViewById(R.id.rvProductos);

        rvProductos.setLayoutManager(new GridLayoutManager(this, 2));

        // Asumo que MenuUtil existe y funciona
        MenuUtil.configurarMenuPorRol(this, navView);

        // Iniciar carga en hilo de fondo con manejo de errores
        iniciarCargaDeDatos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuUtil.configurarMenuPorRol(this, navView);
        resaltarCategoria(categoriaActual);
    }

    /**
     * Inicia la carga de categorías y productos en un hilo de fondo.
     */
    private void iniciarCargaDeDatos() {

        new Thread(() -> {
            List<String> categorias = new ArrayList<>();
            List<Producto> listaProductosInicial = new ArrayList<>();
            boolean cargaExitosa = false;

            try {
                // --- HILO DE FONDO: Realiza la operación pesada de DB ---

                // 1. Cargar Categorías
                categorias = dbHelper.getAllCategorias();
                categorias.add(0, "Todas las categorías");

                // 2. Cargar Productos Iniciales (USA EL MÉTODO SEGURO)
                // Si es "Todas las categorías", usa getAllProductosList
                listaProductosInicial = dbHelper.getAllProductosList();

                cargaExitosa = true; // Si llegamos aquí, la DB se abrió y leyó correctamente

            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> {
                    Toast.makeText(this, "Error al cargar datos iniciales. Verifique la base de datos.", Toast.LENGTH_LONG).show();
                });
            }

            // --- HILO PRINCIPAL: Actualiza la interfaz de usuario ---
            final List<String> finalCategorias = categorias;
            final List<Producto> finalListaProductosInicial = listaProductosInicial;
            final boolean finalCargaExitosa = cargaExitosa;

            uiHandler.post(() -> {
                // 1. Inicializa los botones de categoría
                if (finalCargaExitosa) {
                    inicializarBotonesCategorias(finalCategorias);
                } else {
                    List<String> categoriasFallo = new ArrayList<>();
                    categoriasFallo.add("Todas las categorías");
                    inicializarBotonesCategorias(categoriasFallo);
                }

                // 2. Inicializa el adaptador de productos
                adapter = new ProductoCatalogoAdapter(this, finalListaProductosInicial);
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
     * Ahora usa el método seguro getProductosByCategoriaList del DBHelper.
     */
    private void recargarProductosAsincrono(String categoriaNombre) {
        new Thread(() -> {
            List<Producto> listaProductos = new ArrayList<>();
            try {
                if (categoriaNombre == null) {
                    listaProductos = dbHelper.getAllProductosList(); // Todos
                } else {
                    int idCategoria = dbHelper.getCategoriaIdByNombre(categoriaNombre);
                    if (idCategoria != -1) {
                        listaProductos = dbHelper.getProductosByCategoriaList(idCategoria); // Filtrado
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Volver al hilo principal para actualizar el adaptador
            final List<Producto> finalListaProductos = listaProductos;
            uiHandler.post(() -> {
                if (adapter != null) {
                    adapter.updateList(finalListaProductos);
                }
            });
        }).start();
    }

    // 🛑 SE ELIMINARON obtenerProductosDesdeDB y cursorToProducto (Ya no son necesarios)

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Asegúrate de que MenuUtil esté disponible
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