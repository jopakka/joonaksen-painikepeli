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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "myLog";
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

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
        etEmail = findViewById(R.id.etEmail);
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
        if(v.getId() == R.id.bRegister) {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if(email.isEmpty()){
                //if email field is empty
                etEmail.setError(getString(R.string.errorEmailEmpty));
                etEmail.requestFocus();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                //if email field is not valid
                etEmail.setError(getString(R.string.errorInvalidEmail));
                etEmail.requestFocus();
                return;
            }
            if(password.isEmpty() || password.length() < 6){
                //if password is missing or is too short
                etPassword.setError(getString(R.string.errorPasswordTooShort));
                etPassword.requestFocus();
                return;
            }
            if(!password.equals(confirm)) {
                etConfirmPassword.setError(getString(R.string.errorPasswordNoMatch));
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
        progressDialog.setMessage(getString(R.string.progressRegistering));
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            //user is registered successfully
                            //gives user 20 starting points
                            String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            Map<String, Object> user = new HashMap<>();
                            user.put("points", 20);
                            mDatabase.collection("users").document(userId).set(user);
                            updateUi();
                        } else {
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch(FirebaseAuthUserCollisionException e) {
                                etEmail.setError(getString(R.string.errorEmailInUse));
                                etEmail.requestFocus();
                            } catch(Exception e) {
                                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                                Toast.makeText(RegisterActivity.this, getString(R.string.errorRegisteringFailed), Toast.LENGTH_SHORT).show();
                            }
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
