package com.example.pacial2ah09062.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.database.entity.User;
import com.example.pacial2ah09062.repository.UserRepository;
import com.example.pacial2ah09062.utils.PreferenceManager;
import com.example.pacial2ah09062.utils.ValidationUtils;
import com.example.pacial2ah09062.firebase.FirebaseManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout tilEmail, tilPassword;
    private EditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private PreferenceManager preferencesManager;
    private UserRepository userRepository;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();

        preferencesManager = new PreferenceManager(this);
        userRepository = UserRepository.getInstance(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize ActivityResultLauncher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            // Google Sign In failed, update UI appropriately
                            Log.w(TAG, "Google sign in failed", e);
                            Toast.makeText(LoginActivity.this, "Error en el inicio de sesión con Google.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
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
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());

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

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Check if user exists in local DB, if not, create it
                            userRepository.getUserByEmail(firebaseUser.getEmail(), new UserRepository.UserCallback() {
                                @Override
                                public void onSuccess(User user) {
                                    if (user == null) {
                                        // User does not exist, create a new entry
                                        createNewUserFromFirebase(firebaseUser);
                                    } else {
                                        // User exists, proceed to login
                                        handleSuccessfulLogin(user);
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    // Assume user doesn't exist locally and create them
                                    Log.w(TAG, "getUserByEmail failed, creating new user anyway: " + error);
                                    createNewUserFromFirebase(firebaseUser);
                                }
                            });
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Error de autenticación con Firebase.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNewUserFromFirebase(FirebaseUser firebaseUser) {
        User newUser = new User(
                firebaseUser.getEmail(), // Use email as primary key
                firebaseUser.getDisplayName(),
                "" // No password for Google Sign-In
        );
        userRepository.createUser(newUser, true, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User createdUser) {
                handleSuccessfulLogin(createdUser);
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Error al guardar el usuario localmente: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleSuccessfulLogin(User user) {
        // Actualizar token FCM para este usuario (importante cuando el token ya existía antes de iniciar sesión)
        updateFcmTokenForUser(user.getEmail());

        runOnUiThread(() -> {
            showLoading(false);
            preferencesManager.setUserLoggedIn(user.getEmail());
            Toast.makeText(LoginActivity.this, "Bienvenido " + user.getFullName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, ProductListActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateFcmTokenForUser(String email) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "No se pudo obtener el token FCM", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    if (token == null || token.isEmpty()) {
                        Log.w(TAG, "Token FCM vacío");
                        return;
                    }

                    FirebaseManager firebaseManager = new FirebaseManager();
                    firebaseManager.updateUserFcmToken(email, token, null);
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

        // Validación adicional: verificar fortaleza de contraseña
        String passwordStrengthError = ValidationUtils.getPasswordStrengthMessage(password);
        if (passwordStrengthError != null) {
            tilPassword.setError(passwordStrengthError);
            showLoading(false);
            return;
        }
        
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

                    // Ir a ProductListActivity
                    Intent intent = new Intent(LoginActivity.this, ProductListActivity.class);
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
        btnGoogleLogin.setEnabled(!show);
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