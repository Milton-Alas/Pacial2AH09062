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
     * Valida que la contraseña sea fuerte:
     * - Al menos 8 caracteres
     * - Al menos una letra
     * - Al menos un número
     * - Puede incluir caracteres especiales
     * @param password Contraseña a validar
     * @return true si es válida, false si no
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Verificar que contenga al menos una letra
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        
        // Verificar que contenga al menos un número
        boolean hasNumber = password.matches(".*[0-9].*");
        
        return hasLetter && hasNumber;
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
        return "La contraseña debe tener al menos 8 caracteres, incluyendo letras y números";
    }
    
    /**
     * Obtiene mensaje de error para nombre inválido
     * @return Mensaje de error
     */
    public static String getFullNameErrorMessage() {
        return "El nombre completo debe tener al menos 7 caracteres";
    }
    
    
    // ================== MÉTODOS ADICIONALES DE VALIDACIÓN ==================
    
    /**
     * Obtiene un mensaje específico de qué falta en la contraseña
     * @param password Contraseña a analizar
     * @return Mensaje específico del problema, o null si es válida
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "La contraseña no puede estar vacía";
        }
        
        if (password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres (actual: " + password.length() + ")";
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        
        if (!hasLetter && !hasNumber) {
            return "La contraseña debe incluir al menos una letra y un número";
        }
        
        if (!hasLetter) {
            return "La contraseña debe incluir al menos una letra (a-z, A-Z)";
        }
        
        if (!hasNumber) {
            return "La contraseña debe incluir al menos un número (0-9)";
        }
        
        return null; // Es válida
    }
    
}
