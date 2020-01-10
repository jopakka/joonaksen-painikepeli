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

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "myLog";
    private EditText etEmail;
    private EditText etPassword;

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
        TextView tvRegister = findViewById(R.id.tvRegister);
        Button bLogin = findViewById(R.id.bLogin);

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
                                throw Objects.requireNonNull(task.getException());
                            } catch(FirebaseAuthUserCollisionException e) {
                                etEmail.setError(getString(R.string.errorEmailInUse));
                                etEmail.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e){
                                etEmail.setError(getString(R.string.errorInvalidCredentials));
                                etEmail.requestFocus();
                            }catch(Exception e) {
                                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                            }
                            Log.d(TAG, "Error when login: " + task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.textLoginFailed), Toast.LENGTH_SHORT).show();
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
