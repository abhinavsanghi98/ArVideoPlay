package com.abhinav.play_video.java;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.abhinav.play_video.R;
import com.abhinav.play_video.helpers.BitmapHelper;
import com.abhinav.play_video.kotlin.ArMainActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Receive_Video extends AppCompatActivity {
    private String roomCode;
    private ImageView imageView;
    private Button button1;
    private Button button2;
    String videoURL;
    String imageURL;
    ProgressDialog mDialog;

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_code);
        Intent i=getIntent();
        roomCode=i.getExtras().getString("roomcode");
        imageView=findViewById(R.id.iv);
        button1=findViewById(R.id.load_video);
        button2=findViewById(R.id.bt_ar);
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS).child(roomCode);
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("Please Wait");
        mDialog.setMessage("Data is loading...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //video.setText(snapshot.child("video").child("url").getValue(String.class));
                videoURL=snapshot.child("video").child("url").getValue(String.class);
                imageURL=snapshot.child("image").child("url").getValue(String.class);
                Glide.with(Receive_Video.this).asBitmap().load(imageURL).diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(resource);
                        button1.setVisibility(View.VISIBLE);
                        //bitmap=resource;
                        BitmapHelper.getInstance().setBitmap(resource);
                        mDialog.dismiss();

                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }



                });

               mDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        button1.setOnClickListener(view -> {
           new Thread(new Myrunnable()).start();
            button1.setVisibility(View.GONE);

            button2.setVisibility(View.VISIBLE);

        });

        button2.setOnClickListener(view -> {
//            String filename = "myfile.png";
//            FileOutputStream stream = null;
//            try {
//                stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            //ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//            try {
//                stream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            if(BitmapHelper.getInstance().getBitmap()!= null) {
                Intent it = new Intent(getApplicationContext(), ArMainActivity.class);
                startActivity(it);
            }
            else{
                Toast.makeText(this,"Please wait",Toast.LENGTH_SHORT).show();
            }

        });

    }


    class Myrunnable implements Runnable {

        @Override
        public void run() {
            File dir = new File(Environment.getExternalStorageDirectory().toString(), "ArData");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File videoFile = new File(dir.getPath() + File.separator + "op" + ".mp4");
            if (videoFile.exists())
                videoFile.delete();

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoURL))
                    .setDestinationUri(Uri.fromFile(videoFile));

            downloadManager.enqueue(request);


        }
    }

}
