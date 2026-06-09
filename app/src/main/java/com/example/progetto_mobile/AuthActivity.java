package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.progetto_mobile.databinding.ActivityAuthBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private boolean isLoginMode = true;

    // Launcher per il risultato del login Google
    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        try {
                            GoogleSignInAccount account = GoogleSignIn
                                    .getSignedInAccountFromIntent(result.getData())
                                    .getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(this, "Login Google fallito", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        // se c'è già una sessione attiva vai direttamente alla MainActivity
        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        setupGoogleSignIn();
        setupButtons();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
    }

    private void setupButtons() {
        binding.btnMainAction.setOnClickListener(v -> {
            if (isLoginMode) login();
            else register();
        });

        // Pulsante Google
        binding.btnGoogle.setOnClickListener(v ->
                googleLauncher.launch(googleSignInClient.getSignInIntent())
        );

        // Switch tra login e registrazione
        binding.tvSwitchAction.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;

        if (isLoginMode) {
            binding.tvSubtitle.setText("Accedi al tuo account");
            binding.btnMainAction.setText("Accedi");
            binding.tilName.setVisibility(View.GONE);
            binding.tvSwitchLabel.setText("Non hai un account? ");
            binding.tvSwitchAction.setText("Registrati");
        } else {
            binding.tvSubtitle.setText("Crea il tuo account");
            binding.btnMainAction.setText("Registrati");
            binding.tilName.setVisibility(View.VISIBLE);
            binding.tvSwitchLabel.setText("Hai già un account? ");
            binding.tvSwitchAction.setText("Accedi");
        }
    }

    private void login() {
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!validateEmailPassword(email, password)) return;

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> goToMain())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Errore: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void register() {
        String name     = binding.etName.getText().toString().trim();
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            binding.tilName.setError("Inserisci il tuo nome");
            return;
        }
        if (!validateEmailPassword(email, password)) return;

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    // Salva il nome sul profilo Firebase
                    FirebaseUser user = auth.getCurrentUser();
                    UserProfileChangeRequest profileUpdate =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                    user.updateProfile(profileUpdate)
                            .addOnCompleteListener(task -> goToMain());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Errore: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnSuccessListener(result -> goToMain()).addOnFailureListener(e ->
                        Toast.makeText(this, "Errore: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean validateEmailPassword(String email, String password) {
        boolean valid = true;
        if (email.isEmpty()) {
            binding.tilEmail.setError("Inserisci la tua email");
            valid = false;
        } else {
            binding.tilEmail.setError(null);
        }
        if (password.isEmpty() || password.length() < 6) {
            binding.tilPassword.setError("La password deve avere almeno 6 caratteri");
            valid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        return valid;
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // rimuove AuthActivity dallo stack cosi col tasto back non torno all'authactivity
    }
}