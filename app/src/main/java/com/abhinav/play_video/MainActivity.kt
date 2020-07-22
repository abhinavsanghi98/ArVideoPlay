package com.abhinav.play_video

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {
    /*

    we are extracting 2 urls from the previous activity(I havent added yet those activities to this)
    something like:

    String imageUrl="https://firebasestorage.googleapis.com/v0/b/artest-e2d69.appspot.com/o/uploads%2F1595434893978.png?alt=media&token=3005850e-e71e-408f-9f44-9dd6ec540959";
    String videoUrl="https://firebasestorage.googleapis.com/v0/b/artest-e2d69.appspot.com/o/uploads%2F1595434927220.mp4?alt=media&token=e3463ccf-e40f-4e5f-97ef-41cf78bd22a6";
     */
    /* We are setting up the database of ar in setupAugmentedImageDatabase function of ArVideoFragment
    So,till now, in that function,we are creating bitmap of image1.png(Stored in Asset Folder) and add this bitmap and video(Also stored in asset) to database of ar

    See setupAugmentedImageDatabase function of ArVideoFragment
    But,we want this image and video should be extracted from imageUrl and videoUrl and then should added to database of ar


    Main role is in ArVideoFragment class where initialize session function will setup the database and other things
    and then createArScene() will create the scene.


     */

    private val openGlVersion by lazy {
        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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