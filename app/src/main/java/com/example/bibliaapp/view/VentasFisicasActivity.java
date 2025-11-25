package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.bibliaapp.model.CarritoItem;
import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Pedido;
import com.example.bibliaapp.model.Producto;
import com.example.bibliaapp.model.SharedPreferencesManager; // Importación CLAVE para la validación
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VentasFisicasActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // --- Variables de Navegación (Igual que en InicioActivity) ---
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // --- Variables del Módulo de Ventas ---
    private TextView tvFechaVenta, tvTotalVenta;
    private EditText etNombreCliente, etTelefono, etCantidad;
    private Spinner spMetodoPago, spProductos;
    private Button btnAnadirItem, btnGuardarVenta;
    private RecyclerView rvItemsVenta;

    // Data
    private DBHelper dbHelper;
    private List<Producto> listaProductos;
    private List<CarritoItem> carritoTemporal;
    private Producto productoSeleccionado;
    private CarritoAdapter carritoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- INICIO: BLINDAJE DE SEGURIDAD POR ROL (Vendedor/Administrador) ---
        String rol = SharedPreferencesManager.getInstance(this).getUserRol();
        if (!rol.equals("administrador") && !rol.equals("vendedor")) {
            Toast.makeText(this, "Acceso no autorizado al Módulo de Ventas.", Toast.LENGTH_LONG).show();
            // Redirigir a la pantalla principal (ProductosActivity)
            Intent intent = new Intent(this, ProductosActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return; // Termina la ejecución de onCreate
        }
        // --- FIN: BLINDAJE DE SEGURIDAD ---

        setContentView(R.layout.activity_ventas_fisicas);

        // 1. Configuración del Toolbar y Drawer (Igual que InicioActivity)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Título y color
        getSupportActionBar().setTitle("Módulo de Ventas");
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 2. Inicialización de Lógica de Ventas
        dbHelper = new DBHelper(this);
        carritoTemporal = new ArrayList<>();

        initViewsVentas();
        setupSpinners();
        setupRecyclerView();
        setupListeners();

        actualizarFecha();
        actualizarTotalVenta();
    }

    // --- MÉTODOS DE NAVEGACIÓN (Copiados de InicioActivity) ---

    @Override
    protected void onResume() {
        super.onResume();
        MenuUtil.configurarMenuPorRol(this, navigationView);

        // Re-validación de seguridad en onResume
        String rol = SharedPreferencesManager.getInstance(this).getUserRol();
        if (!rol.equals("administrador") && !rol.equals("vendedor")) {
            finish(); // Cierra si el rol cambió mientras la app estaba en segundo plano.
            return;
        }
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

    // --- MÉTODOS DEL MÓDULO DE VENTAS ---

    private void initViewsVentas() {
        tvFechaVenta = findViewById(R.id.tvFechaVenta);
        tvTotalVenta = findViewById(R.id.tvTotalVenta);
        etNombreCliente = findViewById(R.id.etNombreCliente);
        etTelefono = findViewById(R.id.etTelefono);
        etCantidad = findViewById(R.id.etCantidad);
        spMetodoPago = findViewById(R.id.spMetodoPago);
        spProductos = findViewById(R.id.spProductos);
        btnAnadirItem = findViewById(R.id.btnAnadirItem);
        btnGuardarVenta = findViewById(R.id.btnGuardarVenta);
        rvItemsVenta = findViewById(R.id.rvItemsVenta);
    }

    private void setupSpinners() {
        listaProductos = dbHelper.getAllProductsSimple();
        List<String> nombresProductos = new ArrayList<>();
        for (Producto p : listaProductos) {
            nombresProductos.add(p.getNombre() + " (Stock: " + p.getStock() + ")");
        }
        ArrayAdapter<String> productosAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, nombresProductos);
        spProductos.setAdapter(productosAdapter);

        spProductos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                productoSeleccionado = listaProductos.get(position);
                etCantidad.setText("1");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String[] metodos = new String[]{"Efectivo", "Yape", "Plin"};
        ArrayAdapter<String> pagoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, metodos);
        spMetodoPago.setAdapter(pagoAdapter);
    }

    private void setupRecyclerView() {
        rvItemsVenta.setLayoutManager(new LinearLayoutManager(this));
        carritoAdapter = new CarritoAdapter(carritoTemporal);
        rvItemsVenta.setAdapter(carritoAdapter);
    }

    private void setupListeners() {
        btnAnadirItem.setOnClickListener(v -> agregarItemACarrito());
        btnGuardarVenta.setOnClickListener(v -> confirmarYGuardarVenta());
    }

    private void actualizarFecha() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvFechaVenta.setText("Fecha: " + sdf.format(new Date()));
    }

    private void actualizarTotalVenta() {
        double total = 0;
        for (CarritoItem item : carritoTemporal) total += item.getSubtotal();
        tvTotalVenta.setText("Total: S/ " + String.format(Locale.getDefault(), "%.2f", total));
        btnGuardarVenta.setEnabled(total > 0);
    }

    private void agregarItemACarrito() {
        if (productoSeleccionado == null) {
            Toast.makeText(this, "Seleccione un producto.", Toast.LENGTH_SHORT).show();
            return;
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(etCantidad.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cantidad <= 0 || cantidad > productoSeleccionado.getStock()) {
            Toast.makeText(this, "Stock insuficiente.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean encontrado = false;
        for (CarritoItem item : carritoTemporal) {
            if (item.getProductoId() == productoSeleccionado.getId()) {
                if (item.getCantidad() + cantidad > productoSeleccionado.getStock()) {
                    Toast.makeText(this, "Stock excedido.", Toast.LENGTH_SHORT).show();
                    return;
                }
                item.setCantidad(item.getCantidad() + cantidad);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            // Constructor: id, nombre, precio, cantidad, imagen(placeholder)
            carritoTemporal.add(new CarritoItem(productoSeleccionado.getId(), productoSeleccionado.getNombre(), productoSeleccionado.getPrecio(), cantidad, "physical"));
        }
        carritoAdapter.notifyDataSetChanged();
        actualizarTotalVenta();
        etCantidad.setText("1");
    }

    private void confirmarYGuardarVenta() {
        if (carritoTemporal.isEmpty()) return;

        // Vendedor ID leído de la sesión (Seguro)
        int idVendedor = SharedPreferencesManager.getInstance(this).getUserId();
        if (idVendedor == -1) {
            // Esto solo ocurre si la sesión se pierde en medio de la venta, lo cual es muy improbable
            Toast.makeText(this, "Error de sesión. Intente iniciar sesión de nuevo.", Toast.LENGTH_LONG).show();
            return;
        }

        double total = 0;
        for (CarritoItem item : carritoTemporal) total += item.getSubtotal();

        Pedido pedido = new Pedido(
                dbHelper.generateCodigoPedido(), idVendedor, total, "Completado",
                spMetodoPago.getSelectedItem().toString(), etTelefono.getText().toString().trim(),
                etNombreCliente.getText().toString().trim(), carritoTemporal
        );

        if (dbHelper.insertPedidoFisico(pedido, idVendedor)) {
            Toast.makeText(this, "Venta guardada. Se ha generado la boleta.", Toast.LENGTH_LONG).show();
            carritoTemporal.clear();
            carritoAdapter.notifyDataSetChanged();
            actualizarTotalVenta();
            etNombreCliente.setText("");
            etTelefono.setText("");
            setupSpinners(); // Recargar stock
        } else {
            Toast.makeText(this, "Error al guardar la venta.", Toast.LENGTH_LONG).show();
        }
    }

    private class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.ViewHolder> {
        private List<CarritoItem> items;
        public CarritoAdapter(List<CarritoItem> items) { this.items = items; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito_venta, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CarritoItem item = items.get(position);
            holder.tvNombre.setText(item.getNombre());
            holder.tvCantidad.setText("x" + item.getCantidad());
            holder.tvPrecio.setText("S/" + String.format(Locale.getDefault(), "%.2f", item.getSubtotal()));

            // Forzar color negro (por si acaso)
            holder.tvNombre.setTextColor(getResources().getColor(R.color.black));
            holder.tvCantidad.setTextColor(getResources().getColor(R.color.black));
            holder.tvPrecio.setTextColor(getResources().getColor(R.color.black));

            holder.btnEliminar.setOnClickListener(v -> {
                items.remove(position);
                // Usamos notifyItemRemoved y notifyItemRangeChanged para mejor performance
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, items.size());
                actualizarTotalVenta();
            });
        }

        @Override public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvCantidad, tvPrecio;
            Button btnEliminar;
            ViewHolder(View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvItemNombre);
                tvCantidad = itemView.findViewById(R.id.tvItemCantidad);
                tvPrecio = itemView.findViewById(R.id.tvItemPrecio);
                btnEliminar = itemView.findViewById(R.id.btnEliminarItem);
            }
        }
    }
}