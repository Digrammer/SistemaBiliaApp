package com.example.bibliaapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Importación necesaria para Log
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// Asegúrate de que estas clases existan o reemplaza los imports según tu estructura
// import com.example.bibliaapp.model.CarritoItem;
// import com.example.bibliaapp.model.Pedido;
// import com.example.bibliaapp.model.Producto;

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
    private static final String TABLE_BOLETAS = "boletas";

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
        // CREACIÓN DE TABLAS
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

        // Nota: Agregué la columna 'nombre_cliente' a la tabla PEDIDOS para ventas físicas
        db.execSQL("CREATE TABLE " + TABLE_PEDIDOS + " (" +
                "id_pedido INTEGER PRIMARY KEY AUTOINCREMENT," +
                "codigo TEXT NOT NULL UNIQUE," +
                "id_usuario INTEGER NOT NULL," +
                "fecha DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "total REAL NOT NULL," +
                "estado TEXT NOT NULL," +
                "metodo_pago TEXT NOT NULL," +
                "telefono_contacto TEXT," +
                "nombre_cliente TEXT," + // Columna añadida para la venta física
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


    public int getProductoCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTOS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getStockProducto(int idProducto) {
        SQLiteDatabase db = this.getReadableDatabase();
        int stock = 0;
        Cursor cursor = db.rawQuery("SELECT stock FROM " + TABLE_PRODUCTOS + " WHERE id_producto = ?", new String[]{String.valueOf(idProducto)});
        if (cursor != null && cursor.moveToFirst()) {
            stock = cursor.getInt(0);
            cursor.close();
        }
        return stock;
    }

    public boolean actualizarStockPorCompra(int idProducto, int cantidadComprada) {
        SQLiteDatabase db = this.getWritableDatabase();
        int stockActual = getStockProducto(idProducto);
        int nuevoStock = stockActual - cantidadComprada;

        if (nuevoStock < 0) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put("stock", nuevoStock);

        int filasAfectadas = db.update(TABLE_PRODUCTOS, values, "id_producto = ?", new String[]{String.valueOf(idProducto)});
        return filasAfectadas > 0;
    }

    public boolean actualizarStock(int idProducto, int cantidadAñadir) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        int stockActual = getStockProducto(idProducto);
        int nuevoStock = stockActual + cantidadAñadir;

        if (nuevoStock < 0) {
            return false;
        }

        values.put("stock", nuevoStock);

        int filasAfectadas = db.update(TABLE_PRODUCTOS, values, "id_producto = ?", new String[]{String.valueOf(idProducto)});

        return filasAfectadas > 0;
    }


    public long insertUsuario(String nombre, String apellido, String correo, String contraseña,
                              String dni, String telefono, String direccion, String rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USUARIO_NOMBRE, nombre);
        cv.put(COL_USUARIO_APELLIDO, apellido);
        cv.put(COL_USUARIO_CORREO, correo);
        cv.put(COL_USUARIO_CONTRASENA, contraseña);
        cv.put(COL_USUARIO_DNI, dni);
        cv.put(COL_USUARIO_TELEFONO, telefono);
        cv.put(COL_USUARIO_DIRECCION, direccion);
        cv.put(COL_USUARIO_ROL, rol);
        long result = db.insert(TABLE_USUARIOS, null, cv);
        return result;
    }

    public Cursor getUsuarioByCorreo(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE " + COL_USUARIO_CORREO + " = ?", new String[]{correo});
    }

    public Cursor getUsuarioById(int id_usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE " + COL_USUARIO_ID + " = ?", new String[]{String.valueOf(id_usuario)});
    }

    public String getRolByCorreo(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_USUARIO_ROL + " FROM " + TABLE_USUARIOS + " WHERE " + COL_USUARIO_CORREO + " = ?", new String[]{correo});
        String rol = null;
        if (cursor != null && cursor.moveToFirst()) {
            rol = cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_ROL));
            cursor.close();
        }
        return rol;
    }

    public String validateAndGetRol(String correo, String contraseña) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String rol = null;

        try {
            String[] columns = {COL_USUARIO_CONTRASENA, COL_USUARIO_ROL};
            String selection = COL_USUARIO_CORREO + " = ?";
            String[] selectionArgs = {correo};

            cursor = db.query(TABLE_USUARIOS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String passAlmacenada = cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_CONTRASENA));

                if (passAlmacenada.equals(contraseña)) {
                    rol = cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_ROL));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return rol != null ? rol.toLowerCase().trim() : null;
    }


    public boolean validateLogin(String correo, String contraseña) {
        Cursor cursor = getUsuarioByCorreo(correo);
        boolean isValid = false;
        if (cursor != null && cursor.moveToFirst()) {
            String pass = cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_CONTRASENA));
            isValid = pass.equals(contraseña);
            cursor.close();
        }
        return isValid;
    }

    public int updateUsuario(int id_usuario, String nombre, String apellido, String correo,
                             String contraseña, String dni, String telefono, String direccion, String rol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USUARIO_NOMBRE, nombre);
        cv.put(COL_USUARIO_APELLIDO, apellido);
        cv.put(COL_USUARIO_CORREO, correo);
        cv.put(COL_USUARIO_CONTRASENA, contraseña);
        cv.put(COL_USUARIO_DNI, dni);
        cv.put(COL_USUARIO_TELEFONO, telefono);
        cv.put(COL_USUARIO_DIRECCION, direccion);
        cv.put(COL_USUARIO_ROL, rol);
        int result = db.update(TABLE_USUARIOS, cv, COL_USUARIO_ID + " = ?", new String[]{String.valueOf(id_usuario)});
        return result;
    }

    public int deleteUsuario(int id_usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USUARIOS, COL_USUARIO_ID + " = ?", new String[]{String.valueOf(id_usuario)});
        return result;
    }

    public long insertCategoria(String nombre) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        long result = db.insert(TABLE_CATEGORIAS, null, cv);
        return result;
    }

    public int getCategoriaIdByNombre(String nombre) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id_categoria FROM " + TABLE_CATEGORIAS + " WHERE nombre = ?", new String[]{nombre});
        int id = -1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(0);
            cursor.close();
        }
        return id;
    }

    public List<String> getAllCategorias() {
        List<String> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre FROM " + TABLE_CATEGORIAS, null);
        if (cursor.moveToFirst()) {
            do {
                lista.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public long insertProducto(String nombre, double precio, String imagen,
                               int id_categoria, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("precio", precio);
        cv.put("imagen", imagen);
        cv.put("id_categoria", id_categoria);
        cv.put("stock", stock);
        long result = db.insert(TABLE_PRODUCTOS, null, cv);
        return result;
    }

    public Cursor getProductoById(int id_producto) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTOS + " WHERE id_producto = ?", new String[]{String.valueOf(id_producto)});
    }

    // Se asume que la clase Producto está disponible en el paquete com.example.bibliaapp.model
    public Producto getProductoByIdObject(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Producto producto = null;

        try {
            cursor = db.query(
                    TABLE_PRODUCTOS,
                    new String[] {"id_producto", "nombre", "precio", "imagen", "id_categoria", "stock"},
                    "id_producto" + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow("id_producto");
                int nombreIndex = cursor.getColumnIndexOrThrow("nombre");
                int precioIndex = cursor.getColumnIndexOrThrow("precio");
                int imagenIndex = cursor.getColumnIndexOrThrow("imagen");
                int idCategoriaIndex = cursor.getColumnIndexOrThrow("id_categoria");
                int stockIndex = cursor.getColumnIndexOrThrow("stock");

                int prodId = cursor.getInt(idIndex);
                String nombre = cursor.getString(nombreIndex);
                double precio = cursor.getDouble(precioIndex);
                String imagen = cursor.getString(imagenIndex);
                int idCategoria = cursor.getInt(idCategoriaIndex);
                int stock = cursor.getInt(stockIndex);

                producto = new Producto(prodId, nombre, precio, imagen, stock, idCategoria);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return producto;
    }

    // Se asume que la clase Producto está disponible en el paquete com.example.bibliaapp.model
    public List<Producto> getAllProductosList() {
        List<Producto> listaProductos = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTOS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow("id_producto");
            int nombreIndex = cursor.getColumnIndexOrThrow("nombre");
            int precioIndex = cursor.getColumnIndexOrThrow("precio");
            int imagenIndex = cursor.getColumnIndexOrThrow("imagen");
            int idCategoriaIndex = cursor.getColumnIndexOrThrow("id_categoria");
            int stockIndex = cursor.getColumnIndexOrThrow("stock");

            do {
                try {
                    int prodId = cursor.getInt(idIndex);
                    String nombre = cursor.getString(nombreIndex);
                    double precio = cursor.getDouble(precioIndex);
                    String imagen = cursor.getString(imagenIndex);
                    int idCategoria = cursor.getInt(idCategoriaIndex);
                    int stock = cursor.getInt(stockIndex);

                    Producto producto = new Producto(prodId, nombre, precio, imagen, stock, idCategoria);
                    listaProductos.add(producto);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return listaProductos;
    }

    // Se asume que la clase Producto está disponible en el paquete com.example.bibliaapp.model
    public List<Producto> getProductosByCategoriaList(int id_categoria) {
        List<Producto> listaProductos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTOS + " WHERE id_categoria = ?", new String[]{String.valueOf(id_categoria)});

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow("id_producto");
            int nombreIndex = cursor.getColumnIndexOrThrow("nombre");
            int precioIndex = cursor.getColumnIndexOrThrow("precio");
            int imagenIndex = cursor.getColumnIndexOrThrow("imagen");
            int idCategoriaIndex = cursor.getColumnIndexOrThrow("id_categoria");
            int stockIndex = cursor.getColumnIndexOrThrow("stock");

            do {
                try {
                    int prodId = cursor.getInt(idIndex);
                    String nombre = cursor.getString(nombreIndex);
                    double precio = cursor.getDouble(precioIndex);
                    String imagen = cursor.getString(imagenIndex);
                    int stock = cursor.getInt(stockIndex);
                    int idCategoria = cursor.getInt(idCategoriaIndex);

                    Producto producto = new Producto(prodId, nombre, precio, imagen, stock, idCategoria);
                    listaProductos.add(producto);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return listaProductos;
    }


    public Cursor getAllProductos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTOS, null);
    }

    public Cursor getProductosByCategoria(int id_categoria) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTOS + " WHERE id_categoria = ?", new String[]{String.valueOf(id_categoria)});
    }

    public int updateProducto(int id_producto, String nombre, double precio,
                              String imagen, int id_categoria, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("precio", precio);
        cv.put("imagen", imagen);
        cv.put("id_categoria", id_categoria);
        cv.put("stock", stock);
        int result = db.update(TABLE_PRODUCTOS, cv, "id_producto = ?", new String[]{String.valueOf(id_producto)});
        return result;
    }

    public int deleteProducto(int id_producto) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PRODUCTOS, "id_producto = ?", new String[]{String.valueOf(id_producto)});
        return result;
    }

    public long insertPedido(String codigo, int id_usuario, double total, String estado, String metodo_pago, String telefonoContacto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("codigo", codigo);
        cv.put("id_usuario", id_usuario);
        cv.put("total", total);
        cv.put("estado", estado);
        cv.put("metodo_pago", metodo_pago);
        cv.put("telefono_contacto", telefonoContacto);
        long result = db.insert(TABLE_PEDIDOS, null, cv);
        return result;
    }

    public Cursor getPedidosByUsuario(int id_usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PEDIDOS + " WHERE id_usuario = ?", new String[]{String.valueOf(id_usuario)});
    }

    public int updateEstadoPedido(String codigo, String estado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", estado);
        int result = db.update(TABLE_PEDIDOS, cv, "codigo = ?", new String[]{codigo});
        return result;
    }

    public long insertDetallePedido(int id_pedido, int id_producto, int cantidad, double subtotal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_pedido", id_pedido);
        cv.put("id_producto", id_producto);
        cv.put("cantidad", cantidad);
        cv.put("subtotal", subtotal);
        long result = db.insert(TABLE_DETALLE_PEDIDO, null, cv);
        return result;
    }

    public Cursor getDetalleByPedido(int id_pedido) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_DETALLE_PEDIDO + " WHERE id_pedido = ?", new String[]{String.valueOf(id_pedido)});
    }

    public long insertBoleta(int id_pedido, double total, String numero_boleta) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_pedido", id_pedido);
        cv.put("total", total);
        cv.put("numero_boleta", numero_boleta);
        long result = db.insert(TABLE_BOLETAS, null, cv);
        return result;
    }

    public Cursor getBoletaByPedido(int id_pedido) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BOLETAS + " WHERE id_pedido = ?", new String[]{String.valueOf(id_pedido)});
    }

    // Se asume que las clases Pedido y CarritoItem están disponibles
    public long guardarPedidoCompleto(Pedido pedido, List<CarritoItem> items) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long idPedidoInsertado = -1;
        double totalCalculado = pedido.getTotal();

        try {
            // Nota: Aquí estás usando un ID de usuario fijo (3) para pedidos.
            // Esto es correcto si el cliente de la app web es siempre el mismo usuario "cliente".
            int id_usuario_anonimo = 3;

            ContentValues cvPedido = new ContentValues();
            cvPedido.put("codigo", String.valueOf(pedido.getIdPedido()));
            cvPedido.put("id_usuario", id_usuario_anonimo);
            cvPedido.put("estado", pedido.getEstado());
            cvPedido.put("metodo_pago", pedido.getTipoEntrega());
            cvPedido.put("telefono_contacto", pedido.getTelefono());
            cvPedido.put("total", totalCalculado);

            idPedidoInsertado = db.insert(TABLE_PEDIDOS, null, cvPedido);

            if (idPedidoInsertado > 0) {
                boolean detallesOk = true;

                for (CarritoItem item : items) {
                    ContentValues cvDetalle = new ContentValues();
                    cvDetalle.put("id_pedido", idPedidoInsertado);
                    cvDetalle.put("id_producto", item.getProductoId());
                    cvDetalle.put("cantidad", item.getCantidad());
                    cvDetalle.put("subtotal", item.getSubtotal());

                    long resultDetalle = db.insert(TABLE_DETALLE_PEDIDO, null, cvDetalle);

                    boolean stockReducido = actualizarStockPorCompra(item.getProductoId(), item.getCantidad());

                    if (resultDetalle <= 0 || !stockReducido) {
                        detallesOk = false;
                        break;
                    }
                }

                if (detallesOk) {
                    db.setTransactionSuccessful();
                } else {
                    idPedidoInsertado = -1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            idPedidoInsertado = -1;
        } finally {
            db.endTransaction();
        }

        return idPedidoInsertado;
    }

    public boolean guardarTelefonoDeCliente(String telefono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        // Nota: Igual que antes, estás actualizando el usuario con ID fijo (3).
        cv.put(COL_USUARIO_TELEFONO, telefono);
        int filasAfectadas = db.update(TABLE_USUARIOS, cv, COL_USUARIO_ID + " = ?", new String[]{"3"});
        return filasAfectadas > 0;
    }

    // Se asume que la clase Producto está disponible
    public Producto getProductoByIdModel(int idProducto) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Producto producto = null;

        try {
            cursor = getProductoById(idProducto);

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_producto"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
                String imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
                int id_categoria = cursor.getInt(cursor.getColumnIndexOrThrow("id_categoria"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));

                producto = new Producto(id, nombre, precio, imagen, stock, id_categoria);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return producto;
    }

    public Cursor getDetallePedidoConNombre(long id_pedido) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " +
                "dp.id_producto, dp.cantidad, dp.subtotal, p.nombre, p.precio " +
                "FROM " + TABLE_DETALLE_PEDIDO + " dp " +
                "JOIN " + TABLE_PRODUCTOS + " p ON dp.id_producto = p.id_producto " +
                "WHERE dp.id_pedido = ?";

        return db.rawQuery(query, new String[]{String.valueOf(id_pedido)});
    }

    // --- MÉTODO AÑADIDO PARA OBTENER INFORMACIÓN COMPLETA DEL PEDIDO ---
    public Cursor getPedidoInfoById(long id_pedido) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT codigo, fecha, id_usuario, total, estado, metodo_pago, telefono_contacto, id_pedido, nombre_cliente FROM " +
                TABLE_PEDIDOS + " WHERE id_pedido = ?";
        return db.rawQuery(query, new String[]{String.valueOf(id_pedido)});
    }
    // ----------------------------------------------------------------------
    // --- MÉTODOS DE LA VENTA FÍSICA Y AUXILIARES DE STOCK ---

    // Se asume que la clase Producto está disponible
    /**
     * Obtiene una lista simplificada de todos los productos para usarse en un Spinner de ventas.
     * @return Lista de objetos Producto.
     */
    public List<Producto> getAllProductsSimple() {
        List<Producto> listaProductos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Solo necesitamos el ID, Nombre, Precio y Stock para el punto de venta
            String query = "SELECT id_producto, nombre, precio, stock FROM " + TABLE_PRODUCTOS;
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_producto"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
                    int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));

                    // Usamos el constructor de Producto con los campos disponibles
                    Producto producto = new Producto(id, nombre, precio, null, stock, -1);
                    listaProductos.add(producto);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error al obtener productos para Spinner: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return listaProductos;
    }

    /**
     * Genera un código de pedido aleatorio de 6 dígitos único.
     * @return Código de pedido.
     */
    public int generateCodigoPedido() {
        int codigo;
        boolean exists;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        do {
            // Genera un número aleatorio entre 100000 y 999999
            codigo = 100000 + (int) (Math.random() * 900000);

            String query = "SELECT codigo FROM " + TABLE_PEDIDOS + " WHERE codigo = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(codigo)});
            exists = cursor.getCount() > 0;
            cursor.close();
        } while (exists);
        return codigo;
    }

    // Se asume que las clases Pedido y CarritoItem están disponibles
    /**
     * Inserta un pedido físico y sus detalles dentro de una transacción, y actualiza el stock.
     */
    public boolean insertPedidoFisico(Pedido pedido, int idUsuarioVendedor) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        long pedidoIdDb = -1;

        try {
            // 1. Insertar en la tabla PEDIDOS
            ContentValues pedidoValues = new ContentValues();
            pedidoValues.put("codigo", pedido.getIdPedido());
            pedidoValues.put("id_usuario", idUsuarioVendedor);
            pedidoValues.put("total", pedido.getTotal());
            pedidoValues.put("estado", pedido.getEstado());
            pedidoValues.put("metodo_pago", pedido.getTipoEntrega());
            pedidoValues.put("telefono_contacto", pedido.getTelefono());
            pedidoValues.put("nombre_cliente", pedido.getNombreCliente());

            pedidoIdDb = db.insert(TABLE_PEDIDOS, null, pedidoValues); // Insertamos y obtenemos el ID interno

            if (pedidoIdDb != -1) {
                // 2. Insertar en la tabla DETALLE_PEDIDO y Actualizar STOCK
                for (CarritoItem item : pedido.getItems()) {
                    ContentValues detalleValues = new ContentValues();
                    detalleValues.put("id_pedido", pedidoIdDb);
                    detalleValues.put("id_producto", item.getProductoId());
                    detalleValues.put("cantidad", item.getCantidad());
                    detalleValues.put("subtotal", item.getSubtotal());

                    long detalleId = db.insert(TABLE_DETALLE_PEDIDO, null, detalleValues);

                    if (detalleId == -1) {
                        throw new Exception("Fallo al insertar detalle.");
                    }

                    // 3. Actualizar STOCK
                    // Usamos el método auxiliar updateStock (que maneja la reducción)
                    updateStockVentasFisicas(db, item.getProductoId(), item.getCantidad());
                }

                db.setTransactionSuccessful(); // Si todo sale bien, confirmamos la transacción
                success = true;
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error al registrar pedido físico: " + e.getMessage());
            success = false;
        } finally {
            db.endTransaction(); // Cerramos la transacción (commit o rollback)
        }
        return success;
    }

    // --- MÉTODOS AUXILIARES DE STOCK PARA VENTA FÍSICA (INTEGRADOS Y CORRECTOS) ---

    /**
     * Método auxiliar para actualizar el stock. Se usa DENTRO de la transacción de venta física.
     */
    private void updateStockVentasFisicas(SQLiteDatabase db, int idProducto, int cantidadVendida) {
        // Obtenemos el stock actual
        int stockActual = getStockByIdVentasFisicas(db, idProducto);
        int nuevoStock = stockActual - cantidadVendida;

        ContentValues values = new ContentValues();
        values.put("stock", nuevoStock);

        // Actualiza el producto
        db.update(TABLE_PRODUCTOS, values, "id_producto = ?", new String[]{String.valueOf(idProducto)});
    }

    /**
     * Método auxiliar para obtener el stock actual de un producto.
     */
    private int getStockByIdVentasFisicas(SQLiteDatabase db, int idProducto) {
        Cursor cursor = null;
        int stock = 0;
        try {
            String query = "SELECT stock FROM " + TABLE_PRODUCTOS + " WHERE id_producto = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(idProducto)});
            if (cursor != null && cursor.moveToFirst()) {
                stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error obteniendo stock: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return stock;
    }

    // --- MÉTODO CORREGIDO ---

    /**
     * Busca el ID de un usuario a partir de su correo electrónico.
     * **CORREGIDO**: Usa las constantes correctas de tabla (TABLE_USUARIOS) y columna (COL_USUARIO_ID, COL_USUARIO_CORREO).
     * @param email El correo electrónico del usuario.
     * @return El ID del usuario, o -1 si no se encuentra.
     */
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;
        Cursor cursor = null;

        try {
            String[] columns = {COL_USUARIO_ID};
            String selection = COL_USUARIO_CORREO + " = ?";
            String[] selectionArgs = {email};

            // Usa TABLE_USUARIOS para que coincida con la creación de la tabla
            cursor = db.query(TABLE_USUARIOS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Obtiene el ID usando la constante de columna
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USUARIO_ID));
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error buscando ID de usuario por email: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userId;
    }
}