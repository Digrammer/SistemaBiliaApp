package com.example.bibliaapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.bibliaapp.model.SharedPreferencesManager;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VentasFisicasActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // --- Variables de Navegación ---
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // --- Variables del Módulo de Ventas ---
    private TextView tvFechaVenta, tvTotalVenta;
    private EditText etNombreCliente, etTelefono, etCantidad;
    private Spinner spMetodoPago, spProductos, spTipoComprobante;
    private EditText etRuc, etRazonSocial;
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

        // --- BLINDAJE DE SEGURIDAD POR ROL (Vendedor/Administrador) ---
        String rol = SharedPreferencesManager.getInstance(this).getUserRol();
        if (!rol.equals("administrador") && !rol.equals("vendedor")) {
            Toast.makeText(this, "Acceso no autorizado al Módulo de Ventas.", Toast.LENGTH_LONG).show();
            // Asumiendo que ProductosActivity es la actividad de inicio
            Intent intent = new Intent(this, ProductosActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.activity_ventas_fisicas);

        // 1. Configuración del Toolbar y Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        getSupportActionBar().setTitle("Módulo de Ventas");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // ** CONFIGURACIÓN DE VISIBILIDAD DEL MENÚ **
        // Asegúrate de que los grupos de gestión y reportes sean visibles para el admin/vendedor
        if ("administrador".equals(rol) || "vendedor".equals(rol)) {
            navigationView.getMenu().setGroupVisible(R.id.grupo_gestion, true);
        }
        if ("administrador".equals(rol)) {
            navigationView.getMenu().setGroupVisible(R.id.grupo_reportes, true);
        }
        // *******************************************

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

    @Override
    protected void onResume() {
        super.onResume();
        String rol = SharedPreferencesManager.getInstance(this).getUserRol();
        if (!rol.equals("administrador") && !rol.equals("vendedor")) {
            finish();
            return;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // ** CORRECCIÓN DE IDs DE NAVEGACIÓN **

        if (id == R.id.nav_inicio) {
            startActivity(new Intent(this, InicioActivity.class));
        } else if (id == R.id.nav_productos) {
            startActivity(new Intent(this, ProductosActivity.class));
        } else if (id == R.id.nav_pedidos) {
            startActivity(new Intent(this, PedidosActivity.class));
        } else if (id == R.id.nav_ventas) { // CORREGIDO: Usando nav_ventas del XML
            // Ya estás aquí
        } else if (id == R.id.nav_configuracion) {
            startActivity(new Intent(this, GestionProductosActivity.class)); // Asumiendo que es Configuración/Gestión Productos
        } else if (id == R.id.nav_crear_usuario) {
            startActivity(new Intent(this, GestionUsuariosActivity.class));
        } else if (id == R.id.nav_reportes) {
            startActivity(new Intent(this, ReportesActivity.class)); // Asumiendo que tienes ReportesActivity
        } else if (id == R.id.nav_logout) { // CORREGIDO: Usando nav_logout del XML
            SharedPreferencesManager.getInstance(this).clearUserSession(); // Corregido: clearUserSession
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Migrar a OnBackPressedDispatcher (moderno) si es necesario,
            // pero este enfoque antiguo aún funciona.
            super.onBackPressed();
        }
    }


    // --- MÉTODOS DEL MÓDULO DE VENTAS (Misma lógica, solo para completar el archivo) ---

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

        spTipoComprobante = findViewById(R.id.spTipoComprobante);
        etRuc = findViewById(R.id.etRuc);
        etRazonSocial = findViewById(R.id.etRazonSocial);
    }

    private void setupSpinners() {
        // --- 1. SPINNER PRODUCTOS (Sin cambios) ---
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

        // --- 2. SPINNER MÉTODO PAGO (Sin cambios) ---
        String[] metodos = new String[]{"Efectivo", "Yape", "Plin"};
        ArrayAdapter<String> pagoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, metodos);
        spMetodoPago.setAdapter(pagoAdapter);

        // --- 3. SPINNER TIPO DE COMPROBANTE (NUEVO) ---
        String[] tiposComprobante = new String[]{"Boleta", "Factura"};
        ArrayAdapter<String> comprobanteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tiposComprobante);
        spTipoComprobante.setAdapter(comprobanteAdapter);

        // Listener para mostrar/ocultar campos de RUC/Razón Social
        spTipoComprobante.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if ("Factura".equals(selected)) {
                    etRuc.setVisibility(View.VISIBLE);
                    etRazonSocial.setVisibility(View.VISIBLE);
                } else {
                    etRuc.setVisibility(View.GONE);
                    etRazonSocial.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etRuc.setVisibility(View.GONE);
                etRazonSocial.setVisibility(View.GONE);
            }
        });
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
            carritoTemporal.add(new CarritoItem(productoSeleccionado.getId(), productoSeleccionado.getNombre(), productoSeleccionado.getPrecio(), cantidad, "physical"));
        }
        carritoAdapter.notifyDataSetChanged();
        actualizarTotalVenta();
        etCantidad.setText("1");
    }

    private void confirmarYGuardarVenta() {
        if (carritoTemporal.isEmpty()) {
            Toast.makeText(this, "Debe añadir productos al carrito.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 1. Capturar datos de sesión y totales ---
        int idVendedor = SharedPreferencesManager.getInstance(this).getUserId();
        int idUsuarioCliente = 3; // Cliente genérico (Asegúrate de que el ID 3 exista o sea el ID genérico en tu DB)

        if (idVendedor == -1) {
            Toast.makeText(this, "Error de sesión. Intente iniciar sesión de nuevo.", Toast.LENGTH_LONG).show();
            return;
        }

        double total = 0;
        for (CarritoItem item : carritoTemporal) total += item.getSubtotal();

        // --- 2. Capturar datos de Comprobante ---
        String tipoComprobante = spTipoComprobante.getSelectedItem().toString();
        String ruc = null;
        String razonSocial = null;
        String nombreCliente = etNombreCliente.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if ("Factura".equals(tipoComprobante)) {
            ruc = etRuc.getText().toString().trim();
            razonSocial = etRazonSocial.getText().toString().trim();

            // Validación específica para Factura
            if (ruc.isEmpty() || ruc.length() != 11) {
                Toast.makeText(this, "Ingrese un RUC válido (11 dígitos).", Toast.LENGTH_LONG).show();
                etRuc.requestFocus();
                return;
            }
            if (razonSocial.isEmpty()) {
                Toast.makeText(this, "Ingrese la Razón Social.", Toast.LENGTH_LONG).show();
                etRazonSocial.requestFocus();
                return;
            }
        }

        // Validación básica
        if (nombreCliente.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del cliente.", Toast.LENGTH_LONG).show();
            etNombreCliente.requestFocus();
            return;
        }


        // --- 3. Crear el objeto Pedido completo ---
        Pedido pedido = new Pedido(
                dbHelper.generateCodigoPedido(), // idPedido (código)
                idUsuarioCliente, // idUsuario (el cliente genérico)
                idVendedor, // idVendedor (El usuario logueado)
                total,
                "Completado",
                spMetodoPago.getSelectedItem().toString(), // tipoEntrega (método de pago)
                telefono,
                nombreCliente,
                tipoComprobante, // Tipo Comprobante
                carritoTemporal
        );

        // Asignamos RUC/RazonSocial
        if ("Factura".equals(tipoComprobante)) {
            pedido.setRuc(ruc);
            pedido.setRazonSocial(razonSocial);
        }


        // --- 4. Guardar en la DB ---
        if (dbHelper.insertPedidoFisico(pedido, idVendedor)) {

            // Limpieza y feedback
            Toast.makeText(this, "Venta guardada. Se ha generado la " + tipoComprobante + ".", Toast.LENGTH_LONG).show();
            carritoTemporal.clear();
            carritoAdapter.notifyDataSetChanged();
            actualizarTotalVenta();

            // Limpiar campos de entrada
            etNombreCliente.setText("");
            etTelefono.setText("");
            etRuc.setText("");
            etRazonSocial.setText("");
            spTipoComprobante.setSelection(0); // Volver a Boleta

            setupSpinners(); // Recargar stock para reflejar la venta
        } else {
            Toast.makeText(this, "Error al guardar la venta.", Toast.LENGTH_LONG).show();
        }
    }

    // --- CLASE INTERNA CarritoAdapter (Se deja sin cambios) ---
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

            holder.tvNombre.setTextColor(getResources().getColor(R.color.black));
            holder.tvCantidad.setTextColor(getResources().getColor(R.color.black));
            holder.tvPrecio.setTextColor(getResources().getColor(R.color.black));

            holder.btnEliminar.setOnClickListener(v -> {
                items.remove(position);
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