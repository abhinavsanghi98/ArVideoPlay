package com.abhinav.play_video.java;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import com.bumptech.glide.load.data.FileDescriptorLocalUriFetcher;
import com.bumptech.glide.load.model.StringLoader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Tempor extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileDescriptor fd = null;
        ParcelFileDescriptor parcelFileDescriptor=null;
        try {
            parcelFileDescriptor=new ParcelFileDescriptor(ParcelFileDescriptor.fromFd(2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
       // mediaMetadataRetriever.se

        File baseDir = Environment.getExternalStorageDirectory();
        String audioPath = baseDir.getAbsolutePath() + "234" + ".mp3";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(audioPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fd = fis.getFD();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
