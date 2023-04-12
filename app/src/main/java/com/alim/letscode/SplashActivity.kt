package com.alim.letscode

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.alim.letscode.Database.ApplicationData
import com.alim.letscode.Database.MainDB
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("SdCardPath")
class SplashActivity : AppCompatActivity() {

    var learn = "C"
    private var show = true
    private val myPermission = 101
    private lateinit var mainDB: MainDB
    private lateinit var progressDialog: ProgressDialog
    private val cUrl = "https://raw.githubusercontent.com/your-github-id/LetsCode/master/app/release/output.json"
    private val dUrl = "https://github.com/your-github-id/LetsCode/raw/master/app/release/app-release.apk"
    private final val path = "/sdcard/Android/data/com.alim.letscode/Update/LetsCode.apk"

    private val threadFirst = Thread(Runnable {
        try {
            val inputStream: InputStream = when (learn) {
                "C_P" -> resources.openRawResource(R.raw.c_p_data)
                "J" -> resources.openRawResource(R.raw.j_data)
                "P" -> resources.openRawResource(R.raw.p_data)
                "K" -> resources.openRawResource(R.raw.k_data)
                else -> resources.openRawResource(R.raw.c_data)
            }
            val jsonString: String = readTextFile(inputStream)
            val jsonObject = JSONObject(jsonString)
            val loop = jsonObject.getJSONObject("pageInfo").getInt("resultsPerPage")
            for (x in 0 until loop) {
                try {
                    val obj = jsonObject.getJSONArray("items").getJSONObject(x).getJSONObject("snippet")
                    mainDB.setTitle(learn, x, obj.getString("title"))
                    mainDB.setLink(learn, x, obj.getJSONObject("resourceId").getString("videoId"))
                    mainDB.setThumb(learn, x, obj.getJSONObject("thumbnails").getJSONObject("default").getString("url"))

                } catch (e: Exception) {
                    Log.println(Log.ASSERT,"Exception", e.toString())
                }
            }
            learn = when(learn) {
                "C" -> "C_P"
                "C_P" -> "J"
                "J" -> "P"
                "P" -> "K"
                else -> "D"
            }
            reCall()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    })
    
    private val threadCheck = Thread {
        try {
            val httpClient = DefaultHttpClient()
            val httpGet = HttpGet(cUrl)
            val response = httpClient.execute(httpGet)
            val httpEntity = response.entity
            val jsonArray = JSONArray(EntityUtils.toString(httpEntity))
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            val versionName: String = jsonObject.getJSONObject("apkData").getString("versionName")
            val versioncode: Int = jsonObject.getJSONObject("apkData").getInt("versionCode")
            val versionCode = BuildConfig.VERSION_CODE
            when {
                versioncode < versionCode -> {
                    runOnUiThread {
                        Toast.makeText(this, "Server is under maintenance.", Toast.LENGTH_SHORT).show()
                    }
                }
                versioncode > versionCode -> {
                    Log.println(Log.ASSERT,"DATA","$versioncode and $versionCode")
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Update available")
                            .setCancelable(false)
                            .setMessage("Current Version : ${BuildConfig.VERSION_NAME}\nUpdated version : $versionName")
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .setPositiveButton("Update") { dialog, _ ->
                                dialog.dismiss()
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    myPermission
                                )
                            }
                            .show()
                    }
                }
                else -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        } catch (e: Exception) {
            Log.println(Log.ASSERT,"ERROR",e.toString())
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private val threadDownload = Thread {
        val ID = 101
        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, "DOWNLOAD").apply {
            setContentTitle("Let's Code")
            setOngoing(true)
            setContentText("Download in progress")
            setSmallIcon(R.drawable.file_download_white)
            priority = NotificationCompat.PRIORITY_LOW
        }

        runOnUiThread {
            progressDialog.setMessage("Downloading")
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.isIndeterminate = true
            progressDialog.isIndeterminate = false
            progressDialog.show()
        }

        NotificationManagerCompat.from(this).apply {
            builder.setProgress(100, 0, true)
            notify(ID, builder.build())
            var input: InputStream? = null
            var output: OutputStream? = null
            var connection: HttpURLConnection? = null
            try {
                val url = URL(dUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val fileLength: Int = connection.contentLength
                input = connection.inputStream
                val file = File("/sdcard/Android/data/com.alim.letscode/Update/")
                if (!file.exists()) file.mkdirs()
                output = FileOutputStream(path)
                val data = ByteArray(4096)
                var total: Long = 0
                runOnUiThread {
                    progressDialog.max = fileLength
                }
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    if (show) {
                        show = false
                        builder.setProgress(100, ((total*100)/fileLength).toInt(), false)
                        notify(ID, builder.build())
                        Handler(Looper.getMainLooper()).postDelayed({
                            show = true
                        }, 750)
                    }
                    runOnUiThread {
                        progressDialog.progress = total.toInt()
                    }
                    output.write(data, 0, count)
                }
                builder.setSmallIcon(R.drawable.check_circle_white)
                builder.setOngoing(false)
                builder.setContentText("Download complete")
                    .setProgress(0, 0, false)
                notify(ID, builder.build())
                progressDialog.dismiss()
                install()
            } catch (e: Exception) {
                builder.setProgress(0, 0, false)
                builder.setOngoing(false)
                builder.setSmallIcon(R.drawable.ic_error_white_24dp)
                builder.setContentTitle("Error : $e")
                notify(ID, builder.build())
                progressDialog.dismiss()
            } finally {
                try {
                    output?.close()
                    input?.close()
                } catch (e: IOException) {
                    Log.println(Log.ASSERT,"ERROR", e.toString())
                }
                connection?.disconnect()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val applicationData = ApplicationData(this)
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
        setContentView(R.layout.activity_splash)

        progressDialog = ProgressDialog(this)
        mainDB = MainDB(this)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val logo: ImageView = findViewById(R.id.logo)
        val prog = findViewById<ProgressBar>(R.id.prog)
        val params_logo: ViewGroup.LayoutParams = logo.layoutParams
        val params_prog = prog.layoutParams
        params_logo.height = (screenWidth / 2)
        params_logo.width = (screenWidth / 2)
        params_prog.width = (screenWidth / 3)

        if (applicationData.session)
            Thread(threadCheck).start()
        else
            Thread(threadFirst).start()
    }

    private fun install() {
        var intent: Intent? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val apkUri: Uri = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider",
                    File(path)
                )
                intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                intent.data = apkUri
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            } else {
                val apkUri: Uri = Uri.fromFile(File(path))
                intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            //pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        } catch (e: java.lang.Exception) {
            Log.println(Log.ASSERT, "INSTALL", e.toString())
        }
        this.startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Updater"
            val descriptionText = "This notification channel will be used to show the updating progress"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("DOWNLOAD", name, importance)
                .apply { description = descriptionText }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun readTextFile(inputStream: InputStream): String {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var len: Int
        try {
            while (inputStream.read(buf).also { len = it } != -1) {
                outputStream.write(buf, 0, len)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            Log.println(Log.ASSERT,"IO Exception", e.toString())
        }
        return outputStream.toString()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            myPermission -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Thread(threadDownload).start()
                }
                else { Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show() }
                return
            }
        }
    }

    private fun reCall () {
        if (learn != "D")
            Thread(threadFirst).start()
        else {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}