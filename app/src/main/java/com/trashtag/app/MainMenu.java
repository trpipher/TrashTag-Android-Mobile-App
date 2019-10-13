package com.trashtag.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {


    Button btnMap;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main_menu);

            btnMap = findViewById(R.id.btnMap);
            btnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    startActivity(intent);
                }
            });

        btnMap = findViewById(R.id.profile_id);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), profile.class);
                startActivity(intent);
            }
        });
        btnMap = findViewById(R.id.settings_id);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), settings.class);
                startActivity(intent);
            }
        });
    }
}
