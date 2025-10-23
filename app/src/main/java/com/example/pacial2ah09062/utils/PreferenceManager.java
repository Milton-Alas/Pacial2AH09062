package com.example.pacial2ah09062.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    
    private static final String PREF_NAME = "UserPreferences";
    private static final String KEY_REMEMBER_USER = "remember_user";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_CURRENT_USER_EMAIL = "current_user_email";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    /**
     * Guarda las credenciales del usuario si ha marcado "Recordar usuario"
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param rememberUser Si debe recordar las credenciales
     */
    public void saveUserCredentials(String email, String password, boolean rememberUser) {
        editor.putBoolean(KEY_REMEMBER_USER, rememberUser);
        
        if (rememberUser) {
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putString(KEY_SAVED_PASSWORD, password);
        } else {
            // Si no quiere recordar, limpiar las credenciales guardadas
            editor.remove(KEY_SAVED_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
        }
        
        editor.apply();
    }
    
    /**
     * Obtiene el email guardado si existe
     * @return Email guardado o cadena vacía si no existe
     */
    public String getSavedEmail() {
        return sharedPreferences.getString(KEY_SAVED_EMAIL, "");
    }
    
    /**
     * Obtiene la contraseña guardada si existe
     * @return Contraseña guardada o cadena vacía si no existe
     */
    public String getSavedPassword() {
        return sharedPreferences.getString(KEY_SAVED_PASSWORD, "");
    }
    
    /**
     * Verifica si el usuario ha marcado "Recordar usuario"
     * @return true si debe recordar, false si no
     */
    public boolean shouldRememberUser() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_USER, false);
    }
    
    /**
     * Marca al usuario como logueado y guarda su email
     * @param userEmail Email del usuario logueado
     */
    public void setUserLoggedIn(String userEmail) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_CURRENT_USER_EMAIL, userEmail);
        editor.apply();
    }
    
    /**
     * Verifica si hay un usuario logueado
     * @return true si hay usuario logueado, false si no
     */
    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Obtiene el email del usuario actualmente logueado
     * @return Email del usuario logueado o cadena vacía si no hay usuario logueado
     */
    public String getCurrentUserEmail() {
        return sharedPreferences.getString(KEY_CURRENT_USER_EMAIL, "");
    }
    
    /**
     * Cierra sesión del usuario y limpia las preferencias de sesión
     * Mantiene las credenciales guardadas si el usuario eligió recordarlas
     */
    public void logout() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_CURRENT_USER_EMAIL);
        editor.apply();
    }
    
    /**
     * Limpia todas las preferencias (logout completo)
     */
    public void clearAllPreferences() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Verifica si hay credenciales guardadas válidas
     * @return true si hay email y contraseña guardados, false si no
     */
    public boolean hasValidSavedCredentials() {
        String savedEmail = getSavedEmail();
        String savedPassword = getSavedPassword();
        
        return !savedEmail.isEmpty() && !savedPassword.isEmpty() && shouldRememberUser();
    }
}