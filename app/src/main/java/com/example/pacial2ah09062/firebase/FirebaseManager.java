package com.example.pacial2ah09062.firebase;

import android.util.Log;

import com.example.pacial2ah09062.database.entity.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.os.Looper;

public class FirebaseManager {
    
    private static final String TAG = "FirebaseManager";
    private static final String USERS_COLLECTION = "users";
    private static final long FIREBASE_TIMEOUT_MS = 5000; // 5 segundos timeout
    
    private FirebaseFirestore db;
    private Handler timeoutHandler;
    
    public FirebaseManager() {
        db = FirebaseFirestore.getInstance();
        timeoutHandler = new Handler(Looper.getMainLooper());
        
        // Configurar Firestore para mejor comportamiento offline
        db.enableNetwork();
    }
    
    /**
     * Interface para callbacks de Firebase
     */
    public interface FirebaseCallback {
        void onSuccess();
        void onFailure(String error);
    }
    
    /**
     * Interface para callbacks de búsqueda de usuario
     */
    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }
    
    /**
     * Guarda un usuario en Firebase Firestore
     * @param user Usuario a guardar
     * @param callback Callback para manejar el resultado
     */
    public void saveUser(User user, FirebaseCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getFullName());
        userData.put("password", user.getPassword()); // En producción, usar hash
        userData.put("lastUpdated", user.getLastUpdated());
        
        // Flag para controlar si ya se ejecutó el callback
        final boolean[] callbackExecuted = {false};
        
        // Configurar timeout
        Runnable timeoutRunnable = () -> {
            synchronized (callbackExecuted) {
                if (!callbackExecuted[0]) {
                    callbackExecuted[0] = true;
                    Log.w(TAG, "Timeout guardando usuario en Firebase: " + user.getEmail());
                    callback.onFailure("Sin conexión a internet - usuario guardado localmente");
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, FIREBASE_TIMEOUT_MS);
        
        db.collection(USERS_COLLECTION)
                .document(user.getEmail())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.d(TAG, "Usuario guardado exitosamente en Firebase: " + user.getEmail());
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.e(TAG, "Error guardando usuario en Firebase", e);
                            callback.onFailure(e.getMessage());
                        }
                    }
                });
    }
    
    /**
     * Obtiene un usuario de Firebase por email
     * @param email Email del usuario a buscar
     * @param callback Callback para manejar el resultado
     */
    public void getUser(String email, UserCallback callback) {
        // Flag para controlar si ya se ejecutó el callback
        final boolean[] callbackExecuted = {false};
        
        // Configurar timeout
        Runnable timeoutRunnable = () -> {
            synchronized (callbackExecuted) {
                if (!callbackExecuted[0]) {
                    callbackExecuted[0] = true;
                    Log.w(TAG, "Timeout buscando usuario en Firebase: " + email);
                    callback.onFailure("Sin conexión a internet");
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, FIREBASE_TIMEOUT_MS);
        
        db.collection(USERS_COLLECTION)
                .document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            
                            if (documentSnapshot.exists()) {
                                User user = documentToUser(documentSnapshot);
                                if (user != null) {
                                    Log.d(TAG, "Usuario encontrado en Firebase: " + email);
                                    callback.onSuccess(user);
                                } else {
                                    Log.e(TAG, "Error convertiendo documento a Usuario");
                                    callback.onFailure("Error procesando datos del usuario");
                                }
                            } else {
                                Log.d(TAG, "Usuario no encontrado en Firebase: " + email);
                                callback.onFailure("Usuario no encontrado");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.e(TAG, "Error buscando usuario en Firebase", e);
                            callback.onFailure(e.getMessage());
                        }
                    }
                });
    }
    
    /**
     * Actualiza un usuario en Firebase
     * @param user Usuario con los datos actualizados
     * @param callback Callback para manejar el resultado
     */
    public void updateUser(User user, FirebaseCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", user.getFullName());
        userData.put("lastUpdated", System.currentTimeMillis());
        
        // Flag para controlar si ya se ejecutó el callback
        final boolean[] callbackExecuted = {false};
        
        // Configurar timeout
        Runnable timeoutRunnable = () -> {
            synchronized (callbackExecuted) {
                if (!callbackExecuted[0]) {
                    callbackExecuted[0] = true;
                    Log.w(TAG, "Timeout actualizando usuario en Firebase: " + user.getEmail());
                    callback.onFailure("Sin conexión a internet - cambios guardados localmente");
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, FIREBASE_TIMEOUT_MS);
        
        db.collection(USERS_COLLECTION)
                .document(user.getEmail())
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.d(TAG, "Usuario actualizado exitosamente en Firebase: " + user.getEmail());
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    synchronized (callbackExecuted) {
                        if (!callbackExecuted[0]) {
                            callbackExecuted[0] = true;
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.e(TAG, "Error actualizando usuario en Firebase", e);
                            callback.onFailure(e.getMessage());
                        }
                    }
                });
    }
    
    /**
     * Verifica si un usuario existe en Firebase
     * @param email Email del usuario a verificar
     * @param callback Callback para manejar el resultado
     */
    public void userExists(String email, FirebaseCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Usuario existe en Firebase: " + email);
                        callback.onSuccess();
                    } else {
                        Log.d(TAG, "Usuario no existe en Firebase: " + email);
                        callback.onFailure("Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verificando usuario en Firebase", e);
                    callback.onFailure(e.getMessage());
                });
    }
    
    /**
     * Autentica un usuario verificando email y contraseña
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param callback Callback para manejar el resultado
     */
    public void authenticateUser(String email, String password, UserCallback callback) {
        getUser(email, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user.getPassword().equals(password)) {
                    Log.d(TAG, "Usuario autenticado exitosamente: " + email);
                    callback.onSuccess(user);
                } else {
                    Log.d(TAG, "Contraseña incorrecta para: " + email);
                    callback.onFailure("Credenciales inválidas");
                }
            }
            
            @Override
            public void onFailure(String error) {
                Log.d(TAG, "Error en autenticación: " + error);
                callback.onFailure("Credenciales inválidas");
            }
        });
    }
    
    /**
     * Convierte un DocumentSnapshot de Firebase a objeto User
     * @param document DocumentSnapshot de Firebase
     * @return Objeto User o null si hay error
     */
    private User documentToUser(DocumentSnapshot document) {
        try {
            String email = document.getString("email");
            String fullName = document.getString("fullName");
            String password = document.getString("password");
            Long lastUpdated = document.getLong("lastUpdated");
            
            if (email != null && fullName != null && password != null) {
                User user = new User(email, fullName, password);
                if (lastUpdated != null) {
                    user.setLastUpdated(lastUpdated);
                }
                return user;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo documento a User", e);
        }
        return null;
    }
}