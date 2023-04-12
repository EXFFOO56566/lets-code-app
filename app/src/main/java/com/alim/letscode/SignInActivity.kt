package com.alim.letscode

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.alim.letscode.Database.ApplicationData
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.io.ByteArrayOutputStream

class SignInActivity : AppCompatActivity() {

    private val myPermission = 105
    private lateinit var chooser : Intent
    private lateinit var profile: ImageView
    private val actionRequestGallery = 102
    private lateinit var applicationData:  ApplicationData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationData = ApplicationData(this)
        when (applicationData.theme) {
            0 -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> setTheme(R.style.AppThemeDark)
                    Configuration.UI_MODE_NIGHT_NO -> setTheme(R.style.AppTheme)
                }
            }
            1 -> setTheme(R.style.AppTheme)
            2 -> setTheme(R.style.AppThemeDark)
        }
        setContentView(R.layout.activity_sign_in)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        profile = findViewById(R.id.profile)

        val name = findViewById<TextInputEditText>(R.id.name)
        chooser = Intent.createChooser(intent, "Choose a Picture")

        findViewById<Button>(R.id.start).setOnClickListener{
            if (name.text.toString().isEmpty())
                Snackbar.make(it, "Enter a name", Snackbar.LENGTH_SHORT).show()
            else {
                applicationData.session = true
                applicationData.name = name.text.toString()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        findViewById<Button>(R.id.pic).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), myPermission)
            else
                startActivityForResult(chooser, actionRequestGallery)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            myPermission -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    startActivityForResult(chooser, actionRequestGallery)
                else Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show()
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                actionRequestGallery -> {
                    try {
                        profile.setImageURI(data?.data)
                        encodeTobase64(profile.drawToBitmap())
                    } catch (ex: Exception) {
                        Log.println(Log.ASSERT,"Exception", ex.toString())
                    }
                }
            }
        }
    }

    private fun encodeTobase64(image: Bitmap) {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
        applicationData.image = imageEncoded
    }
}
