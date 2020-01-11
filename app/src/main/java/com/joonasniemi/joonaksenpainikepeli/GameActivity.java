package com.joonasniemi.joonaksenpainikepeli;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "myLog";
    private TextView tvPoints;
    private LinearLayout settingMenu;
    private ImageButton ibSettings;
    private Button bGoal;
    private Button bInfo;
    private Button bLogout;

    private FirebaseAuth mAuth;
    private DocumentReference counterDocRef;
    private DocumentReference userDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        counterDocRef = mDatabase.collection("game").document("counter");
        userDocRef = mDatabase.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        //textfields
        tvPoints = findViewById(R.id.tvPoints);

        //layouts
        settingMenu = findViewById(R.id.layoutSettings);

        //buttons
        ibSettings = findViewById(R.id.ibSettings);
        Button bAddCounter = findViewById(R.id.bAddCounter);
        bGoal = findViewById(R.id.bGoal);
        bInfo = findViewById(R.id.bInfo);
        bLogout = findViewById(R.id.bLogout);

        //click listeners
        ibSettings.setOnClickListener(this);
        bAddCounter.setOnClickListener(this);
        bGoal.setOnClickListener(this);
        bInfo.setOnClickListener(this);
        bLogout.setOnClickListener(this);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.d(TAG, "User logged in: " + Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());

        //listen user points from server
        userDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.w(TAG, "Could not listen.", e);
                    return;
                }
                if(documentSnapshot != null && documentSnapshot.exists()){
                    Log.d(TAG, "User points: " + documentSnapshot.get("points"));
                    tvPoints.setText("" + documentSnapshot.get("points"));
                } else {
                    Log.d(TAG, "No current data");
                }
            }
        });

        //listen counter value from server
        counterDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.w(TAG, "Could not listen.", e);
                    return;
                }
                if(documentSnapshot != null && documentSnapshot.exists()){
                    Log.d(TAG, "Counter value: " + documentSnapshot.get("value"));
                } else {
                    Log.d(TAG, "No current data");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibSettings:
                if(settingMenu.getVisibility() == View.GONE){
                    settingMenu.setVisibility(View.VISIBLE);
                    ibSettings.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_settingsbutton_bg));

                } else{
                    settingMenu.setVisibility(View.GONE);
                    ibSettings.setBackground(null);
                }
                    break;

            case R.id.bAddCounter:
                userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot ds = task.getResult();
                            if(Objects.requireNonNull(ds).exists()){
                                if((Long) ds.get("points") > 0) {
                                    counterDocRef.update("value", FieldValue.increment(1));
                                    checkPrice();
                                } else {
                                    gameOver();
                                }
                            } else {
                                Log.d(TAG, "No document");
                            }
                        } else {
                            Log.d(TAG, "Error while reading value: " + task.getException());
                        }
                    }
                });
                break;

            case R.id.bGoal:
                Intent goalIntent = new Intent(this, GoalActivity.class);
                startActivity(goalIntent);
                break;

            case R.id.bInfo:
                Intent infoIntent = new Intent(this, InfoActivity.class);
                startActivity(infoIntent);
                break;

            case R.id.bLogout:
                logout();
                break;
        }
    }

    private void checkPrice(){
        //checks if player has won
        counterDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot ds = task.getResult();
                    if(Objects.requireNonNull(ds).exists()){
                        userDocRef.update("points", FieldValue.increment(-1));
                        if((Long) ds.get("value") % 500 == 0) {
                            userDocRef.update("points", FieldValue.increment(250));
                            price(250);
                        } else if((Long) ds.get("value") % 100 == 0) {
                            userDocRef.update("points", FieldValue.increment(40));
                            price(40);
                        } else if((Long) ds.get("value") % 10 == 0) {
                            userDocRef.update("points", FieldValue.increment(5));
                            price(5);
                        } else {
                            //checks that player points are positive
                            userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot ds = task.getResult();
                                        if(Objects.requireNonNull(ds).exists()){
                                            if((Long) ds.get("points") > 0) {
                                                clicksToNextPrice();
                                            } else {
                                                gameOver();
                                            }
                                        } else {
                                            Log.d(TAG, "No document");
                                        }
                                    } else {
                                        Log.d(TAG, "Error while reading value: " + task.getException());
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "No document");
                    }
                } else {
                    Log.d(TAG, "Error while reading value: " + task.getException());
                }
            }
        });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void gameOver(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.gameOverTitle))
                .setMessage(getString(R.string.gameOverDesc))
                .setPositiveButton(R.string.textYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userDocRef.update("points", 20);
                    }
                })
                .setNegativeButton(R.string.textNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                }).show();
    }

    private void clicksToNextPrice(){
        counterDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot ds = task.getResult();
                    if(Objects.requireNonNull(ds).exists()){
                        nextPriceAlert(10 - (Long) ds.get("value") % 10);
                    } else {
                        Log.d(TAG, "No document");
                    }
                } else {
                    Log.d(TAG, "Error while reading value: " + task.getException());
                }
            }
        });
    }

    private void price(long price){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.textCongratulationTitle))
                .setMessage(getString(R.string.textCongratulationDesc1) + " " + price + " " + getString(R.string.textCongratulationDesc2))
                .setPositiveButton(R.string.textYea, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void nextPriceAlert(Long clicks){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.textStepsToNextPriceTitle))
                .setMessage(getString(R.string.textNextPriceDesc1) + " " + clicks + " " + getString(R.string.textNextPriceDesc2))
                .setPositiveButton(R.string.textOk, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}
