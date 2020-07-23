package com.abhinav.play_video;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    private Button send;
    private Button receive;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send=(Button)findViewById(R.id.send);
        receive=findViewById(R.id.receive);

        send.setOnClickListener(view -> {
            Intent i=new Intent(getApplicationContext(),Upload_Photo.class);
            startActivity(i);
        });

        receive.setOnClickListener(view -> {
           final EditText view2 = new EditText(getApplicationContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Alert!");
            builder.setMessage("Enter 8 digit Room Code");
            builder.setView(view2);
            builder.setCancelable(true);
            builder.setPositiveButton("Yes", (dialog, which) -> {

                Editable editable=view2.getText();
                String name=editable.toString();
                Intent i=new Intent(getApplicationContext(),Ar_MainActivity.class);
                //i.putExtra("roomcode",name);
                startActivity(i);
            });


            AlertDialog alertDialog = builder.create();
            alertDialog.show();  // Show the Alert Dialog box
        });
    }


}