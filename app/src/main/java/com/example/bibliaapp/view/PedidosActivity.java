package com.example.bibliaapp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliaapp.R;
import com.example.bibliaapp.view.adapter.PedidoAdapter;
import com.example.bibliaapp.controller.PedidoController;
import com.example.bibliaapp.model.Pedido;
import com.example.bibliaapp.model.SharedPreferencesManager;
import com.example.bibliaapp.model.Producto; // Import necesario
import com.example.bibliaapp.model.DetallePedido; // Import necesario

import java.util.ArrayList; // Import necesario
import java.util.List;

/**
 * Activity para mostrar la lista de pedidos.
 * Se ha eliminado la lógica de DrawerLayout (menú lateral) y MenuUtil
 * para concentrarse en la funcionalidad principal de la lista y el PDF.
 */
public class PedidosActivity extends AppCompatActivity implements PedidoAdapter.OnPedidoActionListener {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private Toolbar toolbar;

    // Las variables del menú lateral se eliminaron.

    private SharedPreferencesManager sessionManager;
    private PedidoController pedidoController;
    private String userRol;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el layout activity_pedidos.xml
        setContentView(R.layout.activity_pedidos);

        // Inicialización de componentes y controladores
        toolbar = findViewById(R.id.toolbar_pedidos);
        recyclerView = findViewById(R.id.recycler_pedidos);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        // *** CONFIGURACIÓN DEL TOOLBAR ***
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pedidos");
        }
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));

        // *** SOLUCIÓN PARA EL AVISO DE `onBackPressed` (AndroidX) ***
        // Esta callback se mantiene por si quieres usar el botón de retroceso
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Simplemente finaliza la actividad, ya que no hay DrawerLayout que cerrar.
                finish();
            }
        });
        // ***************************************************************


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pedidoController = new PedidoController(this);
        sessionManager = SharedPreferencesManager.getInstance(this);

        // 1. Obtener Rol e ID del usuario logeado
        userId = sessionManager.getUserId();
        userRol = sessionManager.getUserRol();

        // Cargar los pedidos iniciales
        loadPedidos();
    }

    // Se eliminan los métodos onResume y onNavigationItemSelected que dependían de MenuUtil/DrawerLayout.


    /**
     * Carga los pedidos basándose en el rol del usuario.
     */
    private void loadPedidos() {
        // Ejecutar la carga en un hilo separado para no bloquear la UI
        new Thread(() -> {
            List<Pedido> pedidos;

            // La lógica para decidir qué pedidos traer está en el Controller
            if ("administrador".equals(userRol)) {
                // Admin: Pide todos los pedidos.
                pedidos = pedidoController.getPedidosByRol(-1, userRol);
                Log.d("PedidosActivity", "Cargando TODOS los pedidos para Admin.");
            } else if ("vendedor".equals(userRol) || "cliente".equals(userRol)) {
                // Vendedor/Cliente: Pide solo sus pedidos.
                pedidos = pedidoController.getPedidosByRol(userId, userRol);
                Log.d("PedidosActivity", String.format("Cargando pedidos para ID %d y rol %s.", userId, userRol));
            } else {
                // Visitante o Rol desconocido
                pedidos = new java.util.ArrayList<>();
                Log.w("PedidosActivity", "Rol desconocido o visitante. Lista vacía.");
            }

            // Actualizar la UI en el hilo principal
            runOnUiThread(() -> {
                if (pedidos.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("No hay pedidos para mostrar en este momento.");
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmptyState.setVisibility(View.GONE);

                    // Inicializar y configurar el Adapter con la lista y el rol
                    PedidoAdapter adapter = new PedidoAdapter(this, pedidos, userRol, this);
                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }

    // =========================================================================
    // IMPLEMENTACIÓN DE LA INTERFAZ PedidoAdapter.OnPedidoActionListener
    // =========================================================================

    /**
     * Llamado desde el Adapter cuando se marca un pedido como "Completado".
     * Recarga la lista para actualizar la vista.
     */
    @Override
    public void onEstadoActualizado() {
        loadPedidos();
    }

    /**
     * Llamado desde el Adapter cuando se presiona "Descargar Boleta/PDF".
     */
    @Override
    public void onDescargarPdf(Pedido pedido) {
        Toast.makeText(this, "Iniciando generación de Boleta PDF con API Nativa.", Toast.LENGTH_SHORT).show();
        generateAndSharePdf(pedido);
    }

    /**
     * Lógica final de generación de PDF, usando NativePdfGenerator.
     * Genera datos de prueba para la boleta si no hay un sistema de carrito real.
     */
    private void generateAndSharePdf(Pedido pedido) {
        Log.i("PDF_GENERATOR", "Generando documento para Pedido #"+ pedido.getIdPedido());

        // PASO 1: Crear la lista de DetallePedido (Simulación de Productos comprados)
        List<DetallePedido> detallesDePrueba = new ArrayList<>();

        // Creamos objetos Producto (simulando que vienen de la DB o un listado)
        // Recordar el constructor de Producto: public Producto(String nombre, double precio, String imagen, int stock, int idCategoria)

        // 1. Producto 1: Biblia (Cantidad: 2)
        Producto p1 = new Producto("Biblia Reina Valera 1960 - Tapa Dura", 95.00, "biblia.png", 50, 1);
        detallesDePrueba.add(new DetallePedido(p1, 2));

        // 2. Producto 2: Libro (Cantidad: 1)
        Producto p2 = new Producto("Libro 'Liderazgo 360' de Maxwell", 45.90, "liderazgo.png", 20, 2);
        detallesDePrueba.add(new DetallePedido(p2, 1));

        // 3. Producto 3: Accesorios (Cantidad: 3)
        Producto p3 = new Producto("Separadores Magnéticos (Pack 5u)", 10.00, "separador.png", 100, 3);
        detallesDePrueba.add(new DetallePedido(p3, 3));

        // 4. Producto 4: Música (Cantidad: 1)
        Producto p4 = new Producto("CD Música Cristiana (Colección)", 25.50, "cd.png", 15, 4);
        detallesDePrueba.add(new DetallePedido(p4, 1));

        // PASO 2: Llamamos al generador nativo para crear y compartir el PDF
        NativePdfGenerator.generateAndShareReceipt(this, pedido, detallesDePrueba);
    }
}