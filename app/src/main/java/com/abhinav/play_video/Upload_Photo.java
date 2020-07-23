package com.abhinav.play_video;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Upload_Photo extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 101;
    String roomText;
    private EditText roomcode;
    private ImageView imageView;
    private Button image_chooser;
    private Button proceed;
    private Uri imageFilePath;
    private StorageReference storageReference;
    private DatabaseReference mDatabase;
    private StorageTask mUploadTask;
    //private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);
        roomcode = findViewById(R.id.roomcode);
        imageView = findViewById(R.id.imageview);
        image_chooser = findViewById(R.id.button_photo);
        proceed = findViewById(R.id.proceed);
        storageReference = FirebaseStorage.getInstance().getReference(Constants.STORAGE_PATH_UPLOADS);
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);

        image_chooser.setOnClickListener(view -> showImageChooser());

        proceed.setOnClickListener(view -> uploadFile1());


    }

    //    @Override
//    public void onClick(View view) {
//
//        if(view==image_chooser)
//            showImageChooser();
//
//        if(view==proceed)
//            uploadFile();
//    }
    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageFilePath = data.getData();
//            Handler handler=new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),imageFilePath);
//                        imageView.setImageBitmap(bitmap);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            },500);
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageFilePath);
//                imageView.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

    }

    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile1() {
        roomText = roomcode.getText().toString().trim();
        if (roomText.isEmpty()) {
            roomcode.setError("Enter RoomCode");
            return;
        }

        if (imageFilePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference fileReference = storageReference.child(System.currentTimeMillis()

                    + "." + getFileExtension(imageFilePath));

            mUploadTask = fileReference.putFile(imageFilePath)

                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

//                            Handler handler = new Handler();
//
//                            handler.postDelayed(new Runnable() {
//
//                                @Override
//
//                                public void run() {
//
//                                    mProgressBar.setProgress(0);
//
//                                }
//
//                            }, 500);

                        Toast.makeText(Upload_Photo.this, "Upload successful", Toast.LENGTH_LONG).show();

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!urlTask.isSuccessful());

                        Uri downloadUrl = urlTask.getResult();

                        Upload upload = new Upload("image",downloadUrl.toString());

                        mDatabase.child(roomText).child("image").setValue(upload);
                        Intent i=new Intent(getApplicationContext(),Upload_Video.class);
                        i.putExtra("room",roomText);
                        startActivity(i);

                    })

                    .addOnFailureListener(e -> Toast.makeText(Upload_Photo.this, e.getMessage(), Toast.LENGTH_SHORT).show())

                    .addOnProgressListener(taskSnapshot -> {
                        //displaying the upload progress
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    });

        } else {

            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();

        }

    }
    }
