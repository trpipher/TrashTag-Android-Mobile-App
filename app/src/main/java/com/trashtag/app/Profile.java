package com.trashtag.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;

public class Profile extends AppCompatActivity {

    TextView nameText;
    TextView scoreText;
    TextView trashText;
    TextView recycleText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        nameText = findViewById(R.id.username);
        scoreText = findViewById(R.id.scoreText);
        recycleText = findViewById(R.id.recycleText);
        trashText = findViewById(R.id.trashText);

        nameText.setText(User.user.getUsername());
        scoreText.setText(""+User.user.getScore());
        recycleText.setText(""+User.user.getRecyclePins()+" " + getString(R.string.recyclePlaced));
        trashText.setText(User.user.getTrashPins()+" " + getString(R.string.trashPlaced));




        }
    }
