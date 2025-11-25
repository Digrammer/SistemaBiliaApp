package com.example.bibliaapp.model.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.bibliaapp.model.DBHelper;
import com.example.bibliaapp.model.Usuario;

public class UsuarioDao {
    private final DBHelper dbHelper;

    public UsuarioDao(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertUsuario(String nombre, String apellido, String correo, String contraseña,
                              String dni, String telefono, String direccion, String rol) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_USUARIO_NOMBRE, nombre);
        cv.put(DBHelper.COL_USUARIO_APELLIDO, apellido);
        cv.put(DBHelper.COL_USUARIO_CORREO, correo);
        cv.put(DBHelper.COL_USUARIO_CONTRASENA, contraseña);
        cv.put(DBHelper.COL_USUARIO_DNI, dni);
        cv.put(DBHelper.COL_USUARIO_TELEFONO, telefono);
        cv.put(DBHelper.COL_USUARIO_DIRECCION, direccion);
        cv.put(DBHelper.COL_USUARIO_ROL, rol);
        return db.insert(DBHelper.TABLE_USUARIOS, null, cv);
    }

    public Cursor getUsuarioByCorreo(String correo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_USUARIOS + " WHERE " + DBHelper.COL_USUARIO_CORREO + " = ?", new String[]{correo});
    }

    public Cursor getUsuarioById(int id_usuario) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_USUARIOS + " WHERE " + DBHelper.COL_USUARIO_ID + " = ?", new String[]{String.valueOf(id_usuario)});
    }

    public String getRolByCorreo(String correo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DBHelper.COL_USUARIO_ROL + " FROM " + DBHelper.TABLE_USUARIOS + " WHERE " + DBHelper.COL_USUARIO_CORREO + " = ?", new String[]{correo});
        String rol = null;
        if (cursor != null && cursor.moveToFirst()) {
            rol = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_ROL));
            cursor.close();
        }
        return rol;
    }

    public String validateAndGetRol(String correo, String contraseña) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String rol = null;
        try {
            String[] columns = {DBHelper.COL_USUARIO_CONTRASENA, DBHelper.COL_USUARIO_ROL};
            String selection = DBHelper.COL_USUARIO_CORREO + " = ?";
            String[] selectionArgs = {correo};
            cursor = db.query(DBHelper.TABLE_USUARIOS, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String passAlmacenada = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_CONTRASENA));
                if (passAlmacenada.equals(contraseña)) {
                    rol = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_ROL));
                }
            }
        } catch (Exception e) {
            Log.e("UsuarioDao", "validateAndGetRol: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return rol != null ? rol.toLowerCase().trim() : null;
    }

    public boolean validateLogin(String correo, String contraseña) {
        Cursor cursor = getUsuarioByCorreo(correo);
        boolean isValid = false;
        if (cursor != null && cursor.moveToFirst()) {
            String pass = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_CONTRASENA));
            isValid = pass.equals(contraseña);
            cursor.close();
        }
        return isValid;
    }

    public int updateUsuario(int id_usuario, String nombre, String apellido, String correo,
                             String contraseña, String dni, String telefono, String direccion, String rol) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_USUARIO_NOMBRE, nombre);
        cv.put(DBHelper.COL_USUARIO_APELLIDO, apellido);
        cv.put(DBHelper.COL_USUARIO_CORREO, correo);
        cv.put(DBHelper.COL_USUARIO_CONTRASENA, contraseña);
        cv.put(DBHelper.COL_USUARIO_DNI, dni);
        cv.put(DBHelper.COL_USUARIO_TELEFONO, telefono);
        cv.put(DBHelper.COL_USUARIO_DIRECCION, direccion);
        cv.put(DBHelper.COL_USUARIO_ROL, rol);
        return db.update(DBHelper.TABLE_USUARIOS, cv, DBHelper.COL_USUARIO_ID + " = ?", new String[]{String.valueOf(id_usuario)});
    }

    public int deleteUsuario(int id_usuario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DBHelper.TABLE_USUARIOS, DBHelper.COL_USUARIO_ID + " = ?", new String[]{String.valueOf(id_usuario)});
    }

    public void checkAndInsertInitialUsers(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_USUARIOS, null);
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

    public long insertUsuarioInterno(SQLiteDatabase db, String nombre, String apellido, String correo, String contraseña,
                                     String dni, String telefono, String direccion, String rol) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_USUARIO_NOMBRE, nombre);
        cv.put(DBHelper.COL_USUARIO_APELLIDO, apellido);
        cv.put(DBHelper.COL_USUARIO_CORREO, correo);
        cv.put(DBHelper.COL_USUARIO_CONTRASENA, contraseña);
        cv.put(DBHelper.COL_USUARIO_DNI, dni);
        cv.put(DBHelper.COL_USUARIO_TELEFONO, telefono);
        cv.put(DBHelper.COL_USUARIO_DIRECCION, direccion);
        cv.put(DBHelper.COL_USUARIO_ROL, rol);
        return db.insert(DBHelper.TABLE_USUARIOS, null, cv);
    }

    public boolean guardarTelefonoDeCliente(String telefono) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COL_USUARIO_TELEFONO, telefono);
        int filasAfectadas = db.update(DBHelper.TABLE_USUARIOS, cv, DBHelper.COL_USUARIO_ID + " = ?", new String[]{"3"});
        return filasAfectadas > 0;
    }

    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int userId = -1;
        Cursor cursor = null;

        try {
            String[] columns = {DBHelper.COL_USUARIO_ID};
            String selection = DBHelper.COL_USUARIO_CORREO + " = ?";
            String[] selectionArgs = {email};
            cursor = db.query(DBHelper.TABLE_USUARIOS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_USUARIO_ID));
            }
        } catch (Exception e) {
            Log.e("UsuarioDao", "getUserIdByEmail: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return userId;
    }
}
