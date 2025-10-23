package com.example.pacial2ah09062.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.entity.User;
import com.example.pacial2ah09062.repository.UserRepository;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Inicializar repositorio
        userRepository = UserRepository.getInstance(this);
        
        // Probar funcionalidades del repositorio
        testRepositoryOperations();
    }
    
    private void testRepositoryOperations() {
        Log.d(TAG, "=== INICIANDO PRUEBAS DEL REPOSITORY ===");
        
        // 1. Crear usuario de prueba por defecto para login
        User defaultUser = new User("admin@test.com", "Usuario Administrador", "admin123");
        
        userRepository.registerUser(defaultUser, new UserRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Usuario por defecto creado: admin@test.com / admin123");
            }
            
            @Override
            public void onFailure(String error) {
                Log.d(TAG, "ℹ️ Usuario por defecto ya existe o error: " + error);
            }
        });
        
        // 2. Probar registro de usuario adicional
        User testUser = new User("test@example.com", "Test User Complete", "testpass123");
        
        userRepository.registerUser(testUser, new UserRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Usuario registrado exitosamente");
                
                // 2. Probar autenticación
                userRepository.authenticateUser("test@example.com", "testpass123", 
                    new UserRepository.AuthCallback() {
                        @Override
                        public void onSuccess(User user) {
                            Log.d(TAG, "✅ Usuario autenticado: " + user.getFullName());
                            
                            // 3. Probar actualización de perfil
                            userRepository.updateUserProfile(user.getEmail(), "Test User Updated", 
                                new UserRepository.RepositoryCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "✅ Perfil actualizado exitosamente");
                                        
                                        // 4. Probar sincronización pendiente
                                        userRepository.syncPendingUsers(new UserRepository.RepositoryCallback() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d(TAG, "✅ Sincronización completada");
                                                Log.d(TAG, "=== TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE ===");
                                            }
                                            
                                            @Override
                                            public void onFailure(String error) {
                                                Log.w(TAG, "⚠️ Sincronización con errores: " + error);
                                                Log.d(TAG, "=== PRUEBAS COMPLETADAS (con advertencias) ===");
                                            }
                                        });
                                    }
                                    
                                    @Override
                                    public void onFailure(String error) {
                                        Log.e(TAG, "❌ Error actualizando perfil: " + error);
                                    }
                                });
                        }
                        
                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "❌ Error en autenticación: " + error);
                        }
                    });
            }
            
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Error registrando usuario: " + error);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRepository != null) {
            userRepository.cleanup();
        }
    }
}
