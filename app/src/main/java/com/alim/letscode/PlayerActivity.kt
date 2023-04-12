package com.alim.letscode

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.alim.letscode.Adapter.PlayerAdapter
import com.alim.letscode.Class.GlobalVariable
import com.alim.letscode.Database.ApplicationData
import com.alim.letscode.Database.LearningData
import com.alim.letscode.Database.MainDB
import com.alim.letscode.Database.OfflineData
import com.alim.letscode.Interface.ClickInterface
import com.commit451.youtubeextractor.Stream
import com.commit451.youtubeextractor.YouTubeExtractor
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import io.reactivex.plugins.RxJavaPlugins.onError
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_player.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("SetTextI18n","SourceLockedOrientationActivity")
class PlayerActivity : AppCompatActivity() {

    companion object {
        var run = true
        var uri: Uri = Uri.parse("")
        var NOTIIFCATION_ID = 0
    }

    var LINK = ""
    var loop = 0
    var them = 0
    var show = true
    var PositioN = 0
    var first = true
    var playedSec = 0
    var Playing = false
    var playedMiliSec = 0
    private val tag = "Player Activity"
    var fullscreen = false
    private var LEARN = "C"
    lateinit var POS: TextView
    private val MY_PERMISSIONS = 101
    lateinit var smoothScroller: RecyclerView.SmoothScroller

    lateinit var mainDB: MainDB
    lateinit var learningData: LearningData

    lateinit var fullscreenButton: View
    private lateinit var offlineData: OfflineData
    private lateinit var prog: ProgressBar
    private lateinit var errorT: TextView
    private var player: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    private val link: MutableList<String> = ArrayList()
    private val position: MutableList<Int> = ArrayList()
    private val title: MutableList<String> = ArrayList()
    private val thumb: MutableList<String> = ArrayList()

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var adapter: PlayerAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private val threadOffline = Thread {
        title.clear()
        link.clear()
        position.clear()
        thumb.clear()
        val limit = when(LEARN) {
            "C_P" ->  149
            "J" ->  92
            "P" -> 110
            "K" -> 47
            else -> 176 }
        loop = limit
        for (x in 0..limit) {
            try {
                if (offlineData.getOffline(LEARN, x.toString())) {
                    val l = offlineData.getOfflineL(LEARN, x.toString())
                    link.add(l)
                    position.add(x)
                    title.add(l.substring(l.lastIndexOf("/")+1,l.length-5))
                }
            } catch (e: Exception) {
                Log.println(Log.ASSERT,tag, e.toString())
            }
            runOnUiThread {
                adapter.notifyDataSetChanged()
                smoothScroller.targetPosition = intent.getIntExtra("VIDEO_POS", 0)
                PositioN = intent.getIntExtra("VIDEO_POS", 0)
            }
        }
        loop = title.size
        POS.text = (intent.getIntExtra("VIDEO_POS",0) + 1).toString() + "/" + loop
        Log.println(Log.ASSERT, "position", position[PositioN].toString())
        LearningData(this).setSession(LEARN, position[PositioN])
    }

    private val thread = Thread(Runnable {
        try {
            link.clear()
            title.clear()
            thumb.clear()
            position.clear()
            val pos =  when (LEARN) {
                "C_P" -> 149
                "J" -> 92
                "P" -> 110
                "K" -> 47
                else -> 176 }
            loop = pos
            for (x in 0 until  pos) {
                position.add(x)
                link.add(mainDB.getLink(LEARN, x))
                title.add(mainDB.getTitle(LEARN, x))
                thumb.add(mainDB.getThumb(LEARN, x))
            }
            if (first) {
                runOnUiThread {
                    LINK = link[intent.getIntExtra("VIDEO_POS",0)]
                    smoothScroller.targetPosition = intent.getIntExtra("VIDEO_POS", 0)
                    PositioN = intent.getIntExtra("VIDEO_POS", 0)
                    POS.text = (intent.getIntExtra("VIDEO_POS", 0) + 1).toString() + "/" + loop
                    if (!intent.getBooleanExtra("SINGLE_DOWNLOAD",false))
                        extract("PLAY")
                }
            }
            runOnUiThread { adapter.notifyDataSetChanged() }
        } catch (e: Exception) {
            Log.println(Log.ASSERT,"ERROR", e.toString())
        }
    })

    private fun changeTheme() {
        val applicationData = ApplicationData(this)
        when (applicationData.theme) {
            0 -> {
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        them = 1
                        setTheme(R.style.AppThemeDark)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        them = 0
                        setTheme(R.style.AppTheme)
                    }
                }
            }
            1 ->  { them = 0
                setTheme(R.style.AppTheme) }
            2 -> { them = 1
                setTheme(R.style.AppThemeDark) }
        }
    }

    private fun launchApplication(titleText: TextView) {
        LEARN = intent.getStringExtra("MY_LOCATION")
        when (LEARN) {
            "C" -> titleText.text = "Code with C"
            "C_P" -> titleText.text = "Code with C++"
            "J" -> titleText.text = "Code with Java"
            "P" -> titleText.text = "Code with Python"
            "K" -> titleText.text = "Code with Kotlin"
        }

        if (intent.getBooleanExtra("SINGLE_DOWNLOAD",false)) {
            Thread(thread).start()
            uri = Uri.parse(intent.getStringExtra("VIDEO_ID"))
            initializePlayer()
        } else if (!intent.getBooleanExtra("DOWNLOADED",false)) {
            Thread(thread).start()
        } else {
            Thread(threadOffline).start()
            PositioN = intent.getIntExtra("VIDEO_POS",0)
            uri = Uri.parse(intent.getStringExtra("VIDEO_ID"))
            initializePlayer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeTheme()
        setContentView(R.layout.activity_player)

        mainDB = MainDB(this)
        learningData = LearningData(this)
        offlineData = OfflineData(this)
        playerView = findViewById(R.id.video_view)
        uri = Uri.parse("")
        fullscreenButton = video_view.findViewById(R.id.exo_fullscreen_icon)

        MobileAds.initialize(this) {}
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = resources.getString(R.string.interstitial_ad_id)
        Handler().postDelayed({
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        },2000)

        video_view.findViewById<ImageButton>(R.id.custom).setOnClickListener { playNext(PositioN+1) }
        video_view.findViewById<ImageButton>(R.id.previous).setOnClickListener {playNext(PositioN-1) }

        POS = findViewById(R.id.player_num)
        prog = findViewById(R.id.loading_prog)
        errorT = findViewById(R.id.error)
        val titleText: TextView = findViewById(R.id.player_title)
        recyclerView = findViewById(R.id.player_recycle)
        layoutManager = LinearLayoutManager(this)

        launchApplication(titleText)

        fullscreenButton.setOnClickListener {
            fullScreenClick()
        }

        recyclerView.layoutManager = layoutManager
        PositioN = intent.getIntExtra("VIDEO_POS", 0)
        adapter = PlayerAdapter(!intent.getBooleanExtra("DOWNLOADED",false),LEARN,PositioN,them,
            title, thumb, link, position, this, object : ClickInterface {
                override fun Click(link: String, pos: Int, task: String) {
                    PositioN = pos
                    LINK = link
                    Log.println(Log.ASSERT,"LINK",LINK)
                    Log.println(Log.ASSERT,"TOUCH ADAPTER","TRUE")
                    when (task) {
                        "PLAY_OFFLINE" -> {
                            uri = Uri.parse(link)
                            player!!.release()
                            initializePlayer()
                            player!!.seekTo(0)
                            smoothScroller.targetPosition = pos
                            layoutManager.startSmoothScroll(smoothScroller)
                        }
                        "DOWNLOAD" -> {
                            if (ContextCompat.checkSelfPermission(this@PlayerActivity,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                                ActivityCompat.requestPermissions(this@PlayerActivity,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS)
                            else
                                extract("DOWNLOAD")
                        }
                        else ->
                            playOnline(pos)
                    }
                }
            })

        smoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        adapter.notifyDataSetChanged()
        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }
    }

    private fun playOnline(pos: Int) {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
            mInterstitialAd.adListener = object: AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    // Code to be executed when an ad request fails.
                }

                override fun onAdOpened() {
                    // Code to be executed when the ad is displayed.
                }

                override fun onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                override fun onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                override fun onAdClosed() {
                    smoothScroller.targetPosition = pos
                    layoutManager.startSmoothScroll(smoothScroller)
                    try {
                        player!!.release()
                    } catch (e: Exception) {
                        Log.println(Log.ASSERT,tag, e.toString())
                    }
                    extract("PLAY")
                }
            }
        } else {
            smoothScroller.targetPosition = pos
            layoutManager.startSmoothScroll(smoothScroller)
            try {
                player!!.release()
            } catch (e: Exception) {
                Log.println(Log.ASSERT,tag, e.toString())
            }
            extract("PLAY")
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }
    }

    private fun initializePlayer() {
        releasePlayer()
        try {
            Log.println(Log.ASSERT, "position", position[PositioN].toString())
            LearningData(this).setSession(LEARN, position[PositioN])
        } catch (e: Exception) {
            Log.println(Log.ASSERT,tag, e.toString())
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        player = ExoPlayerFactory.newSimpleInstance(this)
        playerView!!.player = player
        POS.text = (PositioN + 1).toString() + "/" + loop
        Log.println(Log.ASSERT,"POS","${PositioN+1}")
        val mediaSource = buildMediaSource(uri)
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare(mediaSource, false, false)
        player?.addListener(object :Player.EventListener{

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playingChanged(isPlaying)
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                error?.let {}
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                if (!isLoading && first) {
                    first = false
                    try {
                        layoutManager.startSmoothScroll(smoothScroller)
                    }catch (e: Exception){
                        Log.println(Log.ASSERT,tag, e.toString())
                    }
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_ENDED)
                    playNext(PositioN++)
                if (fullscreen) {
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                if (playerView?.visibility != View.VISIBLE) {
                    findViewById<ProgressBar>(R.id.loading_prog).visibility = View.GONE
                    playerView?.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun playingChanged(isPlaying: Boolean) {
        Playing = isPlaying
        if (isPlaying)
            playedMiliSec = System.currentTimeMillis().toInt()
        else
            playedSec += (System.currentTimeMillis().toInt()-playedMiliSec)
        when(LEARN) {
            "C" -> { learningData.setPosition("C", PositioN.toString()) }
            "C_P" -> { learningData.setPosition("C_P", PositioN.toString()) }
            "J" -> { learningData.setPosition("J", PositioN.toString()) }
            "P" -> { learningData.setPosition("P", PositioN.toString()) }
            "K" -> { learningData.setPosition("K", PositioN.toString()) }
        }
        Log.println(Log.ASSERT,"Total : ", playedSec.toString())
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(this, "exoplayer-codelab")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun fullScreenClick() {
        if (fullscreen) {
            fullscreenButton.background = ContextCompat.getDrawable(
                this, R.drawable.ic_fullscreen_black_24dp)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            if (supportActionBar != null) {
                supportActionBar!!.show()
            }
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val params: FrameLayout.LayoutParams =
                video_view.layoutParams as FrameLayout.LayoutParams
            params.width = FrameLayout.LayoutParams.MATCH_PARENT
            params.height = FrameLayout.LayoutParams.WRAP_CONTENT
            video_view.layoutParams = params
            fullscreen = false
        } else {
            fullscreenButton.background = ContextCompat.getDrawable(
                this, R.drawable.ic_fullscreen_exit_black_24dp)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            if (supportActionBar != null) {
                supportActionBar!!.hide()
            }
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val params: FrameLayout.LayoutParams =
                video_view.layoutParams as FrameLayout.LayoutParams

            params.width = FrameLayout.LayoutParams.MATCH_PARENT
            params.height = FrameLayout.LayoutParams.MATCH_PARENT
            video_view.layoutParams = params
            fullscreen = true
        }
    }

    public override fun onStart() {
        super.onStart()
        run = true
    }

    public override fun onResume() {
        super.onResume()
        if (player == null && !first)
            initializePlayer()
    }

    public override fun onPause() {
        super.onPause()
        releasePlayer()
        if (Playing)
            playedSec += (System.currentTimeMillis().toInt()-playedMiliSec)
        when(LEARN) {
            "C" -> {
                learningData.setPosition("C",PositioN.toString())
                learningData.setWatchTime("C",learningData.getWatchTime("C")+playedSec)
            }
            "C_P" -> {
                learningData.setPosition("C_P",PositioN.toString())
                learningData.setWatchTime("C_P",learningData.getWatchTime("C_P")+playedSec)
            }
            "J" -> {
                learningData.setPosition("J",PositioN.toString())
                learningData.setWatchTime("J",learningData.getWatchTime("J")+playedSec)
            }
            "P" -> {
                learningData.setPosition("P",PositioN.toString())
                learningData.setWatchTime("P",learningData.getWatchTime("P")+playedSec)
            }
            "K" -> {
                learningData.setPosition("K",PositioN.toString())
                learningData.setWatchTime("K",learningData.getWatchTime("K")+playedSec)
            }
        }
        Playing = false
        playedSec = 0
    }

    public override fun onStop() {
        if (Util.SDK_INT >= 24)
            releasePlayer()
        run = false
        super.onStop()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) { extract("DOWNLOAD") }
                else { Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show() }
                return
            }
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player!!.playWhenReady
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }

    @SuppressLint("CheckResult")
    private fun  extract(task : String) {
        val extractor = YouTubeExtractor.Builder()
            .build()
        extractor.extract(LINK)
            .subscribeOn(Schedulers.io())
            .subscribe({ extraction ->
                runOnUiThread {
                    val url = if (Build.VERSION.SDK_INT < 23) extraction.streams.filterIsInstance<Stream.VideoStream>().get(0).url
                    else extraction.streams.filterIsInstance<Stream.VideoStream>().get(1).url

                    if (task == "DOWNLOAD") {
                        val name = when {
                            PositioN < 10 -> "00$PositioN"
                            PositioN < 100 -> "0$PositioN"
                            else -> "$PositioN"
                        }
                        Thread(Runnable { downloadFiles(url, "$name. " + title[PositioN]) }).start()
                    } else if (run) {
                        uri = Uri.parse(url)
                        initializePlayer()
                        player!!.seekTo(0)
                    }
                }
            }, { t ->
                onError(t)
            })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Downloader"
            val descriptionText = "This notification channel will be used to show the downloading progress"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("DOWNLOAD", name, importance)
                .apply { description = descriptionText }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("SdCardPath")
    private fun downloadFiles(u: String, name :String){
        NOTIIFCATION_ID++
        val ID = NOTIIFCATION_ID
        createNotificationChannel()
        val POSI = PositioN.toString()
        val offlineData = OfflineData(this)
        val builder = NotificationCompat.Builder(this, "DOWNLOAD").apply {
            setContentTitle(title[PositioN])
            setContentText("Download in progress")
            setOngoing(true)
            setSmallIcon(R.drawable.file_download_white)
            priority = NotificationCompat.PRIORITY_LOW
        }

        NotificationManagerCompat.from(this).apply {
            builder.setProgress(100, 0, true)
            notify(ID, builder.build())
            var input: InputStream? = null
            var output: OutputStream? = null
            var connection: HttpURLConnection? = null
            try {
                val url = URL(u)
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val fileLength: Int = connection.contentLength
                Log.println(Log.ASSERT, "SIZE", "file_size = $fileLength")
                input = connection.inputStream
                val file = File("/sdcard/Android/data/com.alim.letscode/$LEARN/")
                if (!file.exists()) file.mkdirs()
                output = FileOutputStream("/sdcard/Android/data/com.alim.letscode/$LEARN/$name.code")
                val data = ByteArray(4096)
                var total: Long = 0
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
                    output.write(data, 0, count)
                }
                builder.setSmallIcon(R.drawable.check_circle_white)
                builder.setContentText("Download complete")
                    .setProgress(0, 0, false)
                builder.setOngoing(false)
                notify(ID, builder.build())
                Log.println(Log.ASSERT,"POSITION", POSI)
                offlineData.setOffline(LEARN,POSI,"/sdcard/Android/data/com.alim.letscode/$LEARN/$name.code",true)
                runOnUiThread { adapter.notifyDataSetChanged() }
            } catch (e: Exception) {
                builder.setSmallIcon(R.drawable.ic_error_white_24dp)
                builder.setContentText("Download failed")
                    .setProgress(0, 0, false)
                builder.setOngoing(false)
                notify(ID, builder.build())
                Log.println(Log.ASSERT,"ERROR", e.toString())
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

    private fun playNext(pos: Int) {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
            mInterstitialAd.adListener = object: AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    // Code to be executed when an ad request fails.
                }

                override fun onAdOpened() {
                    // Code to be executed when the ad is displayed.
                }

                override fun onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                override fun onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                override fun onAdClosed() {
                    play(pos)
                }
            }
        } else {
            play(pos)
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }
    }

    private fun play(pos: Int) {
        PositioN = pos
        if (PositioN < link.size) {
            if (PositioN > -1)
                LINK = link[PositioN]
            else
                PositioN = 0
            if (intent.getBooleanExtra("DOWNLOADED", false)) {
                uri = Uri.parse(LINK)
                player!!.release()
                initializePlayer()
                player!!.seekTo(0)
            } else {
                startPlay()
            }
            GlobalVariable().setPosition(PositioN)
            adapter.notifyDataSetChanged()
            smoothScroller.targetPosition = PositioN
            layoutManager.startSmoothScroll(smoothScroller)
        } else {
            Toast.makeText(this,"End of the course !",Toast.LENGTH_LONG).show()
            if (fullscreen) fullscreenButton.callOnClick()
        }
    }

    private fun startPlay() {
        try {
            player!!.release()
        } catch (e: Exception) {
            Log.println(Log.ASSERT,tag,e.toString())
        }
        if (offlineData.getOffline(LEARN, PositioN.toString())) {
            uri = Uri.parse(offlineData.getOfflineL(LEARN, PositioN.toString()))
            initializePlayer()
            player!!.seekTo(0)
        } else
            extract("PLAY")
    }

    override fun onBackPressed() {
        if (fullscreen)
            fullscreenButton.callOnClick()
        else
            super.onBackPressed()
    }
}