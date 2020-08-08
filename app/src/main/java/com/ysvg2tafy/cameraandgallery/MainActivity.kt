package com.ysvg2tafy.cameraandgallery

import android.Manifest
import android.R.attr.bitmap
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import pl.aprilapps.easyphotopicker.*


class MainActivity : AppCompatActivity() {

    lateinit var easyImage:EasyImage

    private val PHOTOS_KEY = "easy_image_photos_list"
    private val CHOOSER_PERMISSIONS_REQUEST_CODE = 7459
    private val CAMERA_REQUEST_CODE = 7500
    private val CAMERA_VIDEO_REQUEST_CODE = 7501
    private val GALLERY_REQUEST_CODE = 7502
    private val DOCUMENTS_REQUEST_CODE = 7503

    private val photos: ArrayList<MediaFile> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        easyImage = EasyImage.Builder(this) // Chooser only
                // Will appear as a system chooser title, DEFAULT empty string
                .setChooserTitle("Pick media")
                // Will tell chooser that it should show documents or gallery apps
                //.setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)  you can use this or the one below
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                // Setting to true will cause taken pictures to show up in the device gallery, DEFAULT false
                .setCopyImagesToPublicGalleryFolder(false) // Sets the name for images stored if setCopyImagesToPublicGalleryFolder = true
                .setFolderName("EasyImage sample") // Allow multiple picking
                .allowMultiple(false)
                .build()

        checkGalleryAppAvailability()



        gallery_button.setOnClickListener {
            val necessaryPermissions = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (arePermissionsGranted(necessaryPermissions)) {
                easyImage.openGallery(this@MainActivity)
            } else {
                requestPermissionsCompat(necessaryPermissions, GALLERY_REQUEST_CODE)
            }
        }

        camera_button.setOnClickListener {
            val necessaryPermissions = arrayOf<String>(Manifest.permission.CAMERA)
            if (arePermissionsGranted(necessaryPermissions)) {
                easyImage.openCameraForImage(this@MainActivity)
            } else {
                requestPermissionsCompat(necessaryPermissions, CAMERA_REQUEST_CODE)
            }
        }

        camera_video_button.setOnClickListener {
            val necessaryPermissions = arrayOf<String>(Manifest.permission.CAMERA)
            if (arePermissionsGranted(necessaryPermissions)) {
                easyImage.openCameraForVideo(this@MainActivity)
            } else {
                requestPermissionsCompat(necessaryPermissions, CAMERA_VIDEO_REQUEST_CODE)
            }
        }

        documents_button.setOnClickListener {
            val necessaryPermissions = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (arePermissionsGranted(necessaryPermissions)) {
                easyImage.openDocuments(this@MainActivity)
            } else {
                requestPermissionsCompat(necessaryPermissions, DOCUMENTS_REQUEST_CODE)
            }
        }

        chooser_button.setOnClickListener {
            val necessaryPermissions = arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (arePermissionsGranted(necessaryPermissions)) {
                easyImage.openChooser(this@MainActivity)
            } else {
                requestPermissionsCompat(necessaryPermissions, CHOOSER_PERMISSIONS_REQUEST_CODE)
            }
        }

        cache_button.setOnClickListener {
            println(this.cacheDir.totalSpace)
            println(this.cacheDir.deleteRecursively())
            println(this.cacheDir.totalSpace)
            //println(this.filesDir.deleteRecursively())
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(PHOTOS_KEY, photos)
    }

    private fun checkGalleryAppAvailability() {
        if (!easyImage.canDeviceHandleGallery()) {
            //Device has no app that handles gallery intent
            gallery_button.visibility=View.INVISIBLE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CHOOSER_PERMISSIONS_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openChooser(this@MainActivity)
        } else if (requestCode == CAMERA_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openCameraForImage(this@MainActivity)
        } else if (requestCode == CAMERA_VIDEO_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openCameraForVideo(this@MainActivity)
        } else if (requestCode == GALLERY_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openGallery(this@MainActivity)
        } else if (requestCode == DOCUMENTS_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openDocuments(this@MainActivity)
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        easyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                println(imageFiles)
                Log.i("RDX",imageFiles.toString())
                Log.i("RDX2",photos.toString())

                var bitmap=BitmapFactory.decodeFile(imageFiles[0].file.path)

                val ei = ExifInterface(imageFiles[0].file.path)
                val orientation: Int = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED)

                fun rotateImage(source:Bitmap, angle:Float): Bitmap? {
                    var matrix = Matrix()
                    matrix.postRotate(angle);
                    return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                            matrix, true);
                }

                var rotatedBitmap: Bitmap? = null
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(bitmap, 270f)
                    ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
                    else -> rotatedBitmap = bitmap
                }


                display.setImageBitmap(rotatedBitmap)
            }

            override fun onImagePickerError(error: Throwable, source: MediaSource) {
                //Some error handling
                error.printStackTrace()
            }

            override fun onCanceled(source: MediaSource) {
                //Not necessary to remove any files manually anymore
            }
        })
    }

    /*private fun onPhotosReturned(returnedPhotos: Array<MediaFile>) {
        photos.addAll(Arrays.asList(returnedPhotos))
        imagesAdapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(photos.size - 1)
    }*/

    private fun arePermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    private fun requestPermissionsCompat(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this@MainActivity, permissions, requestCode)
    }


}
