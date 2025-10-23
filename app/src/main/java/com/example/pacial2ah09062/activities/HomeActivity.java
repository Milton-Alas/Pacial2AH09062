package com.example.pacial2ah09062.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.entity.User;
import com.example.pacial2ah09062.repository.UserRepository;
import com.example.pacial2ah09062.utils.PreferenceManager;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvSyncStatus, tvUserEmail, tvUserName;
    private MaterialCardView cardProfile, cardMap, cardSync;

    private PreferenceManager preferencesManager;
    private UserRepository userRepository;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Inicio");
        }

        initializeViews();

        preferencesManager = new PreferenceManager(this);
        userRepository = UserRepository.getInstance(this);
        currentUserEmail = preferencesManager.getCurrentUserEmail();

        loadUserData();
        checkSyncStatus();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando volvemos de perfil
        loadUserData();
        checkSyncStatus();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSyncStatus = findViewById(R.id.tvSyncStatus);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserName = findViewById(R.id.tvUserName);
        cardProfile = findViewById(R.id.cardProfile);
        cardMap = findViewById(R.id.cardMap);
        cardSync = findViewById(R.id.cardSync);
    }

    private void loadUserData() {
        userRepository.getUserByEmail(currentUserEmail, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    tvWelcome.setText("¡Bienvenido/a!");
                    tvUserEmail.setText(user.getEmail());
                    tvUserName.setText(user.getFullName());
                });
            }
            
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    tvWelcome.setText("Usuario no encontrado");
                });
            }
        });
    }

    private void checkSyncStatus() {
        // Por ahora mostrar estado estático, se implementará método en UserRepository después
        tvSyncStatus.setText("✓ Datos sincronizados");
        tvSyncStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
    }

    private void setupListeners() {
        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        cardMap.setOnClickListener(v -> {
            // TODO: Implementar MapActivity
            Toast.makeText(this, "Mapa próximamente", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(HomeActivity.this, MapActivity.class);
            // startActivity(intent);
        });

        cardSync.setOnClickListener(v -> {
            syncData();
        });
    }

    private void syncData() {
        Toast.makeText(this, "Sincronizando datos...", Toast.LENGTH_SHORT).show();

        userRepository.syncPendingUsers(new UserRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, "Sincronización completada", Toast.LENGTH_SHORT).show();
                    checkSyncStatus();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        preferencesManager.logout();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
