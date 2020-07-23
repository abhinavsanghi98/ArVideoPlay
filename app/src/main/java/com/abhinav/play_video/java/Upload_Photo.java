package com.abhinav.play_video.java;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.abhinav.play_video.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Upload_Photo extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 101;
    String roomText;
    boolean roomExists=false;
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

            Picasso.get().load(imageFilePath).fit().centerCrop()
                    .into(imageView);

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


        if(imageFilePath!=null){

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference fileReference = storageReference.child(System.currentTimeMillis()

                    + "." + getFileExtension(imageFilePath));

            mUploadTask = fileReference.putFile(imageFilePath)

                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                        Toast.makeText(Upload_Photo.this, "Upload successful", Toast.LENGTH_LONG).show();

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!urlTask.isSuccessful());

                        Uri downloadUrl = urlTask.getResult();

                        Upload upload = new Upload("image",downloadUrl.toString());

                        mDatabase.child(roomText).child("image").setValue(upload);
                        Intent i=new Intent(getApplicationContext(), Upload_Video.class);
                        i.putExtra("room",roomText);
                        startActivity(i);
                        finish();

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
