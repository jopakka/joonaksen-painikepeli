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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "myLog";
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button bAdd;
    private TextView tvLogin;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //hides action bar
        try {
            getSupportActionBar().hide();
        } catch (Exception e){
            Log.d(TAG, "Actionbar error: " + e);
        }

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        //views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvLogin = findViewById(R.id.tvLogin);

        //buttons
        bAdd = findViewById(R.id.bRegister);

        //click listeners
        bAdd.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bRegister) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

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
            if(!password.equals(confirm)) {
                etConfirmPassword.setError("Password don't match");
                etConfirmPassword.requestFocus();
                return;
            }

            registerUser(email, password);

        } else if(v.getId() == R.id.tvLogin){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void registerUser(String email, String password) {
        progressDialog.setMessage("Rekisteröi käyttäjää...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user is registered successfully
                            progressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Rekisteröinti onnistui", Toast.LENGTH_SHORT).show();
                            updateUi();
                        } else {
                            progressDialog.hide();
                            Log.d(TAG, "Error when creating user: " + task.getException());
                            Toast.makeText(RegisterActivity.this, "Rekisteröinti epäonnistui. Ole hyvä ja yritä uudestaan.", Toast.LENGTH_SHORT).show();
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
