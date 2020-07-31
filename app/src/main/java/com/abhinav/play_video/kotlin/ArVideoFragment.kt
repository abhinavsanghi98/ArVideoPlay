package com.abhinav.play_video.kotlin

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.*
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.animation.doOnStart
import androidx.core.graphics.rotationMatrix
import androidx.core.graphics.transform
import com.abhinav.play_video.R
import com.abhinav.play_video.helpers.BitmapHelper
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException

open class ArVideoFragment : ArFragment() {

    private lateinit var mp: MediaPlayer
    private lateinit var externalTexture: ExternalTexture
    private lateinit var videoModel: ModelRenderable
    private lateinit var anchorVideoNode: VideoAnchorNode
   // private lateinit var videoAnchorNode: AnchorNode
   // private lateinit var filename:String

    private lateinit var arAct:ArMainActivity

    private var trackedAugmentedImages: AugmentedImage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //filename="/storage/emulated/0/234.mp4"
        mp = MediaPlayer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        arAct= activity as ArMainActivity
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        arSceneView.isLightEstimationEnabled = false

        initializeSession()
        createScene()

        return view
    }

    override fun getSessionConfiguration(session: Session): Config {

       fun loadAugmentedImageBitmap(imageName: String): Bitmap=
    requireContext().assets.open(imageName).use { return BitmapFactory.decodeStream(it) }

        fun setupAugmentedImageDatabase(config: Config, session: Session): Boolean {
            try {
                config.augmentedImageDatabase = AugmentedImageDatabase(session).also { db ->

                     db.addImage(TEST_VIDEO_2, BitmapHelper.getInstance().bitmap)
                    db.addImage(TEST_VIDEO_1,loadAugmentedImageBitmap(TEST_IMAGE_1))
                }
                return true
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Could not add bitmap to augmented image database", e)
            } catch (e: IOException) {
                Log.e(TAG, "IO exception loading augmented image bitmap.", e)
            }
            return false
        }

        return super.getSessionConfiguration(session).also {
            it.lightEstimationMode = Config.LightEstimationMode.DISABLED
            it.focusMode = Config.FocusMode.AUTO

            if (!setupAugmentedImageDatabase(it, session)) {
                Toast.makeText(requireContext(), "Could not setup augmented image database", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createScene() {
        // Create an ExternalTexture for displaying the contents of the video.
        externalTexture = ExternalTexture().also {
            mp.setSurface(it.surface)
        }

        // Create a renderable with a material that has a parameter of type 'samplerExternal' so that
        // it can display an ExternalTexture.
        ModelRenderable.builder()
            .setSource(requireContext(), R.raw.augmented_video_model)
            .build()
            .thenAccept { renderable ->
                videoModel = renderable
                renderable.isShadowCaster = false
                renderable.isShadowReceiver = false
                renderable.material.setExternalTexture("videoTexture", externalTexture)
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Could not create ModelRenderable", throwable)
                return@exceptionally null
            }

        anchorVideoNode = VideoAnchorNode().apply {
            setParent(arSceneView.scene)
        }
    }

    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView.arFrame ?: return

        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)

        // If current active augmented image isn't tracked anymore and video playback is started - pause video playback
        val nonFullTrackingImages = updatedAugmentedImages.filter { it.trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING }
        trackedAugmentedImages?.let { activeAugmentedImage ->
            if (isArVideoPlaying() && nonFullTrackingImages.any { it.index == activeAugmentedImage.index }) {
                pauseArVideo()
            }
        }

        val fullTrackingImages = updatedAugmentedImages.filter { it.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING }
        if (fullTrackingImages.isEmpty()) return

        // If current active augmented image is tracked but video playback is paused - resume video playback
        trackedAugmentedImages?.let { activeAugmentedImage ->
            if (fullTrackingImages.any { it.index == activeAugmentedImage.index }) {
                if (!isArVideoPlaying()) {
                    resumeArVideo()
                }
                return
            }
        }

        // Otherwise - make the first tracked image active and start video playback
        fullTrackingImages.firstOrNull()?.let { augmentedImage ->
            try {
                playbackArVideo(augmentedImage)
            } catch (e: Exception) {
                Log.e(TAG, "Could not play video [${augmentedImage.name}]", e)
            }
        }
    }

    private fun isArVideoPlaying() = mp.isPlaying

    private fun pauseArVideo() {
        anchorVideoNode.renderable = null
        mp.pause()
    }



    private fun resumeArVideo() {
        mp.start()
        fadeInVideo()
    }


    private fun dismissArVideo() {
        anchorVideoNode.anchor?.detach()
        anchorVideoNode.renderable = null
        trackedAugmentedImages = null
        mp.reset()
    }


    private fun playbackArVideo(augmentedImage: AugmentedImage) {
        Log.d(TAG, "playbackVideo = ${augmentedImage.name}")

        requireContext().assets.openFd(augmentedImage.name)
                .use { a ->

                    val metadataRetriever = MediaMetadataRetriever()
                    metadataRetriever.setDataSource(
                            a.fileDescriptor,
                            a.startOffset,
                            a.length
                    )

                    val videoWidth = metadataRetriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH)?.toFloatOrNull()
                            ?: 0f
                    val videoHeight = metadataRetriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT)?.toFloatOrNull()
                            ?: 0f
                    val videoRotation = metadataRetriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION)?.toFloatOrNull()
                            ?: 0f

                    // Account for video rotation, so that scale logic math works properly
                    val imageSize = RectF(0f, 0f, augmentedImage.extentX, augmentedImage.extentZ)
                            .transform(rotationMatrix(videoRotation))

                    val videoScaleType = VideoScaleType.CenterCrop

                    anchorVideoNode.setVideoProperties(
                            videoWidth = videoWidth, videoHeight = videoHeight, videoRotation = videoRotation,
                            imageWidth = imageSize.width(), imageHeight = imageSize.height(),
                            videoScaleType = videoScaleType
                    )

                    // Update the material parameters
                    videoModel.material.setFloat2(MATERIAL_IMAGE_SIZE, imageSize.width(), imageSize.height())
                    videoModel.material.setFloat2(MATERIAL_VIDEO_SIZE, videoWidth, videoHeight)
                    videoModel.material.setBoolean(MATERIAL_VIDEO_CROP, VIDEO_CROP_ENABLED)

                    mp.reset()
                    mp.setDataSource(a)
                }.also {
                    mp.isLooping = true
                    mp.prepare()
                    mp.start()
                }





        anchorVideoNode.anchor?.detach()
        anchorVideoNode.anchor = augmentedImage.createAnchor(augmentedImage.centerPose)

        trackedAugmentedImages = augmentedImage

        externalTexture.surfaceTexture.setOnFrameAvailableListener{
            it.setOnFrameAvailableListener(null)
            fadeInVideo()
        }
    }
//private fun playbackArVideo(augmentedImage: AugmentedImage) {
//    Log.d(TAG, "playbackVideo = ${augmentedImage.name}")
//
//    requireContext().assets.openFd(augmentedImage.name)
//            .use { descriptor ->
//                mp.reset()
//                context?.let { mp.setDataSource(it,Uri.parse(filename)) }
//            }.also {
//                mp.isLooping = true
//                mp.prepare()
//                mp.start()
//            }
//
//
//
//    videoAnchorNode.anchor?.detach()
//        videoAnchorNode.anchor = augmentedImage.createAnchor(augmentedImage.centerPose)
//        videoAnchorNode.localScale = Vector3(
//                augmentedImage.extentX, // width
//                1.0f,
//                augmentedImage.extentZ
//        ) // height
//
//        trackedAugmentedImages = augmentedImage
//
//        externalTexture.surfaceTexture.setOnFrameAvailableListener {
//            it.setOnFrameAvailableListener(null)
//            videoAnchorNode.renderable = videoModel
//
//    }
//}


    private fun fadeInVideo() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400L
            interpolator = LinearInterpolator()
            addUpdateListener { v ->
                videoModel.material.setFloat(MATERIAL_VIDEO_ALPHA, v.animatedValue as Float)
            }
            doOnStart { anchorVideoNode.renderable = videoModel }
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        dismissArVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        mp.release()
    }

    companion object {
        private const val TAG = "ArVideoFragment"
        private const val TEST_IMAGE_1 = "t1.jpeg"


        private const val TEST_VIDEO_1 = "V1.mp4"
        private const val TEST_VIDEO_2 = "test_video_1.mp4"

        private const val VIDEO_CROP_ENABLED = true

        private const val MATERIAL_IMAGE_SIZE = "imageSize"
        private const val MATERIAL_VIDEO_SIZE = "videoSize"
        private const val MATERIAL_VIDEO_CROP = "videoCropEnabled"
        private const val MATERIAL_VIDEO_ALPHA = "videoAlpha"
    }
}