package com.abhinav.play_video.java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.abhinav.play_video.kotlin.ArMainActivity;
import com.abhinav.play_video.R;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_CODE =0 ;
    private ImageView send;
    private ImageView receive;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send= findViewById(R.id.send);
        receive=findViewById(R.id.receive);
        send.setOnClickListener(view -> {
            Intent i=new Intent(getApplicationContext(), Upload_Photo.class);
            startActivity(i);
        });

        receive.setOnClickListener(view -> {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);

        });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handle();
            }
        }

    }

    private void handle() {

            final EditText view2 = new EditText(getApplicationContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Alert!");
            builder.setMessage("Enter 8 digit Room Code");
            builder.setView(view2);
            builder.setCancelable(true);
            builder.setPositiveButton("Yes", (dialog, which) -> {

                Editable editable=view2.getText();
                String name=editable.toString();
                if(name.length()==8) {
                    Intent i = new Intent(getApplicationContext(), Receive_Video.class);
                    i.putExtra("roomcode", name);
                    startActivity(i);
                }
                else{
                    Toast.makeText(this,"Enter 8 digit Code",Toast.LENGTH_SHORT).show();
                }
            });


            AlertDialog alertDialog = builder.create();
            alertDialog.show();  // Show the Alert Dialog box

    }


}