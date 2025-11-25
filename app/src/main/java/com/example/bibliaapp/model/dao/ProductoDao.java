package com.example.bibliaapp.model.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Producto;

import java.util.ArrayList;
import java.util.List;

public class ProductoDao {
    private final DBHelper dbHelper;

    public ProductoDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public int getProductoCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_PRODUCTOS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getStockProducto(int idProducto) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int stock = 0;
        Cursor cursor = db.rawQuery("SELECT stock FROM " + DBHelper.TABLE_PRODUCTOS + " WHERE id_producto = ?", new String[]{String.valueOf(idProducto)});
        if (cursor != null && cursor.moveToFirst()) {
            stock = cursor.getInt(0);
            cursor.close();
        }
        return stock;
    }

    public boolean actualizarStockPorCompra(int idProducto, int cantidadComprada) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int stockActual = getStockProducto(idProducto);
        int nuevoStock = stockActual - cantidadComprada;

        if (nuevoStock < 0) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put("stock", nuevoStock);

        int filasAfectadas = db.update(DBHelper.TABLE_PRODUCTOS, values, "id_producto = ?", new String[]{String.valueOf(idProducto)});
        return filasAfectadas > 0;
    }

    public boolean actualizarStock(int idProducto, int cantidadAñadir) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int stockActual = getStockProducto(idProducto);
        int nuevoStock = stockActual + cantidadAñadir;
        if (nuevoStock < 0) return false;
        ContentValues values = new ContentValues();
        values.put("stock", nuevoStock);
        int filasAfectadas = db.update(DBHelper.TABLE_PRODUCTOS, values, "id_producto = ?", new String[]{String.valueOf(idProducto)});
        return filasAfectadas > 0;
    }

    public long insertProducto(String nombre, double precio, String imagen, int id_categoria, int stock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("precio", precio);
        cv.put("imagen", imagen);
        cv.put("id_categoria", id_categoria);
        cv.put("stock", stock);
        return db.insert(DBHelper.TABLE_PRODUCTOS, null, cv);
    }

    public Cursor getProductoById(int id_producto) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_PRODUCTOS + " WHERE id_producto = ?", new String[]{String.valueOf(id_producto)});
    }

    public Producto getProductoByIdObject(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Producto producto = null;
        try {
            cursor = db.query(
                    DBHelper.TABLE_PRODUCTOS,
                    new String[]{"id_producto", "nombre", "precio", "imagen", "id_categoria", "stock"},
                    "id_producto" + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int prodId = cursor.getInt(cursor.getColumnIndexOrThrow("id_producto"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
                String imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
                int idCategoria = cursor.getInt(cursor.getColumnIndexOrThrow("id_categoria"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));

                producto = new Producto(prodId, nombre, precio, imagen, stock, idCategoria);
            }
        } catch (Exception e) {
            Log.e("ProductoDao", "getProductoByIdObject: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return producto;
    }

    public Producto getProductoByIdModel(int idProducto) {
        return getProductoByIdObject(idProducto);
    }

    public List<Producto> getAllProductosList() {
        List<Producto> listaProductos = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DBHelper.TABLE_PRODUCTOS;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
                    Log.e("ProductoDao", "getAllProductosList: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return listaProductos;
    }

    public List<Producto> getProductosByCategoriaList(int id_categoria) {
        List<Producto> listaProductos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_PRODUCTOS + " WHERE id_categoria = ?", new String[]{String.valueOf(id_categoria)});

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
                    Log.e("ProductoDao", "getProductosByCategoriaList: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) cursor.close();
        return listaProductos;
    }

    public Cursor getAllProductos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_PRODUCTOS, null);
    }

    public Cursor getProductosByCategoria(int id_categoria) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_PRODUCTOS + " WHERE id_categoria = ?", new String[]{String.valueOf(id_categoria)});
    }

    public int updateProducto(int id_producto, String nombre, double precio, String imagen, int id_categoria, int stock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("precio", precio);
        cv.put("imagen", imagen);
        cv.put("id_categoria", id_categoria);
        cv.put("stock", stock);
        return db.update(DBHelper.TABLE_PRODUCTOS, cv, "id_producto = ?", new String[]{String.valueOf(id_producto)});
    }

    public int deleteProducto(int id_producto) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DBHelper.TABLE_PRODUCTOS, "id_producto = ?", new String[]{String.valueOf(id_producto)});
    }

    // Categorias helpers
    public long insertCategoria(String nombre) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        return db.insert(DBHelper.TABLE_CATEGORIAS, null, cv);
    }

    public int getCategoriaIdByNombre(String nombre) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id_categoria FROM " + DBHelper.TABLE_CATEGORIAS + " WHERE nombre = ?", new String[]{nombre});
        int id = -1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(0);
            cursor.close();
        }
        return id;
    }

    public List<String> getAllCategorias() {
        List<String> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre FROM " + DBHelper.TABLE_CATEGORIAS, null);
        if (cursor.moveToFirst()) {
            do {
                lista.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // Métodos para inicialización (puedes mantenerlos en DBHelper si prefieres)
    public void insertProductoInicial(SQLiteDatabase db, String nombre, double precio, String imagen, String categoriaNombre, int stock) {
        db.execSQL("INSERT OR IGNORE INTO " + DBHelper.TABLE_CATEGORIAS + " (nombre) VALUES (?)", new String[]{categoriaNombre});
        Cursor cursor = db.rawQuery("SELECT id_categoria FROM " + DBHelper.TABLE_CATEGORIAS + " WHERE nombre = ?", new String[]{categoriaNombre});
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
            db.insertWithOnConflict(DBHelper.TABLE_PRODUCTOS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void insertInitialProducts(SQLiteDatabase db) {
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

    public List<Producto> getAllProductsSimple() {
        List<Producto> listaProductos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT id_producto, nombre, precio, stock FROM " + DBHelper.TABLE_PRODUCTOS;
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id_producto"));
                    String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                    double precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"));
                    int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                    Producto producto = new Producto(id, nombre, precio, null, stock, -1);
                    listaProductos.add(producto);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ProductoDao", "getAllProductsSimple: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return listaProductos;
    }
}
