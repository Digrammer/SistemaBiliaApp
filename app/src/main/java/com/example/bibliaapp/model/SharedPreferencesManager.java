package com.example.bibliaapp.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    // CONSTANTES
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROL = "userRol";
    private static final String ROL_VISITANTE_DEFAULT = "visitante"; // Constante de seguridad

    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SharedPreferencesManager(Context context) {
        // Inicialización
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            // Usamos getApplicationContext() para evitar fugas de memoria si se llama desde Activity
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Guarda el ID, Correo y ROL de la sesión del usuario.
     * El ROL se guarda SIEMPRE en minúsculas para consistencia.
     */
    public void saveUserSession(int userId, String userEmail, String userRol) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_ROL, userRol.toLowerCase()); // << AQUI ESTA EL BLINDAJE >>
        editor.apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Obtiene el rol del usuario, siempre en minúsculas, por defecto retorna "visitante".
     */
    public String getUserRol() {
        // CAMBIO MENOR: Usamos la constante ROL_VISITANTE_DEFAULT para el valor por defecto.
        String rol = sharedPreferences.getString(KEY_USER_ROL, ROL_VISITANTE_DEFAULT);
        return rol.toLowerCase();
    }

    /**
     * Cierra la sesión y limpia todos los datos.
     * (Corregido el nombre del método para resolver 'Cannot resolve method clearUserSession')
     */
    public void clearUserSession() {
        editor.clear();
        editor.apply();
    }
}