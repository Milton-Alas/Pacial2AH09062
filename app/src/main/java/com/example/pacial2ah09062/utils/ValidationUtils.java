package com.example.pacial2ah09062.utils;

import android.util.Patterns;

public class ValidationUtils {
    
    /**
     * Valida que el email tenga formato correcto
     * @param email Email a validar
     * @return true si es válido, false si no
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }
    
    /**
     * Valida que la contraseña tenga al menos 8 caracteres
     * @param password Contraseña a validar
     * @return true si es válida, false si no
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }
    
    /**
     * Valida que el nombre tenga al menos 7 caracteres
     * @param fullName Nombre completo a validar
     * @return true si es válido, false si no
     */
    public static boolean isValidFullName(String fullName) {
        return fullName != null && fullName.trim().length() >= 7;
    }
    
    /**
     * Obtiene mensaje de error para email inválido
     * @return Mensaje de error
     */
    public static String getEmailErrorMessage() {
        return "Por favor ingrese un email válido";
    }
    
    /**
     * Obtiene mensaje de error para contraseña inválida
     * @return Mensaje de error
     */
    public static String getPasswordErrorMessage() {
        return "La contraseña debe tener al menos 8 caracteres";
    }
    
    /**
     * Obtiene mensaje de error para nombre inválido
     * @return Mensaje de error
     */
    public static String getFullNameErrorMessage() {
        return "El nombre completo debe tener al menos 7 caracteres";
    }
    
    /**
     * Valida todos los campos de registro
     * @param email Email del usuario
     * @param fullName Nombre completo del usuario
     * @param password Contraseña del usuario
     * @return Mensaje de error o null si todo es válido
     */
    public static String validateRegistrationFields(String email, String fullName, String password) {
        if (!isValidEmail(email)) {
            return getEmailErrorMessage();
        }
        
        if (!isValidFullName(fullName)) {
            return getFullNameErrorMessage();
        }
        
        if (!isValidPassword(password)) {
            return getPasswordErrorMessage();
        }
        
        return null; // Todo válido
    }
    
    /**
     * Valida campos de login
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return Mensaje de error o null si todo es válido
     */
    public static String validateLoginFields(String email, String password) {
        if (!isValidEmail(email)) {
            return getEmailErrorMessage();
        }
        
        if (!isValidPassword(password)) {
            return getPasswordErrorMessage();
        }
        
        return null; // Todo válido
    }
}