package com.joonasniemi.joonaksenpainikepeli;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etUsername;
    private EditText etPassword;
    private Button bAdd;

    private Map db = new HashMap();
    public static final String SALT = "very-salty-text";

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
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bAdd) {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();

            if(!username.isEmpty() && !password.isEmpty()){
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users");

                String id = myRef.push().getKey();
                User user = new User(username, password);

                myRef.child(id).setValue(user);

                Toast.makeText(this, "Käyttäjä lisätty", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Syötä käyttäjätunnus ja/tai salasana", Toast.LENGTH_LONG).show();
            }
        }
    }
}
