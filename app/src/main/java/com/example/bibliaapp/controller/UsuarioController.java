package com.example.bibliaapp.controller;

import android.content.Context;

import com.example.bibliaapp.model.DBHelper;

public class UsuarioController {

    private final DBHelper dbHelper;

    public UsuarioController(Context context) {
        dbHelper = new DBHelper(context);
    }

    public String getRolPorCorreo(String correo) {
        return dbHelper.getRolByCorreo(correo);
    }

    public boolean registrar(String nombre, String apellido, String correo, String contrasena, String dni, String telefono, String direccion) {
        long id = dbHelper.insertUsuario(nombre, apellido, correo, contrasena, dni, telefono, direccion, "cliente");
        return id != -1;
    }

    public boolean login(String correo, String contrasena) {
        return dbHelper.validateLogin(correo, contrasena);
    }
}