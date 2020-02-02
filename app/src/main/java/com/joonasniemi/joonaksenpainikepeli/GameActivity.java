package com.joonasniemi.joonaksenpainikepeli;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class GameActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private final String TAG = "myLog";
    private TextView tvPoints;
    private SharedPreferences sp;
    private TextView tvUsersOnline;
    private Resources res;

    private int userPoints;
    private int bigPrice = 250;
    private int mediumPrice = 40;
    private int smallPrice = 5;

    private FirebaseAuth mAuth;
    private DocumentReference counterDocRef;
    private DocumentReference userDocRef;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        counterDocRef = mDatabase.collection("game").document("counter");
        userDocRef = mDatabase.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        //textfields
        tvPoints = findViewById(R.id.tvPoints);
        tvUsersOnline = findViewById(R.id.tvUsersOnline);

        //buttons
        ImageButton ibSettings = findViewById(R.id.ibSettings);
        Button bAddCounter = findViewById(R.id.bAddCounter);

        //click listeners
        ibSettings.setOnClickListener(this);
        bAddCounter.setOnClickListener(this);

        res = getResources();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "User logged in: " + Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());

        sp = getSharedPreferences("userSavedPoints", Context.MODE_PRIVATE);
        Log.d(TAG, "Points for user " + mAuth.getCurrentUser().getEmail() + " in shaderpreferences: "
                + sp.getInt(mAuth.getCurrentUser().getUid(), 0));

        //set users points value from server if Intent is null
        if (getIntent() == null) {
            String textUsers = Integer.toString(sp.getInt(mAuth.getCurrentUser().getUid(), 0));
            tvPoints.setText(textUsers);
        } else {
            String textUsers = Integer.toString(getIntent().getIntExtra("points", 0));
            tvPoints.setText(textUsers);
        }

        //listens online users
        mDatabase.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "Error while reading users: " + e);
                }
                if (queryDocumentSnapshots != null) {
                    int usersOnline = 0;
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        if ((boolean) queryDocumentSnapshots.getDocuments().get(i).get("online")) {
                            usersOnline++;
                        }
                    }
                    Log.d(TAG, "Users online: " + usersOnline);
                    String textUsers = String.format(res.getString(R.string.textUsersOnline),
                            usersOnline);
                    tvUsersOnline.setText(textUsers);

                } else {
                    Log.d(TAG, "No users snapshot.");
                }
            }
        });

        //listens user points from server
        userDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "Could not listen.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "User points: " + documentSnapshot.get("points"));
                    String textPoints = (Objects.requireNonNull(documentSnapshot.get("points"))).toString();
                    tvPoints.setText(textPoints);
                    userPoints = Objects.requireNonNull(documentSnapshot.getLong("points")).intValue();
                } else {
                    Log.d(TAG, "No current data");
                }
            }
        });

        //listens counter value from server
        counterDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Could not listen.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "Counter value: " + documentSnapshot.get("value"));
                } else {
                    Log.d(TAG, "No current data");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserStatusOnline();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth.getCurrentUser() != null) {
            sp.edit().putInt(mAuth.getCurrentUser().getUid(), userPoints).apply();
            setUserStatusOffline();
        }
        Log.d(TAG, "User points in sharedpreferences (onPause): " + userPoints);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuth.getCurrentUser() != null) {
            sp.edit().putInt(mAuth.getCurrentUser().getUid(), userPoints).apply();
            setUserStatusOffline();
        }
        Log.d(TAG, "User points in sharedpreferences (onDestroy): " + userPoints);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibSettings:
                showSettingMenu(v);
                break;

            case R.id.bAddCounter:
                userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot ds = task.getResult();
                            if (Objects.requireNonNull(ds).exists()) {
                                if (ds.getLong("points") > 0) {
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
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bGoal:
                Intent goalIntent = new Intent(this, GoalActivity.class);
                startActivity(goalIntent);
                return true;

            case R.id.bInfo:
                Intent infoIntent = new Intent(this, InfoActivity.class);
                startActivity(infoIntent);
                return true;

            case R.id.bLogout:
                logout();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        exitAlert();
    }

    private void setUserStatusOnline() {
        userDocRef.update("online", true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User " + Objects.requireNonNull(mAuth.getCurrentUser()).getEmail() + " is online");
                }
            }
        });
    }

    private void setUserStatusOffline() {
        userDocRef.update("online", false).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User " + Objects.requireNonNull(mAuth.getCurrentUser()).getEmail() + " is offline");
                }
            }
        });
    }

    private void showSettingMenu(View v) {
        Context context = new ContextThemeWrapper(this, R.style.popup_style);
        PopupMenu popup = new PopupMenu(context, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.settings_popup);
        popup.show();
    }

    //checks if player has won
    private void checkPrice() {
        counterDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot ds = task.getResult();
                    if (Objects.requireNonNull(ds).exists()) {
                        userDocRef.update("points", FieldValue.increment(-1));
                        if (ds.getLong("value") % 500 == 0) {
                            userDocRef.update("points", FieldValue.increment(bigPrice));
                            givePricePopup(bigPrice);
                        } else if (ds.getLong("value") % 100 == 0) {
                            userDocRef.update("points", FieldValue.increment(mediumPrice));
                            givePricePopup(mediumPrice);
                        } else if (ds.getLong("value") % 10 == 0) {
                            userDocRef.update("points", FieldValue.increment(smallPrice));
                            givePricePopup(smallPrice);
                        } else {
                            //checks that player points are positive
                            userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot ds = task.getResult();
                                        if (Objects.requireNonNull(ds).exists()) {
                                            if (ds.getLong("points") > 0) {
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
        sp.edit().putInt(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), userPoints).apply();
        setUserStatusOffline();
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void gameOver() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.gameOverTitle))
                .setMessage(getString(R.string.gameOverDesc))
                .setPositiveButton(R.string.buttonYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userDocRef.update("points", 20);
                    }
                })
                .setNegativeButton(R.string.buttonNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                }).show();
    }

    private void clicksToNextPrice() {
        counterDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot ds = task.getResult();
                    if (Objects.requireNonNull(ds).exists()) {
                        nextPriceAlert(10 - Objects.requireNonNull(ds.getLong("value")).intValue() % 10);
                    } else {
                        Log.d(TAG, "No document");
                    }
                } else {
                    Log.d(TAG, "Error while reading value: " + task.getException());
                }
            }
        });
    }

    private void givePricePopup(int price) {
        final AlertDialog alert = new AlertDialog.Builder(this)
                .setView(R.layout.price_popup_layout)
                .create();

        alert.show();

        TextView tv = alert.findViewById(R.id.tvPriceTitle);
        ImageView iv = alert.findViewById(R.id.ivPriceImage);
        if (tv != null) {
            if (price == bigPrice) {
                tv.setText(res.getString(R.string.textBigPrice, bigPrice));
                Objects.requireNonNull(iv).setImageResource(R.drawable.treasure_chest);
            } else if (price == mediumPrice) {
                tv.setText(res.getString(R.string.textMediumPrice, mediumPrice));
                Objects.requireNonNull(iv).setImageResource(R.drawable.coin_purse);
            } else {
                tv.setText(res.getString(R.string.textSmallPrice, smallPrice));
                Objects.requireNonNull(iv).setImageResource(R.drawable.coin);
            }
        }

        Button button = alert.findViewById(R.id.bPriceOk);
        Objects.requireNonNull(button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
    }

    private void nextPriceAlert(int clicks) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(res.getQuantityString(R.plurals.pluralNextPrice, clicks, clicks))
                .setPositiveButton(R.string.buttonYarr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void exitAlert() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.textExit))
                .setPositiveButton(R.string.buttonYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GameActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.buttonNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
