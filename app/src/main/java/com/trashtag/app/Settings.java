package com.trashtag.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class Settings extends AppCompatActivity {

    Button btnBack;
    Button btnSignOut;
    GoogleSignInClient mGoogleSignInClient;
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            btnBack = findViewById(R.id.home);
            btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                startActivity(intent);
            }
        });

        btnSignOut = findViewById(R.id.signOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);


                if (AppResources.mAuth().getCurrentUser() == null) {
                    Toast.makeText(getApplicationContext(), "Not Logged In...", Toast.LENGTH_SHORT).show();
                } else {
                    // Firebase sign out
                    AppResources.mAuth().signOut();
                    try {
                        // Google sign out
                        mGoogleSignInClient.signOut();
                    } catch (Exception e) {
                        Log.e("ERROR SIGN OUT", "message", e);
                    }
                    Toast.makeText(getApplicationContext(), "Signed Out...", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }
}
