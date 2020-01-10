package com.joonasniemi.joonaksenpainikepeli;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "myLog";
    private ImageButton ibSettings;
    private TextView tvPoints;
    private Button bAddCounter;

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
        bAddCounter = findViewById(R.id.bAddCounter);

        //click listeners
        ibSettings.setOnClickListener(this);
        bAddCounter.setOnClickListener(this);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.d(TAG, "User logged in: " + mAuth.getCurrentUser().getEmail());

        //listen user points from server
        DocumentReference userDocRef = mDatabase.collection("users").document(mAuth.getCurrentUser().getUid());
        userDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.w(TAG, "Could not listen.", e);
                    return;
                }
                if(documentSnapshot != null && documentSnapshot.exists()){
                    Log.d(TAG, "User points: " + documentSnapshot.get("points"));
                    tvPoints.setText(getString(R.string.textPoints) + documentSnapshot.get("points"));
                } else {
                    Log.d(TAG, "No current data");
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
        } else if(v.getId() == R.id.bAddCounter){
            //decrease players points by 1
            mDatabase.collection("users").document(mAuth.getCurrentUser().getUid())
                    .update("points", FieldValue.increment(-1));

            //adds value to counter
            mDatabase.collection("game").document("counter")
                    .update("value", FieldValue.increment(1));
        }
    }

    private void updateUi() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
