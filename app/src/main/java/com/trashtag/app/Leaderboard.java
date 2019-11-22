package com.trashtag.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.trashtag.app.AppResources.databaseReference;

public class Leaderboard extends AppCompatActivity {

    ArrayList<Score> Top10 = new ArrayList<Score>();
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        for (int i = 0; i < 10; i++){
            Top10.add(new Score(0,"user"));
        }
        load();




    }

    public void load(){
        DatabaseReference loadRef = databaseReference("Users");
        loadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user: dataSnapshot.getChildren())
                {
                    int score = user.child("score").getValue(int.class);
                    int low = lowestScore(Top10);
                    Log.wtf("Scores", "Score: "+ score + " Low: " + low);
                    if(score >= Top10.get(low).score)
                    {
                        String username = user.child("username").getValue(String.class);
                        Top10.set(low, new Score(score, username));
                    }


                }
                Top10.sort(new Comparator<Score>() {
                    @Override
                    public int compare(Score score, Score t1) {
                        return t1.score - score.score;
                    }
                });
                //for(Score s : Top10) Log.wtf("Score: ", "Score: " + s.score + " user: " + s.user);

                LinearLayout scores = findViewById(R.id.scoreLayout);
                LinearLayout users = findViewById(R.id.userLayout);
                for(int i = 0; i < 10; i++){
                    ((TextView)scores.getChildAt(i+1)).setText(Top10.get(i).score+"");
                    ((TextView)users.getChildAt(i+1)).setText(Top10.get(i).user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    public int lowestScore(ArrayList<Score> list){
        int min = Integer.MAX_VALUE;
        int mini = -1;
        for(int i = 0; i < list.size(); i++)
        {
            if(list.get(i).score < min) {
                min = list.get(i).score;
                mini = i;
            }
        }

        return mini;
    }

}

class Score{
    public int score;
    public String user;

    public Score(int a, String u){
        score = a;
        user = u;
    }
}
