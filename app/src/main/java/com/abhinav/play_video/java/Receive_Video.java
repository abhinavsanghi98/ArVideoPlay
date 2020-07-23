package com.abhinav.play_video.java;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.abhinav.play_video.R;
import com.abhinav.play_video.java.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Receive_Video extends AppCompatActivity {
    private String roomCode;
    private TextView image;
    private TextView video;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_code);
        Intent i=getIntent();
        roomCode=i.getExtras().getString("roomcode");
        image=findViewById(R.id.image_link);
        video=findViewById(R.id.video_link);
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS).child(roomCode);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                image.setText(snapshot.child("image").child("url").getValue(String.class));
                video.setText(snapshot.child("video").child("url").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}
