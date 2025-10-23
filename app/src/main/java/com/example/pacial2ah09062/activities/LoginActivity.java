package com.example.pacial2ah09062.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.repository.UserRepository;
import com.example.pacial2ah09062.utils.PreferenceManager;
import com.example.pacial2ah09062.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private EditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private PreferenceManager preferencesManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();

        preferencesManager = new PreferenceManager(this);
        userRepository = UserRepository.getInstance(this);

        loadSavedCredentials();
        setupListeners();
    }

    private void initializeViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadSavedCredentials() {
        if (preferencesManager.shouldRememberUser()) {
            etEmail.setText(preferencesManager.getSavedEmail());
            etPassword.setText(preferencesManager.getSavedPassword());
            cbRememberMe.setChecked(true);
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Limpiar errores al escribir
        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
            }
        });
    }

    private void attemptLogin() {
        // Limpiar errores previos
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validaciones
        boolean isValid = true;

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(ValidationUtils.getEmailErrorMessage());
            isValid = false;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError(ValidationUtils.getPasswordErrorMessage());
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Mostrar progreso
        showLoading(true);

        // Validar credenciales
        userRepository.authenticateUser(email, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(com.example.pacial2ah09062.database.entity.User user) {
                runOnUiThread(() -> {
                    showLoading(false);

                    // Guardar credenciales si se solicitó
                    preferencesManager.saveUserCredentials(email, password, cbRememberMe.isChecked());
                    preferencesManager.setUserLoggedIn(email);

                    Toast.makeText(LoginActivity.this, "Bienvenido " + user.getFullName(), Toast.LENGTH_SHORT).show();

                    // Ir a Home
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
        cbRememberMe.setEnabled(!show);
    }

    // Clase auxiliar para TextWatcher simplificado
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(android.text.Editable s) {}
    }
}