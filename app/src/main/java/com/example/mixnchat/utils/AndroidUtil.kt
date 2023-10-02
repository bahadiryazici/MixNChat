package com.example.mixnchat.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.mixnchat.R
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import java.util.Date

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
    fun initDatePicker( context: Context, button: Button) : DatePickerDialog {
        val dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->

            val month = month + 1
            val date = makeDateString(day,month,year)
            button.text = date
        }
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val style = R.style.MyTimePickerDialogTheme
        val datePickerDialog = DatePickerDialog(context,style,dateSetListener,year,month,day)
        val millisecondsInYear: Long = 18 * 365 * 24 * 60 * 60 * 1000L
        datePickerDialog.datePicker.maxDate = Date().time - millisecondsInYear
        return datePickerDialog
    }
    private fun makeDateString(day: Int, month: Int, year: Int): String {
        return getMonthFormat(month) + " " + day + " " + year
    }
    private fun getMonthFormat(month: Int): String {
        return when (month) {
            1 -> " Jan"
            2 -> " Feb"
            3 -> " Mar"
            4 -> " Apr"
            5 -> " May"
            6 -> " Jun"
            7 -> " Jul"
            8 -> " Aug"
            9 -> " Sep"
            10 -> " Oct"
            11 -> " Nov"
            12 -> " Dec"
            else -> "Dec"
        }
    }
}