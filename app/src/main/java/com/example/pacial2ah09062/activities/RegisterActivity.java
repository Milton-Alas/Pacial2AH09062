package com.example.pacial2ah09062.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.entity.User;
import com.example.pacial2ah09062.repository.UserRepository;
import com.example.pacial2ah09062.utils.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilFullName, tilPassword;
    private EditText etEmail, etFullName, etPassword;
    private Button btnRegister;
    private ProgressBar progressBar;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registro de Usuario");
        }

        initializeViews();
        userRepository = UserRepository.getInstance(this);
        setupListeners();
    }

    private void initializeViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilFullName = findViewById(R.id.tilFullName);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etFullName = findViewById(R.id.etFullName);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        // Limpiar errores al escribir
        etEmail.addTextChangedListener(createTextWatcher(tilEmail));
        etFullName.addTextChangedListener(createTextWatcher(tilFullName));
        etPassword.addTextChangedListener(createTextWatcher(tilPassword));
    }

    private TextWatcher createTextWatcher(TextInputLayout textInputLayout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    private void attemptRegister() {
        // Limpiar errores previos
        tilEmail.setError(null);
        tilFullName.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validaciones
        boolean isValid = true;

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(ValidationUtils.getEmailErrorMessage());
            isValid = false;
        }

        if (!ValidationUtils.isValidFullName(fullName)) {
            tilFullName.setError(ValidationUtils.getFullNameErrorMessage());
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

        // Crear usuario
        User newUser = new User(email, fullName, password);

        // Registrar usuario
        userRepository.registerUser(newUser, new UserRepository.RepositoryCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_LONG).show();
                    finish(); // Volver al login
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        etEmail.setEnabled(!show);
        etFullName.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}