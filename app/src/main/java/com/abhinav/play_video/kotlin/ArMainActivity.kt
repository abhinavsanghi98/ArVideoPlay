package com.abhinav.play_video.kotlin

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.abhinav.play_video.R
import java.io.FileInputStream

class ArMainActivity : AppCompatActivity() {
   lateinit var bitmap: Bitmap

    private val openGlVersion by lazy {
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_main)
//        var intent =intent
//        var filename: String? =intent.getStringExtra("BitmapImage")
//        var fileInputStream:FileInputStream=openFileInput(filename)
        //bitmap=BitmapFactory.decodeStream(fileInputStream)
       // fileInputStream.close()



        if (openGlVersion.toDouble() >= MIN_OPEN_GL_VERSION) {
            supportFragmentManager.inTransaction { replace(R.id.fragmentContainer, ArVideoFragment()) }
        } else {
            AlertDialog.Builder(this)
                .setTitle("Device is not supported")
                .setMessage("OpenGL ES 3.0 or higher is required. The device is running OpenGL ES $openGlVersion.")
                .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
                .show()
        }
    }

    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
        beginTransaction().func().commit()
    }

    companion object {
        private const val MIN_OPEN_GL_VERSION = 3.0
    }
}