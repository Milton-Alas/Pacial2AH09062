package com.example.pacial2ah09062.repository;

import android.content.Context;
import android.util.Log;

import com.example.pacial2ah09062.database.AppDatabase;
import com.example.pacial2ah09062.database.entity.User;
import com.example.pacial2ah09062.database.dao.UserDAO;
import com.example.pacial2ah09062.firebase.FirebaseManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository que maneja las operaciones de datos de Usuario
 * Coordina entre la base de datos local (Room) y remota (Firebase)
 */
public class UserRepository {
    
    private static final String TAG = "UserRepository";
    private static volatile UserRepository INSTANCE;
    
    private UserDAO userDAO;
    private FirebaseManager firebaseManager;
    private ExecutorService executor;
    
    public UserRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        userDAO = database.userDAO();
        firebaseManager = new FirebaseManager();
        executor = Executors.newFixedThreadPool(4);
    }
    
    public static UserRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UserRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }
    
    // ================== INTERFACES PARA CALLBACKS ==================
    
    public interface RepositoryCallback {
        void onSuccess();
        void onFailure(String error);
    }
    
    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }
    
    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }
    
    // ================== OPERACIONES DE REGISTRO ==================
    
    /**
     * Registra un nuevo usuario (guarda en local y sincroniza con Firebase)
     */
    public void registerUser(User user, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                // 1. Verificar si el usuario ya existe localmente
                User existingUser = userDAO.getUserByEmail(user.getEmail());
                if (existingUser != null) {
                    callback.onFailure("El usuario ya existe");
                    return;
                }
                
                // 2. Guardar en base de datos local
                user.setPendingSync(true); // Marcar como pendiente de sincronización
                userDAO.insertUser(user);
                Log.d(TAG, "Usuario guardado localmente: " + user.getEmail());
                
                // 3. Intentar sincronizar con Firebase
                syncUserToFirebase(user, new RepositoryCallback() {
                    @Override
                    public void onSuccess() {
                        // Actualizar estado de sincronización
                        executor.execute(() -> {
                            userDAO.updateSyncStatus(user.getEmail(), false);
                            Log.d(TAG, "Usuario sincronizado exitosamente: " + user.getEmail());
                        });
                        callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        Log.w(TAG, "No se pudo sincronizar con Firebase, usuario guardado localmente: " + error);
                        // El usuario ya está guardado localmente, así que es un éxito parcial
                        callback.onSuccess();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error registrando usuario", e);
                callback.onFailure("Error interno del sistema");
            }
        });
    }

    /**
     * Crea un nuevo usuario, usado principalmente para login social donde el usuario ya está autenticado.
     */
    public void createUser(User user, boolean syncToFirebase, AuthCallback callback) {
        executor.execute(() -> {
            try {
                user.setPendingSync(syncToFirebase);
                userDAO.insertUser(user);
                Log.d(TAG, "Usuario creado localmente: " + user.getEmail());

                if (syncToFirebase) {
                    syncUserToFirebase(user, new RepositoryCallback() {
                        @Override
                        public void onSuccess() {
                            executor.execute(() -> userDAO.updateSyncStatus(user.getEmail(), false));
                            callback.onSuccess(user);
                        }

                        @Override
                        public void onFailure(String error) {
                            // Todavía es un éxito porque el usuario se creó localmente
                            callback.onSuccess(user);
                        }
                    });
                } else {
                    callback.onSuccess(user);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creando usuario", e);
                callback.onFailure("No se pudo crear el usuario localmente.");
            }
        });
    }
    
    // ================== OPERACIONES DE AUTENTICACIÓN ==================
    
    /**
     * Autentica un usuario (verifica primero localmente, luego Firebase si es necesario)
     */
    public void authenticateUser(String email, String password, AuthCallback callback) {
        executor.execute(() -> {
            try {
                // 1. Verificar localmente primero
                User localUser = userDAO.getUserByEmail(email);
                
                if (localUser != null && localUser.getPassword().equals(password)) {
                    Log.d(TAG, "Usuario autenticado localmente: " + email);
                    callback.onSuccess(localUser);
                    return;
                }
                
                // 2. Si no está local o la contraseña no coincide, verificar en Firebase
                firebaseManager.authenticateUser(email, password, new FirebaseManager.UserCallback() {
                    @Override
                    public void onSuccess(User firebaseUser) {
                        Log.d(TAG, "Usuario autenticado en Firebase: " + email);
                        
                        // Guardar/actualizar en base local
                        executor.execute(() -> {
                            firebaseUser.setPendingSync(false);
                            if (localUser != null) {
                                userDAO.updateUser(firebaseUser);
                            } else {
                                userDAO.insertUser(firebaseUser);
                            }
                        });
                        
                        callback.onSuccess(firebaseUser);
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        Log.d(TAG, "Autenticación fallida: " + error);
                        callback.onFailure("Credenciales inválidas");
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error en autenticación", e);
                callback.onFailure("Error interno del sistema");
            }
        });
    }
    
    // ================== OPERACIONES DE ACTUALIZACIÓN ==================
    
    /**
     * Actualiza el perfil de usuario (local y Firebase)
     */
    public void updateUserProfile(String email, String newFullName, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                // 1. Obtener usuario local
                User user = userDAO.getUserByEmail(email);
                if (user == null) {
                    callback.onFailure("Usuario no encontrado");
                    return;
                }
                
                // 2. Actualizar datos localmente
                user.setFullName(newFullName);
                user.setLastUpdated(System.currentTimeMillis());
                user.setPendingSync(true);
                
                userDAO.updateUser(user);
                Log.d(TAG, "Perfil actualizado localmente: " + email);
                
                // 3. Sincronizar con Firebase
                firebaseManager.updateUser(user, new FirebaseManager.FirebaseCallback() {
                    @Override
                    public void onSuccess() {
                        executor.execute(() -> {
                            userDAO.updateSyncStatus(email, false);
                            Log.d(TAG, "Perfil sincronizado exitosamente: " + email);
                        });
                        callback.onSuccess();
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        Log.w(TAG, "No se pudo sincronizar perfil con Firebase: " + error);
                        // Los datos ya están guardados localmente
                        callback.onSuccess();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error actualizando perfil", e);
                callback.onFailure("Error interno del sistema");
            }
        });
    }
    
    // ================== OPERACIONES DE SINCRONIZACIÓN ==================
    
    /**
     * Sincroniza todos los usuarios pendientes con Firebase
     */
    public void syncPendingUsers(RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                List<User> pendingUsers = userDAO.getUsersPendingSync();
                
                if (pendingUsers.isEmpty()) {
                    Log.d(TAG, "No hay usuarios pendientes de sincronización");
                    callback.onSuccess();
                    return;
                }
                
                Log.d(TAG, "Sincronizando " + pendingUsers.size() + " usuarios pendientes");
                
                final int[] pendingCount = {pendingUsers.size()};
                final boolean[] hasErrors = {false};
                
                for (User user : pendingUsers) {
                    syncUserToFirebase(user, new RepositoryCallback() {
                        @Override
                        public void onSuccess() {
                            executor.execute(() -> {
                                userDAO.updateSyncStatus(user.getEmail(), false);
                            });
                            
                            synchronized (pendingCount) {
                                pendingCount[0]--;
                                if (pendingCount[0] == 0) {
                                    if (hasErrors[0]) {
                                        callback.onFailure("Algunos usuarios no se pudieron sincronizar");
                                    } else {
                                        callback.onSuccess();
                                    }
                                }
                            }
                        }
                        
                        @Override
                        public void onFailure(String error) {
                            hasErrors[0] = true;
                            synchronized (pendingCount) {
                                pendingCount[0]--;
                                if (pendingCount[0] == 0) {
                                    callback.onFailure("Algunos usuarios no se pudieron sincronizar");
                                }
                            }
                        }
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error en sincronización", e);
                callback.onFailure("Error interno del sistema");
            }
        });
    }
    
    /**
     * Obtiene un usuario por email (primero local, luego Firebase si es necesario)
     */
    public void getUserByEmail(String email, UserCallback callback) {
        executor.execute(() -> {
            try {
                // Verificar localmente primero
                User localUser = userDAO.getUserByEmail(email);
                if (localUser != null) {
                    callback.onSuccess(localUser);
                    return;
                }
                
                // Si no está local, buscar en Firebase
                firebaseManager.getUser(email, new FirebaseManager.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        // Guardar en local para próximas consultas
                        executor.execute(() -> {
                            user.setPendingSync(false);
                            userDAO.insertUser(user);
                        });
                        callback.onSuccess(user);
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error obteniendo usuario", e);
                callback.onFailure("Error interno del sistema");
            }
        });
    }
    
    // ================== MÉTODOS PRIVADOS ==================
    
    private void syncUserToFirebase(User user, RepositoryCallback callback) {
        firebaseManager.saveUser(user, new FirebaseManager.FirebaseCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Usuario sincronizado con Firebase: " + user.getEmail());
                if (callback != null) callback.onSuccess();
            }
            
            @Override
            public void onFailure(String error) {
                Log.w(TAG, "Error sincronizando con Firebase: " + error);
                // No marcar como error crítico si es problema de conexión
                if (error.contains("Sin conexión") || error.contains("timeout") || error.contains("network")) {
                    Log.d(TAG, "Error de conectividad, usuario queda pendiente de sincronización: " + user.getEmail());
                }
                if (callback != null) callback.onFailure(error);
            }
        });
    }
    
    // ================== MÉTODOS DE LIMPIEZA ==================
    
    public void cleanup() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}