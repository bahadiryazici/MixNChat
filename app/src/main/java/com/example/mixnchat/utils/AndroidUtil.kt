package com.example.mixnchat.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.mixnchat.R
import com.google.android.material.snackbar.Snackbar

class AndroidUtil {


    fun showToast(context: Context, message : String){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }

    fun askPermission(context: Context, activity: Activity, view : View, permissionLauncher: ActivityResultLauncher<String>, activityResultLauncher: ActivityResultLauncher<Intent>){
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, activity.getString(R.string.permission), Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"
                ) {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()
            } else {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    fun startAnimation(animationView: LottieAnimationView, scrollView: ScrollView){
        animationView.playAnimation()
        scrollView.visibility = View.INVISIBLE
    }


    fun stopAnimation(animationView: LottieAnimationView,scrollView: ScrollView){
        animationView.pauseAnimation()
        animationView.cancelAnimation()
        animationView.visibility = View.INVISIBLE
        scrollView.visibility = View.VISIBLE
    }
}