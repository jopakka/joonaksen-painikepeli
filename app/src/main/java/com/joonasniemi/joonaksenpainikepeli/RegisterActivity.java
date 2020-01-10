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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "myLog";
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button bRegister;
    private TextView tvLogin;

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
        tvLogin = findViewById(R.id.tvLogin);

        //buttons
        bRegister = findViewById(R.id.bRegister);

        //click listeners
        bRegister.setOnClickListener(this);
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
            if(password.isEmpty()){
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
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = mAuth.getCurrentUser().getUid();

                            //give user 20 starting points
                            User user = new User(20);

                            // Add a new document with a user ID
                            mDatabase.collection("users")
                                    .document(userId)
                                    .set(user);
                            updateUi();
                        } else {
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthUserCollisionException e) {
                                etEmail.setError(getString(R.string.errorEmailInUse));
                                etEmail.requestFocus();
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
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
