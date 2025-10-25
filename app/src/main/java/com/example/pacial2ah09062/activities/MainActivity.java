package com.example.pacial2ah09062.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.repository.UserRepository;
import com.google.firebase.FirebaseApp;

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
        
        // Verificar Firebase
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "✅ Firebase inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando Firebase: " + e.getMessage(), e);
        }
        
        // Inicializar repositorio
        userRepository = UserRepository.getInstance(this);
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRepository != null) {
            userRepository.cleanup();
        }
    }
    
}
