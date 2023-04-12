package com.alim.letscode.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import com.alim.letscode.Class.CircleImageView
import com.alim.letscode.Class.GlobalVariable
import com.alim.letscode.Database.ApplicationData
import com.alim.letscode.Database.MainDB
import com.alim.letscode.Database.OfflineData
import com.alim.letscode.Interface.NavigationInterface
import com.alim.letscode.MainActivity
import com.alim.letscode.PlayerActivity
import com.alim.letscode.R
import com.commit451.youtubeextractor.Stream
import com.commit451.youtubeextractor.YouTubeExtractor
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.util.*

class SettingsFragment : Fragment() {

    var loc = "C"
    var total = 176
    var show = true
    var recall = false
    private lateinit var profile: CircleImageView
    private val storagePermissionCode = 105
    private lateinit var chooser : Intent
    private val actionRequestGalleryCode = 102
    private lateinit var applicationData: ApplicationData
    private val myPermission = 101
    lateinit var mainDB: MainDB
    lateinit var offlineData: OfflineData
    val globalVariable = GlobalVariable()

    lateinit var c: Button
    lateinit var cp: Button
    lateinit var j: Button
    lateinit var p: Button
    lateinit var k: Button

    @SuppressLint("SdCardPath")
    private val thread = Thread {
        val pos = globalVariable.getDownloadingPos()
        val name = when {
            pos <10 -> "00$pos"
            pos <100 -> "0$pos"
            else -> "$pos"
        }+". " + mainDB.getTitle(loc, globalVariable.getDownloadingPos())


        val extractor = YouTubeExtractor.Builder()
            .build()
        extractor.extract(mainDB.getLink(loc, globalVariable.getDownloadingPos()))
            .subscribeOn(Schedulers.io())
            .subscribe({ extraction ->
                activity!!.runOnUiThread {
                    val url = if (Build.VERSION.SDK_INT < 23) extraction.streams.filterIsInstance<Stream.VideoStream>().get(0).url
                    else extraction.streams.filterIsInstance<Stream.VideoStream>().get(1).url

                    val iD = 1101
                    createNotificationChannel()
                    total = when(loc) {
                        "C_P" -> 149
                        "J" -> 92
                        "P" -> 110
                        "K" -> 47
                        else -> 176
                    }
                    val builder = NotificationCompat.Builder(GlobalVariable.context,
                        "DOWNLOAD").apply {
                        setContentTitle(mainDB.getTitle(loc, pos) + "  ${pos+1}/$total")
                        setContentText("Download in progress")
                        setOngoing(true)
                        setSmallIcon(R.drawable.file_download_white)
                        priority = NotificationCompat.PRIORITY_LOW
                    }

                    NotificationManagerCompat.from(GlobalVariable.context).apply {
                        builder.setProgress(100, 0, true)
                        notify(iD, builder.build())
                        globalVariable.setTask(loc)
                        globalVariable.setDownloading(true)

                        PRDownloader.download(url, "/sdcard/Android/data/com.alim.letscode/$loc",
                            "$name.code")
                            .build()
                            .setOnProgressListener { progress ->
                                globalVariable.setTotal(progress.totalBytes.toInt())
                                globalVariable.setProgress(progress.currentBytes.toInt())
                                if (show) {
                                    show = false
                                    builder.setProgress(
                                        progress.totalBytes.toInt(),
                                        progress.currentBytes.toInt(),
                                        false
                                    )
                                    notify(iD, builder.build())
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        show = true
                                    }, 750)
                                }
                            }
                            .start(object : OnDownloadListener {
                                override fun onDownloadComplete() {
                                    builder.setSmallIcon(R.drawable.check_circle_white)
                                    builder.setContentText("Download complete")
                                        .setProgress(0, 0, false)
                                    builder.setOngoing(false)
                                    notify(iD, builder.build())
                                    globalVariable.setDownloadingPos(globalVariable.getDownloadingPos() + 1)
                                    offlineData.setOffline(loc, pos.toString(),
                                        "/sdcard/Android/data/com.alim.letscode/$loc/$name.code", true)
                                    reCall()
                                    //Toast.makeText(activity!!, "DONE", Toast.LENGTH_LONG).show()
                                    recall = false
                                }

                                override fun onError(error: com.downloader.Error?) {
                                    globalVariable.setDownloading(false)
                                    if (!recall)
                                        reCall()
                                    Log.println(Log.ASSERT,"Error", error.toString())
                                    recall = true
                                }
                            })
                    }
                }
            }, { t ->
                globalVariable.setDownloading(false)
                if (!recall)
                    reCall()
                Log.println(Log.ASSERT,"Error", t.toString())
                recall = true
                Toast.makeText(activity!!, t.message, Toast.LENGTH_LONG).show()
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        mainDB = MainDB(activity!!)
        offlineData = OfflineData(activity!!)
        applicationData = ApplicationData(activity!!)
        rootView.findViewById<TextView>(R.id.name).text = applicationData.name

        MobileAds.initialize(activity!!)
        val template = rootView.findViewById<TemplateView>(R.id.my_template)
        val styles = NativeTemplateStyle.Builder().build()

        val adLoader = AdLoader.Builder(activity!!, resources.getString(R.string.native_ad_id))
            .forUnifiedNativeAd {
                template.setStyles(styles)
                template.setNativeAd(it)
            }.withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    template.visibility = View.VISIBLE
                }
            })
            .build()
        Handler().postDelayed({
            adLoader.loadAd(AdRequest.Builder().build())
        },1000)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        chooser = Intent.createChooser(intent, "Choose a Picture")

        profile = rootView.findViewById(R.id.pic)

        val name = rootView.findViewById<TextInputEditText>(R.id.text)
        name.clearFocus()

        Handler().postDelayed({
            name.text = Editable.Factory.getInstance().newEditable(applicationData.name)
            if (applicationData.image.isNotEmpty())
                profile.setImageBitmap(decodeBase64(applicationData.image))
        },300)

        val chipGroup = rootView.findViewById<ChipGroup>(R.id.chip_theme)
        val chipL = rootView.findViewById<Chip>(R.id.chip_l)
        val chipD = rootView.findViewById<Chip>(R.id.chip_d)
        val chipS = rootView.findViewById<Chip>(R.id.chip_s)

        c = rootView.findViewById<Button>(R.id.download_all_c)
        cp = rootView.findViewById<Button>(R.id.download_all_c_p)
        j = rootView.findViewById<Button>(R.id.download_all_j)
        p = rootView.findViewById<Button>(R.id.download_all_p)
        k = rootView.findViewById<Button>(R.id.download_all_k)

        globalVariable.registerNavigaionInterface2(object : NavigationInterface{
            override fun Position(pos: Int) {
                if (pos==3) {
                    registerNav()
                }
            }
        })

        rootView.findViewById<Button>(R.id.change_pic).setOnClickListener {
            if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), storagePermissionCode)
            else
                startActivityForResult(chooser, actionRequestGalleryCode)
        }

        rootView.findViewById<Button>(R.id.change_name).setOnClickListener {
            applicationData.name = name.text.toString()
            rootView.findViewById<TextView>(R.id.name).text = name.text.toString()
            name.clearFocus()
            //name.text = Editable.Factory.getInstance().newEditable(applicationData.name)
        }

        rootView.findViewById<ImageView>(R.id.git_hub).setOnClickListener {
            context!!.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://github.com/SouravAlim"))
            )
        }

        rootView.findViewById<ImageView>(R.id.twitter).setOnClickListener {
            try {
                context!!.packageManager.getPackageInfo("com.twitter.android", 0)
                context!!.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=sourav_alim")
                    )
                )
            } catch (e: Exception) {
                context!!.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://twitter.com/sourav_alim"))
                )
            }
        }

        rootView.findViewById<ImageView>(R.id.facebook).setOnClickListener {
            try {
                context!!.packageManager.getPackageInfo("com.facebook.katana", 0)
                context!!.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("fb://profile/100006302621357")
                    )
                )
            } catch (e: java.lang.Exception) {
                context!!.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/alim.sourav"))
                )
            }
        }

        rootView.findViewById<ImageView>(R.id.instagram).setOnClickListener {
            try {
                context!!.packageManager.getPackageInfo("com.instagram.android", 0)
                context!!.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/alim_sourav")
                    )
                )
            } catch (e: java.lang.Exception) {
                context!!.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://instagram.com/alim_sourav"))
                )
            }
        }


        when (applicationData.theme) {
            0 -> {
                chipS.isClickable = false
                chipS.isChecked = true
            }
            1 -> {
                chipL.isClickable = false
                chipL.isChecked = true
            }
            2 -> {
                chipD.isClickable = false
                chipD.isChecked = true
            }
        }

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            chipL.isClickable = true
            chipD.isClickable = true
            chipS.isClickable = true
            themeChange(checkedId)
            reCreate()
        }

        c.setOnClickListener { download("C") }

        cp.setOnClickListener { download("C_P") }

        j.setOnClickListener { download("J") }

        p.setOnClickListener { download("P") }

        k.setOnClickListener { download("K") }

        downloadButtonStatus()

        return rootView
    }

    private fun downloadButtonStatus() {
        if (offlineData.getAll("C")) { c.isEnabled = false
            c.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0 ) }

        if (offlineData.getAll("C_P")) { cp.isEnabled = false
            cp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0 ) }

        if (offlineData.getAll("J")) { j.isEnabled = false
            j.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0 ) }

        if (offlineData.getAll("P")) { p.isEnabled = false
            p.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0 ) }

        if (offlineData.getAll("K")) { k.isEnabled = false
            k.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0 ) }
    }

    private fun themeChange(checkedId: Int) {
        when(checkedId) {
            R.id.chip_l -> { applicationData.theme = 1 }
            R.id.chip_d -> { applicationData.theme = 2 }
            R.id.chip_s -> { applicationData.theme = 0 }
        }
    }

    private fun  reCall() {
        if (!offlineData.getOffline(loc, globalVariable.getDownloadingPos().toString())) {
            if (total >= globalVariable.getDownloadingPos() + 1)
                Thread(thread).start()
            else
                globalVariable.setDownloading(false)
        } else {
            globalVariable.setDownloadingPos(globalVariable.getDownloadingPos() + 1)
            reCall()
        }
    }

    private fun registerNav() {
        if (offlineData.getAll("C")) {
            c.isEnabled = false
            c.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0)
        } else {
            c.isEnabled = true
            c.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.file_download_white,
                0,
                0,
                0
            )
        }
        if (offlineData.getAll("C_P")) {
            cp.isEnabled = false
            cp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0)
        } else {
            cp.isEnabled = true
            cp.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.file_download_white,
                0,
                0,
                0
            )
        }
        if (offlineData.getAll("J")) {
            j.isEnabled = false
            j.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0)
        } else {
            j.isEnabled = true
            j.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.file_download_white,
                0,
                0,
                0
            )
        }
        if (offlineData.getAll("P")) {
            p.isEnabled = false
            p.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0)
        } else {
            p.isEnabled = true
            p.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.file_download_white,
                0,
                0,
                0
            )
        }
        if (offlineData.getAll("K")) {
            k.isEnabled = false
            k.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0)
        } else {
            k.isEnabled = true
            k.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.file_download_white,
                0,
                0,
                0
            )
        }
    }

    private fun download(lan: String) {
        val text = when(lan) {
            "C_P" -> " C++ "
            "J" -> " Java "
            "P" -> " Python "
            "K" -> " Kotlin "
            else -> " C "
        }
        if (globalVariable.getDownloading())
            Toast.makeText(activity!!,"Wait until previous task finish",Toast.LENGTH_LONG).show()
        else {
            AlertDialog.Builder(activity!!)
                .setTitle("Download")
                .setMessage(activity!!.resources.getString(R.string.download_alert_first)+text
                        +activity!!.resources.getString(R.string.download_alert_last))
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Download") { _, _ ->
                    createNotificationChannel()
                    loc = lan
                    reCall() }.show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Downloader"
            val descriptionText = "This notification channel will be used to show the downloading progress"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("DOWNLOAD", name, importance)
                .apply { description = descriptionText }
            val notificationManager: NotificationManager = GlobalVariable.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            myPermission -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    reCall()
                }
                else { Toast.makeText(activity!!,"Permission Denied 1",Toast.LENGTH_LONG).show() }
                return
            }

            storagePermissionCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    startActivityForResult(chooser, actionRequestGalleryCode)
                else Toast.makeText(activity!!,"Permission Denied 2", Toast.LENGTH_LONG).show()
                return
            }
        }
        Log.println(Log.ASSERT,"PERM","Touch")
    }

    private fun reCreate() {
        val intent = Intent(activity!!, MainActivity::class.java)
        intent.putExtra("FROM", "SETTINGS")
        startActivity(intent)
        Objects.requireNonNull(activity!!.overridePendingTransition(R.anim.fade_in, R.anim.fade_out))
        activity!!.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                actionRequestGalleryCode -> {
                    val galleryImageUri: Uri? = data?.data
                    try {
                        val selectedImagePath = galleryImageUri?.path
                        profile.setImageURI(galleryImageUri)
                        encodeTobase64(profile.drawToBitmap())
                        Log.println(Log.ASSERT,"LOC",selectedImagePath.toString())
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        val selectedImagePath = galleryImageUri?.path
                        Log.println(Log.ASSERT,"LOC",selectedImagePath.toString())
                    }
                }
            }
        }
    }


    private fun decodeBase64(input: String?): Bitmap {
        val decodedByte: ByteArray = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    private fun encodeTobase64(image: Bitmap) {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
        applicationData.image = imageEncoded
    }
}