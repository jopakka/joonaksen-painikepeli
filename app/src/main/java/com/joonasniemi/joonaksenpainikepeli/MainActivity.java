package com.joonasniemi.joonaksenpainikepeli;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etUsername;
    private EditText etPassword;
    private Button bAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        bAdd = findViewById(R.id.bAdd);

        bAdd.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bAdd) {
            Toast.makeText(this, "Button toimii", Toast.LENGTH_LONG).show();
        }
    }
}
