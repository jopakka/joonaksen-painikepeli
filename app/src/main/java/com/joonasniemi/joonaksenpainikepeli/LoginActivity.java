package com.joonasniemi.joonaksenpainikepeli;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "myLog";
    private TextView tvRegister;
    private EditText etEmail;
    private EditText etPassword;
    private Button bLogin;

    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);

        //firebase
        mAuth = FirebaseAuth.getInstance();

        //textfields
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvRegister = findViewById(R.id.tvRegister);
        bLogin = findViewById(R.id.bLogin);

        //click listeners
        bLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null){
            updateUi();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bLogin) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(email.isEmpty()){
                //if email field is empty
                etEmail.setError("Missing email");
                etEmail.requestFocus();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                //if email field is not valid
                etEmail.setError("Invalid email");
                etEmail.requestFocus();
                return;
            }
            if(password.isEmpty() || password.length() < 8){
                //if password is missing or is too short
                etPassword.setError("Password must be at least 8 characters long");
                etPassword.requestFocus();
                return;
            }

            loginUser(email, password);

        } else if(v.getId() == R.id.tvRegister){
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loginUser(String email, String password){
        progressDialog.setMessage(getString(R.string.progressLogin));
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            //user is successfully login
                            updateUi();
                        } else {
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthUserCollisionException e) {
                                etEmail.setError(getString(R.string.errorEmailInUse));
                                etEmail.requestFocus();
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                            Log.d(TAG, "Error when login: " + task.getException());
                            Toast.makeText(LoginActivity.this, "Kirjautuminen epäonnistui. Ole hyvä ja yritä uudestaan.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUi(){
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        finish();
    }
}
