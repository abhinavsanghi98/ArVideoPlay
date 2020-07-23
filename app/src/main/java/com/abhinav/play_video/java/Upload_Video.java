package com.abhinav.play_video.java;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.abhinav.play_video.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Upload_Video extends AppCompatActivity {
    private static final int PICK_VIDEO_REQUEST = 211;
    private VideoView videoView;
    private Button video_chooser;
    private Button upload;
    private Uri videoFilePath;
    private StorageReference storageReference;
    private DatabaseReference mDatabase;
    private MediaController mediaController;
    String roomCode;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
        Intent intent=getIntent();
        roomCode=intent.getExtras().getString("room");
        videoView=findViewById(R.id.videoview);
        video_chooser=findViewById(R.id.button_video);
        upload=findViewById(R.id.create_gift);
        storageReference = FirebaseStorage.getInstance().getReference( Constants.STORAGE_PATH_UPLOADS);
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);

        videoView.setOnPreparedListener(mediaPlayer -> mediaPlayer.setOnVideoSizeChangedListener((mediaPlayer1, i, i1) -> {
            mediaController=new MediaController(Upload_Video.this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);

        }));
        videoView.start();
        video_chooser.setOnClickListener(view -> showVideoChooser());
        upload.setOnClickListener(view -> uploadFile1());
    }

//    @Override
//    public void onClick(View view) {
//        if(view==video_chooser)
//            showVideoChooser();;
//            if(view==upload)
//                uploadFinal();
//
//
//    }
    private void showVideoChooser() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_VIDEO_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_VIDEO_REQUEST && resultCode==RESULT_OK && data!=null){
            videoFilePath=data.getData();
            videoView.setVideoURI(videoFilePath);

        }

    }

    private void uploadFinal(){
        if (videoFilePath != null) {
            //displaying progress dialog while image is uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            //getting the storage reference
            StorageReference sRef = storageReference.child(Constants.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + "." + getFileExtension(videoFilePath));

            //adding the file to reference
            sRef.putFile(videoFilePath)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            //dismissing the progress dialog
//                            progressDialog.dismiss();
//
//                            //displaying success toast
//                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
//
//                            //creating the upload object to store uploaded image details
//                            //Upload upload = new Upload("image",taskSnapshot.get);
//
//                            //adding an upload to firebase database
//                            String uploadId = mDatabase.push().getKey();
//                            mDatabase.child(roomCode).child("image").setValue(upload);
//                        }
//                    })
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                        Upload upload1= new Upload("image",task.getResult().getStorage().getDownloadUrl().toString());
                        //String uploadId = mDatabase.push().getKey();
                        mDatabase.child(roomCode).child("video").setValue(upload1);



                    })
                    .addOnFailureListener(exception -> {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        //displaying the upload progress
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    });
        } else {
            //display an error if no file is selected
        }
    }

    private void uploadFile1() {

        if (videoFilePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference fileReference = storageReference.child(System.currentTimeMillis()

                    + "." + getFileExtension(videoFilePath));

            mUploadTask = fileReference.putFile(videoFilePath)

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

                        Toast.makeText(Upload_Video.this, "Upload successful", Toast.LENGTH_LONG).show();

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!urlTask.isSuccessful());

                        Uri downloadUrl = urlTask.getResult();

                        Upload upload = new Upload("video",downloadUrl.toString());

                        mDatabase.child(roomCode).child("video").setValue(upload);
                        finish();


                    })

                    .addOnFailureListener(e -> Toast.makeText(Upload_Video.this, e.getMessage(), Toast.LENGTH_SHORT).show())

                    .addOnProgressListener(taskSnapshot -> {
                        //displaying the upload progress
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    });

        } else {

            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();

        }

    }



    public String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
