package com.example.bibliaapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.bibliaapp.model.dao.DetallePedidoDao;
import com.example.bibliaapp.model.dao.PedidoDao;
import com.example.bibliaapp.model.dao.ProductoDao;
import com.example.bibliaapp.model.dao.UsuarioDao;

import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "BibliaAppDB";
    private static final int DB_VERSION = 2;

    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COL_USUARIO_ID = "id_usuario";
    public static final String COL_USUARIO_NOMBRE = "nombre";
    public static final String COL_USUARIO_APELLIDO = "apellido";
    public static final String COL_USUARIO_CORREO = "correo";
    public static final String COL_USUARIO_CONTRASENA = "contraseña";
    public static final String COL_USUARIO_DNI = "dni";
    public static final String COL_USUARIO_TELEFONO = "telefono";
    public static final String COL_USUARIO_DIRECCION = "direccion";
    public static final String COL_USUARIO_ROL = "rol";

    public static final String TABLE_PRODUCTOS = "productos";
    public static final String TABLE_CATEGORIAS = "categorias";
    public static final String TABLE_PEDIDOS = "pedidos";
    public static final String TABLE_DETALLE_PEDIDO = "detalle_pedido";
    public static final String TABLE_BOLETAS = "boletas";

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREACIÓN DE TABLAS (idéntico a lo que tenías)
        db.execSQL("CREATE TABLE " + TABLE_USUARIOS + " (" +
                COL_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_USUARIO_NOMBRE + " TEXT NOT NULL," +
                COL_USUARIO_APELLIDO + " TEXT," +
                COL_USUARIO_CORREO + " TEXT UNIQUE NOT NULL," +
                COL_USUARIO_CONTRASENA + " TEXT NOT NULL," +
                COL_USUARIO_DNI + " TEXT," +
                COL_USUARIO_TELEFONO + " TEXT," +
                COL_USUARIO_DIRECCION + " TEXT," +
                COL_USUARIO_ROL + " TEXT NOT NULL" +
                ");");
        db.execSQL("CREATE INDEX idx_usuarios_correo ON " + TABLE_USUARIOS + "(" + COL_USUARIO_CORREO + ");");

        db.execSQL("CREATE TABLE " + TABLE_CATEGORIAS + " (" +
                "id_categoria INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL UNIQUE" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_PRODUCTOS + " (" +
                "id_producto INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "precio REAL NOT NULL," +
                "imagen TEXT," +
                "id_categoria INTEGER NOT NULL," +
                "stock INTEGER NOT NULL," +
                "FOREIGN KEY(id_categoria) REFERENCES " + TABLE_CATEGORIAS + "(id_categoria)" +
                ");");
        db.execSQL("CREATE INDEX idx_productos_categoria ON " + TABLE_PRODUCTOS + "(id_categoria);");
        db.execSQL("CREATE INDEX idx_productos_nombre ON " + TABLE_PRODUCTOS + "(nombre);");

        db.execSQL("CREATE TABLE " + TABLE_PEDIDOS + " (" +
                "id_pedido INTEGER PRIMARY KEY AUTOINCREMENT," +
                "codigo TEXT NOT NULL UNIQUE," +
                "id_usuario INTEGER NOT NULL," +
                "fecha DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "total REAL NOT NULL," +
                "estado TEXT NOT NULL," +
                "metodo_pago TEXT NOT NULL," +
                "telefono_contacto TEXT," +
                "nombre_cliente TEXT," +
                "FOREIGN KEY(id_usuario) REFERENCES " + TABLE_USUARIOS + "(" + COL_USUARIO_ID + ")" +
                ");");
        db.execSQL("CREATE INDEX idx_pedidos_usuario ON " + TABLE_PEDIDOS + "(id_usuario);");
        db.execSQL("CREATE INDEX idx_pedidos_codigo ON " + TABLE_PEDIDOS + "(codigo);");

        db.execSQL("CREATE TABLE " + TABLE_DETALLE_PEDIDO + " (" +
                "id_detalle INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_pedido INTEGER NOT NULL," +
                "id_producto INTEGER NOT NULL," +
                "cantidad INTEGER NOT NULL," +
                "subtotal REAL NOT NULL," +
                "FOREIGN KEY(id_pedido) REFERENCES " + TABLE_PEDIDOS + "(id_pedido)," +
                "FOREIGN KEY(id_producto) REFERENCES " + TABLE_PRODUCTOS + "(id_producto)" +
                ");");
        db.execSQL("CREATE INDEX idx_detalle_pedido_pedido ON " + TABLE_DETALLE_PEDIDO + "(id_pedido);");
        db.execSQL("CREATE INDEX idx_detalle_pedido_producto ON " + TABLE_DETALLE_PEDIDO + "(id_producto);");

        db.execSQL("CREATE TABLE " + TABLE_BOLETAS + " (" +
                "id_boleta INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_pedido INTEGER NOT NULL," +
                "fecha DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "total REAL NOT NULL," +
                "numero_boleta TEXT UNIQUE," +
                "FOREIGN KEY(id_pedido) REFERENCES " + TABLE_PEDIDOS + "(id_pedido)" +
                ");");
        db.execSQL("CREATE INDEX idx_boletas_pedido ON " + TABLE_BOLETAS + "(id_pedido);");

        db.execSQL("CREATE TABLE empleados (" +
                "id_empleado INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "correo TEXT UNIQUE," +
                "telefono TEXT" +
                ");");

        // Inserciones iniciales (mantengo tus métodos de inserción aquí)
        checkAndInsertInitialCategories(db);
        insertInitialProducts(db);
        checkAndInsertInitialUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETALLE_PEDIDO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEDIDOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOLETAS);
        db.execSQL("DROP TABLE IF EXISTS empleados");
        onCreate(db);
    }

    // ---------- Mantengo los métodos de inicialización que se ejecutan en onCreate ----------
    private void insertProductoInicial(SQLiteDatabase db, String nombre, double precio, String imagen, String categoriaNombre, int stock) {
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_CATEGORIAS + " (nombre) VALUES (?)", new String[]{categoriaNombre});
        Cursor cursor = db.rawQuery("SELECT id_categoria FROM " + TABLE_CATEGORIAS + " WHERE nombre = ?", new String[]{categoriaNombre});
        int id_categoria = -1;
        if (cursor != null && cursor.moveToFirst()) {
            id_categoria = cursor.getInt(0);
            cursor.close();
        }

        if (id_categoria != -1) {
            ContentValues cv = new ContentValues();
            cv.put("nombre", nombre);
            cv.put("precio", precio);
            cv.put("imagen", imagen);
            cv.put("id_categoria", id_categoria);
            cv.put("stock", stock);
            db.insertWithOnConflict(TABLE_PRODUCTOS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private void insertInitialProducts(SQLiteDatabase db) {
        insertProductoInicial(db, "Biblia RVR 1960 Gigante", 79.90, "biblia1", "Biblias", 50);
        insertProductoInicial(db, "Biblia Devocional Mujer", 165.00, "biblia2", "Biblias", 30);
        insertProductoInicial(db, "Libro \"Vida con Propósito\"", 49.00, "libro1", "Libros", 20);
        insertProductoInicial(db, "Cuentos Bíblicos Ilustrados", 32.00, "libro2", "Libros", 45);
        insertProductoInicial(db, "Placa Decorativa Fe", 38.00, "regalo1", "Regalos", 15);
        insertProductoInicial(db, "Mini Figura Ángel Guarda", 65.00, "regalo2", "Regalos", 10);
        insertProductoInicial(db, "Bolso Tote Versículo", 55.00, "accesorio1", "Accesorios", 25);
        insertProductoInicial(db, "Llavero Metálico Ichtus", 15.00, "accesorio2", "Accesorios", 60);
        insertProductoInicial(db, "Agenda 2026 Versículo", 45.00, "papeleria1", "Papelería", 35);
        insertProductoInicial(db, "Separadores Citas Bíblicas", 20.00, "papeleria2", "Papelería", 80);
    }

    public void checkAndInsertInitialCategories(SQLiteDatabase db) {
        String[] categoriasIniciales = {"Biblias", "Libros", "Regalos", "Accesorios", "Papelería"};
        for (String categoria : categoriasIniciales) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_CATEGORIAS + " (nombre) VALUES (?)", new String[]{categoria});
        }
    }

    public void checkAndInsertInitialUsers(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USUARIOS, null);
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();
            if (count == 0) {
                insertUsuarioInterno(db, "Admin", "Root", "admin@gmail.com", "Admin@123", "00000000", "999999999", "Dirección Admin", "administrador");
                insertUsuarioInterno(db, "Vendedor", "Principal", "vendedor@gmail.com", "Venta@123", "11111111", "988888888", "Dirección Ventas", "vendedor");
                insertUsuarioInterno(db, "Cliente", "Prueba", "cliente@gmail.com", "Cliente@123", "22222222", "977777777", "Dirección Cliente", "cliente");
            }
        }
    }

    private long insertUsuarioInterno(SQLiteDatabase db, String nombre, String apellido, String correo, String contraseña,
                                      String dni, String telefono, String direccion, String rol) {
        ContentValues cv = new ContentValues();
        cv.put(COL_USUARIO_NOMBRE, nombre);
        cv.put(COL_USUARIO_APELLIDO, apellido);
        cv.put(COL_USUARIO_CORREO, correo);
        cv.put(COL_USUARIO_CONTRASENA, contraseña);
        cv.put(COL_USUARIO_DNI, dni);
        cv.put(COL_USUARIO_TELEFONO, telefono);
        cv.put(COL_USUARIO_DIRECCION, direccion);
        cv.put(COL_USUARIO_ROL, rol);
        return db.insert(TABLE_USUARIOS, null, cv);
    }

    // ---------- FIN inicialización ----------

    // ---------- Métodos públicos: ahora DELEGAN a los DAOs ----------
    // Usuario
    public long insertUsuario(String nombre, String apellido, String correo, String contraseña,
                              String dni, String telefono, String direccion, String rol) {
        return new UsuarioDao(this).insertUsuario(nombre, apellido, correo, contraseña, dni, telefono, direccion, rol);
    }

    public Cursor getUsuarioByCorreo(String correo) {
        return new UsuarioDao(this).getUsuarioByCorreo(correo);
    }

    public Cursor getUsuarioById(int id_usuario) {
        return new UsuarioDao(this).getUsuarioById(id_usuario);
    }

    public String getRolByCorreo(String correo) {
        return new UsuarioDao(this).getRolByCorreo(correo);
    }

    public String validateAndGetRol(String correo, String contraseña) {
        return new UsuarioDao(this).validateAndGetRol(correo, contraseña);
    }

    public boolean validateLogin(String correo, String contraseña) {
        return new UsuarioDao(this).validateLogin(correo, contraseña);
    }

    public int updateUsuario(int id_usuario, String nombre, String apellido, String correo,
                             String contraseña, String dni, String telefono, String direccion, String rol) {
        return new UsuarioDao(this).updateUsuario(id_usuario, nombre, apellido, correo, contraseña, dni, telefono, direccion, rol);
    }

    public int deleteUsuario(int id_usuario) {
        return new UsuarioDao(this).deleteUsuario(id_usuario);
    }

    public boolean guardarTelefonoDeCliente(String telefono) {
        return new UsuarioDao(this).guardarTelefonoDeCliente(telefono);
    }

    public int getUserIdByEmail(String email) {
        return new UsuarioDao(this).getUserIdByEmail(email);
    }

    // Producto
    public int getProductoCount() {
        return new ProductoDao(this).getProductoCount();
    }

    public int getStockProducto(int idProducto) {
        return new ProductoDao(this).getStockProducto(idProducto);
    }

    public boolean actualizarStockPorCompra(int idProducto, int cantidadComprada) {
        return new ProductoDao(this).actualizarStockPorCompra(idProducto, cantidadComprada);
    }

    public boolean actualizarStock(int idProducto, int cantidadAñadir) {
        return new ProductoDao(this).actualizarStock(idProducto, cantidadAñadir);
    }

    public long insertProducto(String nombre, double precio, String imagen, int id_categoria, int stock) {
        return new ProductoDao(this).insertProducto(nombre, precio, imagen, id_categoria, stock);
    }

    public Cursor getProductoById(int id_producto) {
        return new ProductoDao(this).getProductoById(id_producto);
    }

    public Producto getProductoByIdObject(int id) {
        return new ProductoDao(this).getProductoByIdObject(id);
    }

    public Producto getProductoByIdModel(int idProducto) {
        return new ProductoDao(this).getProductoByIdModel(idProducto);
    }

    public List<Producto> getAllProductosList() {
        return new ProductoDao(this).getAllProductosList();
    }

    public List<Producto> getProductosByCategoriaList(int id_categoria) {
        return new ProductoDao(this).getProductosByCategoriaList(id_categoria);
    }

    public Cursor getAllProductos() {
        return new ProductoDao(this).getAllProductos();
    }

    public Cursor getProductosByCategoria(int id_categoria) {
        return new ProductoDao(this).getProductosByCategoria(id_categoria);
    }

    public int updateProducto(int id_producto, String nombre, double precio, String imagen, int id_categoria, int stock) {
        return new ProductoDao(this).updateProducto(id_producto, nombre, precio, imagen, id_categoria, stock);
    }

    public int deleteProducto(int id_producto) {
        return new ProductoDao(this).deleteProducto(id_producto);
    }

    public long insertCategoria(String nombre) {
        return new ProductoDao(this).insertCategoria(nombre);
    }

    public int getCategoriaIdByNombre(String nombre) {
        return new ProductoDao(this).getCategoriaIdByNombre(nombre);
    }

    public java.util.List<String> getAllCategorias() {
        return new ProductoDao(this).getAllCategorias();
    }

    public java.util.List<Producto> getAllProductsSimple() {
        return new ProductoDao(this).getAllProductsSimple();
    }

    // Pedido y detalle
    public long insertPedido(String codigo, int id_usuario, double total, String estado, String metodo_pago, String telefonoContacto) {
        return new PedidoDao(this).insertPedido(codigo, id_usuario, total, estado, metodo_pago, telefonoContacto);
    }

    public Cursor getPedidosByUsuario(int id_usuario) {
        return new PedidoDao(this).getPedidosByUsuario(id_usuario);
    }

    public int updateEstadoPedido(String codigo, String estado) {
        return new PedidoDao(this).updateEstadoPedido(codigo, estado);
    }

    public long insertDetallePedido(int id_pedido, int id_producto, int cantidad, double subtotal) {
        return new DetallePedidoDao(this).insertDetallePedido(id_pedido, id_producto, cantidad, subtotal);
    }

    public Cursor getDetalleByPedido(int id_pedido) {
        return new DetallePedidoDao(this).getDetalleByPedido(id_pedido);
    }

    public long insertBoleta(int id_pedido, double total, String numero_boleta) {
        return new PedidoDao(this).insertBoleta(id_pedido, total, numero_boleta);
    }

    public Cursor getBoletaByPedido(int id_pedido) {
        return new PedidoDao(this).getBoletaByPedido(id_pedido);
    }

    public long guardarPedidoCompleto(Pedido pedido, java.util.List<com.example.bibliaapp.model.CarritoItem> items) {
        return new PedidoDao(this).guardarPedidoCompleto(pedido, items);
    }

    public Cursor getDetallePedidoConNombre(long id_pedido) {
        return new DetallePedidoDao(this).getDetallePedidoConNombre(id_pedido);
    }

    public Cursor getPedidoInfoById(long id_pedido) {
        return new PedidoDao(this).getPedidoInfoById(id_pedido);
    }

    public int generateCodigoPedido() {
        return new PedidoDao(this).generateCodigoPedido();
    }

    public boolean insertPedidoFisico(Pedido pedido, int idUsuarioVendedor) {
        return new PedidoDao(this).insertPedidoFisico(pedido, idUsuarioVendedor);
    }
    // ---------- fin delegaciones ----------
}
