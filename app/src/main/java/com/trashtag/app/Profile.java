package com.trashtag.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.squareup.picasso.Picasso;

import static com.trashtag.app.AppResources.mAuth;

public class Profile extends AppCompatActivity {

    TextView nameText;
    TextView scoreText;
    TextView trashText;
    TextView recycleText;
    Button btnSignOut;
    GoogleSignInClient mGoogleSignInClient;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        nameText = findViewById(R.id.username);
        scoreText = findViewById(R.id.scoreText);
        recycleText = findViewById(R.id.recycleText);
        trashText = findViewById(R.id.trashText);
        img = findViewById(R.id.user_avatar);

        nameText.setText(User.user.getUsername());
        scoreText.setText(""+User.user.getScore());
        recycleText.setText(""+User.user.getRecyclePins()+" " + getString(R.string.recyclePlaced));
        trashText.setText(User.user.getTrashPins()+" " + getString(R.string.trashPlaced));
        Picasso.get().load(mAuth().getCurrentUser().getPhotoUrl()).into(img);

        btnSignOut = findViewById(R.id.signOut2);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);


                if (mAuth().getCurrentUser() == null) {
                    Toast.makeText(getApplicationContext(), "Not Logged In...", Toast.LENGTH_SHORT).show();
                } else {
                    // Firebase sign out
                    mAuth().signOut();
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