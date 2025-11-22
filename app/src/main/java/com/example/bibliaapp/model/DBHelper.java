package com.example.bibliaapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "BibliaAppDB";
    private static final int DB_VERSION = 2;

    private static final String TABLE_USUARIOS = "usuarios";
    public static final String COL_USUARIO_ID = "id_usuario";
    public static final String COL_USUARIO_NOMBRE = "nombre";
    public static final String COL_USUARIO_APELLIDO = "apellido";
    public static final String COL_USUARIO_CORREO = "correo";
    public static final String COL_USUARIO_CONTRASENA = "contraseña";
    public static final String COL_USUARIO_DNI = "dni";
    public static final String COL_USUARIO_TELEFONO = "telefono";
    public static final String COL_USUARIO_DIRECCION = "direccion";
    public static final String COL_USUARIO_ROL = "rol";

    private static final String TABLE_PRODUCTOS = "productos";
    private static final String TABLE_CATEGORIAS = "categorias";
    private static final String TABLE_PEDIDOS = "pedidos";
    private static final String TABLE_DETALLE_PEDIDO = "detalle_pedido";
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
        // Creación de la tabla USUARIOS
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

        // Creación de la tabla CATEGORIAS
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIAS + " (" +
                "id_categoria INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL UNIQUE" +
                ");");

        // Creación de la tabla PRODUCTOS
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

        // Creación de la tabla PEDIDOS
        db.execSQL("CREATE TABLE " + TABLE_PEDIDOS + " (" +
                "id_pedido INTEGER PRIMARY KEY AUTOINCREMENT," +
                "codigo TEXT NOT NULL UNIQUE," +
                "id_usuario INTEGER NOT NULL," +
                "fecha DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "total REAL NOT NULL," +
                "estado TEXT NOT NULL," +
                "metodo_pago TEXT NOT NULL," +
                "telefono_contacto TEXT," +
                "FOREIGN KEY(id_usuario) REFERENCES " + TABLE_USUARIOS + "(" + COL_USUARIO_ID + ")" +
                ");");
        db.execSQL("CREATE INDEX idx_pedidos_usuario ON " + TABLE_PEDIDOS + "(id_usuario);");
        db.execSQL("CREATE INDEX idx_pedidos_codigo ON " + TABLE_PEDIDOS + "(codigo);");

        // Creación de la tabla DETALLE_PEDIDO
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

        // Creación de la tabla BOLETAS
        db.execSQL("CREATE TABLE " + TABLE_BOLETAS + " (" +
                "id_boleta INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_pedido INTEGER NOT NULL," +
                "fecha DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "total REAL NOT NULL," +
                "numero_boleta TEXT UNIQUE," +
                "FOREIGN KEY(id_pedido) REFERENCES " + TABLE_PEDIDOS + "(id_pedido)" +
                ");");
        db.execSQL("CREATE INDEX idx_boletas_pedido ON " + TABLE_BOLETAS + "(id_pedido);");

        // Creación de la tabla empleados
        db.execSQL("CREATE TABLE empleados (" +
                "id_empleado INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "correo TEXT UNIQUE," +
                "telefono TEXT" +
                ");");

        // --- INICIALIZACIÓN DE DATOS (SOLO SE EJECUTA UNA VEZ) ---
        checkAndInsertInitialCategories(db); // Primero categorías
        insertInitialProducts(db);          // Luego productos que dependen de categorías
        checkAndInsertInitialUsers(db);     // Finalmente usuarios
        // ---------------------------------------------------------
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
            // Uso de insertWithOnConflict con CONFLICT_IGNORE para evitar duplicados si se llama dos veces por error
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
        // Corregido a mayúscula inicial para consistencia con los productos
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
                // El método de inserción interno usa la DB que se le pasa
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


    // --- MÉTODOS PÚBLICOS Y EXISTENTES ABAJO (Se eliminó db.close() de todos ellos) ---

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
}