package com.trashtag.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AppStartActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_start);

        firebaseInitiation();
        Login();


    }

    private void firebaseInitiation () {
        mAuth = AppResources.mAuth();
        databaseReference = AppResources.databaseReference();
    }

    private void Login () {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    public void onActivityResult ( int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("FAILED", "Google Sign In failed", e);
                Toast.makeText(getApplicationContext(), "Sign In Failed...", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void firebaseAuthWithGoogle (GoogleSignInAccount acct)
    {
        Log.d("ACCOUNT INFO", "firebaseAuthWithGoogle" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Google Authorization Successful", Toast.LENGTH_SHORT).show();
                            Log.d("SUCCESS", "SingInWithCredential");
                            loadUser();
                            Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    private void loadUser(){
        databaseReference.child("Users/"+mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FireUser temp = dataSnapshot.getValue(FireUser.class);
                if(temp != null){
                    Log.wtf("NOT","NULL");
                    User.user = new User(temp);
                }
                else {
                    Log.wtf("DEFF","NULL");
                    User.user = new User(spliceEmail());
                    databaseReference.child("Users/"+mAuth.getCurrentUser().getUid()).setValue(new FireUser(User.user));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String spliceEmail(){
        String username = mAuth.getCurrentUser().getEmail();
        return username.substring(0, username.indexOf('@'));
    }


}
