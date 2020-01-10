package com.joonasniemi.joonaksenpainikepeli;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "myLog";
    private ImageButton ibSettings;
    private TextView tvPoints;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        //textfields
        tvPoints = findViewById(R.id.tvPoints);

        //buttons
        ibSettings = findViewById(R.id.ibSettings);

        //click listeners
        ibSettings.setOnClickListener(this);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.d(TAG, "User logged in: " + mAuth.getCurrentUser().getEmail());

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference docRef = mDatabase.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        tvPoints.append(" " + user.getPoints());
                        Log.d(TAG, "DocumentSnapshot data: " + user.getPoints());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ibSettings){
            //TODO add settings
            mAuth.signOut();
            updateUi();
        }
    }

    private void updateUi() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
