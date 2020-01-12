package com.joonasniemi.joonaksenpainikepeli;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "myLog";
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private int currentPoints;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        //textfields
        etEmail = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        TextView tvLogin = findViewById(R.id.tvLogin);

        //buttons
        Button bRegister = findViewById(R.id.bRegister);

        //click listeners
        bRegister.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bRegister) {
            registerUser();

        } else if (v.getId() == R.id.tvLogin) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void registerUser() {
        String username = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty()) {
            //if email field is empty
            etEmail.setError(getString(R.string.errorUsernameEmpty));
            etEmail.requestFocus();
            return;
        }
        username += "@joonaksenpainikepeli.net";
        if (password.isEmpty() || password.length() < 6) {
            //if password is missing or is too short
            etPassword.setError(getString(R.string.errorPasswordTooShort));
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError(getString(R.string.errorPasswordNoMatch));
            etConfirmPassword.requestFocus();
            return;
        }

        progressDialog.setMessage(getString(R.string.progressRegistering));
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user is registered successfully
                            //gives user 20 starting points
                            String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            Map<String, Object> user = new HashMap<>();
                            user.put("points", 20);
                            user.put("online", true);
                            mDatabase.collection("users").document(userId).set(user);
                            updateUi();
                        } else {
                            try {
                                progressDialog.hide();
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthUserCollisionException e) {
                                etEmail.setError(getString(R.string.errorUsernameInUse));
                                etEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                                Toast.makeText(RegisterActivity.this, getString(R.string.errorRegisteringFailed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void updateUi() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(mAuth.getCurrentUser().getUid(), getCurrentPoints());
        startActivity(intent);
        finish();
    }

    private int getCurrentPoints() {
        FirebaseFirestore.getInstance().collection("users")
                .document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot ds = task.getResult();
                    if (Objects.requireNonNull(ds).exists()) {
                        currentPoints = ((Long) ds.get("points")).intValue();
                    } else {
                        Log.d(TAG, "No document");
                    }
                } else {
                    Log.d(TAG, "Error while reading value: " + task.getException());
                }
            }
        });
        return currentPoints;
    }
}
