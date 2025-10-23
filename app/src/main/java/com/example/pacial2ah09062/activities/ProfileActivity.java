package com.example.pacial2ah09062.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.entity.User;
import com.example.pacial2ah09062.repository.UserRepository;
import com.example.pacial2ah09062.utils.PreferenceManager;
import com.example.pacial2ah09062.utils.ValidationUtils;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvEmail, tvSyncWarning;
    private TextInputLayout tilFullName;
    private EditText etFullName;
    private Button btnUpdate, btnRetrySync;
    private ProgressBar progressBar;

    private PreferenceManager preferencesManager;
    private UserRepository userRepository;
    private String currentUserEmail;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }

        initializeViews();

        preferencesManager = new PreferenceManager(this);
        userRepository = UserRepository.getInstance(this);
        currentUserEmail = preferencesManager.getCurrentUserEmail();

        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
        tvEmail = findViewById(R.id.tvEmail);
        tvSyncWarning = findViewById(R.id.tvSyncWarning);
        tilFullName = findViewById(R.id.tilFullName);
        etFullName = findViewById(R.id.etFullName);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnRetrySync = findViewById(R.id.btnRetrySync);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadUserData() {
        showLoading(true);

        userRepository.getUserByEmail(currentUserEmail, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    showLoading(false);
                    currentUser = user;
                    tvEmail.setText(user.getEmail());
                    etFullName.setText(user.getFullName());

                    // Mostrar advertencia si hay sincronización pendiente
                    if (user.isPendingSync()) {
                        tvSyncWarning.setVisibility(View.VISIBLE);
                        btnRetrySync.setVisibility(View.VISIBLE);
                    } else {
                        tvSyncWarning.setVisibility(View.GONE);
                        btnRetrySync.setVisibility(View.GONE);
                    }
                });
            }
            
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void setupListeners() {
        btnUpdate.setOnClickListener(v -> updateProfile());

        btnRetrySync.setOnClickListener(v -> retrySync());

        etFullName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilFullName.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void updateProfile() {
        tilFullName.setError(null);

        String newName = etFullName.getText().toString().trim();

        // Validación
        if (!ValidationUtils.isValidFullName(newName)) {
            tilFullName.setError(ValidationUtils.getFullNameErrorMessage());
            return;
        }

        // Verificar si realmente cambió
        if (currentUser != null && newName.equals(currentUser.getFullName())) {
            Toast.makeText(this, "No hay cambios para actualizar", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        userRepository.updateUserProfile(currentUserEmail, newName, new UserRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    loadUserData(); // Recargar datos
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                    loadUserData(); // Recargar para ver el estado
                });
            }
        });
    }

    private void retrySync() {
        showLoading(true);

        userRepository.syncPendingUsers(new UserRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Sincronización completada exitosamente", Toast.LENGTH_SHORT).show();
                    loadUserData(); // Recargar datos
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!show);
        btnRetrySync.setEnabled(!show);
        etFullName.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}