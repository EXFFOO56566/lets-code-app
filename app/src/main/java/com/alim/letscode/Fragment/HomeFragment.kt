package com.alim.letscode.Fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Base64.decode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.letscode.Adapter.RecycleAdapter
import com.alim.letscode.Database.ApplicationData
import com.alim.letscode.Database.LearningData
import com.alim.letscode.PlayerActivity
import com.alim.letscode.R
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.alim.letscode.Class.NetWork
import com.alim.letscode.Database.MainDB
import com.alim.letscode.Database.OfflineData
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class HomeFragment : Fragment() {

    private lateinit var mainDB: MainDB
    private val storagePermissionCode = 105
    private lateinit var chooser : Intent
    private lateinit var offlineData: OfflineData
    private val actionRequestGalleryCode = 102
    private lateinit var applicationData: ApplicationData
    private lateinit var profile: ImageView

    //C Coding Part
    private lateinit var adapterC: RecycleAdapter
    private lateinit var recyclerViewC: RecyclerView
    private lateinit var layoutManagerC: RecyclerView.LayoutManager

    //C++ Coding Part
    private lateinit var adapterCP: RecycleAdapter
    private lateinit var recyclerviewCP: RecyclerView
    private lateinit var layoutManagerCP: RecyclerView.LayoutManager

    //JAVA Coding Part
    private lateinit var adapterJ: RecycleAdapter
    private lateinit var recyclerViewJ: RecyclerView
    private lateinit var layoutManagerJ: RecyclerView.LayoutManager

    //PYTHON Coding Part
    private lateinit var adapterP: RecycleAdapter
    private lateinit var recyclerViewP: RecyclerView
    private lateinit var layoutManagerP: RecyclerView.LayoutManager

    //KOTLIN Coding Part
    private lateinit var adapterK: RecycleAdapter
    private lateinit var recyclerViewK: RecyclerView
    private lateinit var layoutManagerK: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        MobileAds.initialize(activity!!)
        val template = rootView.findViewById<TemplateView>(R.id.my_template)
        val template2 = rootView.findViewById<TemplateView>(R.id.my_template2)
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

        val adLoader2 = AdLoader.Builder(activity!!, resources.getString(R.string.native_ad_id))
            .forUnifiedNativeAd {
                template2.setStyles(styles)
                template2.setNativeAd(it)
            }.withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    template2.visibility = View.VISIBLE
                }
            })
            .build()
        Handler().postDelayed({
            adLoader.loadAd(AdRequest.Builder().build())
            adLoader2.loadAd(AdRequest.Builder().build())
        },1000)

        applicationData = ApplicationData(activity!!)
        mainDB = MainDB(activity!!)
        offlineData = OfflineData(activity!!)
        val name = rootView.findViewById<TextView>(R.id.name)
        name.text = applicationData.name

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        chooser = Intent.createChooser(intent, "Choose a Picture")

        profile = rootView.findViewById(R.id.profile)

        Handler().postDelayed({
            if (applicationData.image.isNotEmpty())
                profile.setImageBitmap(decodeBase64(applicationData.image))
        },300)

        profile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), storagePermissionCode)
            else
                startActivityForResult(chooser, actionRequestGalleryCode)
        }

        //Part C
        var codePosC = LearningData(activity!!).getSession("C")
        var codePosCP = LearningData(activity!!).getSession("C_P")
        var codePosJ = LearningData(activity!!).getSession("J")
        var codePosP = LearningData(activity!!).getSession("P")
        var codePosK = LearningData(activity!!).getSession("K")

        val cContinue = rootView.findViewById<Button>(R.id.c_contin)
        val cpContinue = rootView.findViewById<Button>(R.id.c_p_contin)
        val jContinue = rootView.findViewById<Button>(R.id.j_contin)
        val pContinue = rootView.findViewById<Button>(R.id.p_contin)
        val kContinue = rootView.findViewById<Button>(R.id.k_contin)

        if (codePosC > -1)
            cContinue.text = resources.getString(R.string._continue)
        if (codePosCP > -1)
            cpContinue.text = resources.getString(R.string._continue)
        if (codePosJ > -1)
            jContinue.text = resources.getString(R.string._continue)
        if (codePosP > -1)
            pContinue.text = resources.getString(R.string._continue)
        if (codePosK > -1)
            kContinue.text = resources.getString(R.string._continue)


        cContinue.setOnClickListener {
            codePosC = LearningData(activity!!).getSession("C")
            startContinue(codePosC, "C")
            Handler().postDelayed({
                cContinue.text = resources.getString(R.string._continue)
            },1000)
        }

        cpContinue.setOnClickListener {
            codePosCP = LearningData(activity!!).getSession("C_P")
            startContinue(codePosCP, "C_P")
            Handler().postDelayed({
                cpContinue.text = resources.getString(R.string._continue)
            },1000)
        }

        jContinue.setOnClickListener {
            codePosJ = LearningData(activity!!).getSession("J")
            startContinue(codePosJ, "J")
            Handler().postDelayed({
                jContinue.text = resources.getString(R.string._continue)
            },1000)
        }

        pContinue.setOnClickListener {
            codePosP = LearningData(activity!!).getSession("P")
            startContinue(codePosP, "P")
            Handler().postDelayed({
                pContinue.text = resources.getString(R.string._continue)
            },1000)
        }

        kContinue.setOnClickListener {
            codePosK = LearningData(activity!!).getSession("K")
            startContinue(codePosK, "K")
            Handler().postDelayed({
                kContinue.text = resources.getString(R.string._continue)
            },1000)
        }

        recyclerViewC = rootView.findViewById(R.id.recycle_c)
        layoutManagerC = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        recyclerViewC.layoutManager = layoutManagerC
        adapterC = RecycleAdapter(176,"C", activity!!)
        recyclerViewC.adapter = adapterC
        recyclerViewC.itemAnimator = DefaultItemAnimator()
        adapterC.notifyDataSetChanged()

        //Part C++
        recyclerviewCP = rootView.findViewById(R.id.recycle_c_p)
        layoutManagerCP = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        recyclerviewCP.layoutManager = layoutManagerCP
        adapterCP = RecycleAdapter(149,"C_P", activity!!)
        recyclerviewCP.adapter = adapterCP
        recyclerviewCP.itemAnimator = DefaultItemAnimator()
        adapterCP.notifyDataSetChanged()

        //Part JAVA
        recyclerViewJ = rootView.findViewById(R.id.recycle_j)
        layoutManagerJ = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        recyclerViewJ.layoutManager = layoutManagerJ
        adapterJ = RecycleAdapter(92, "J", activity!!)
        recyclerViewJ.adapter = adapterJ
        recyclerViewJ.itemAnimator = DefaultItemAnimator()
        adapterJ.notifyDataSetChanged()

        //Part PYTHON
        recyclerViewP = rootView.findViewById(R.id.recycle_p)
        layoutManagerP = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        recyclerViewP.layoutManager = layoutManagerP
        adapterP = RecycleAdapter(110, "P", activity!!)
        recyclerViewP.adapter = adapterP
        recyclerViewP.itemAnimator = DefaultItemAnimator()
        adapterP.notifyDataSetChanged()

        //Part KOTLIN
        recyclerViewK = rootView.findViewById(R.id.recycle_k)
        layoutManagerK = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        recyclerViewK.layoutManager = layoutManagerK
        adapterK = RecycleAdapter(47, "K", activity!!)
        recyclerViewK.adapter = adapterK
        recyclerViewK.itemAnimator = DefaultItemAnimator()
        adapterK.notifyDataSetChanged()

        return rootView
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            storagePermissionCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    startActivityForResult(chooser, actionRequestGalleryCode)
                else Toast.makeText(activity!!,"Permission Denied", Toast.LENGTH_LONG).show()
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                actionRequestGalleryCode -> {
                    val galleryImageUri: Uri? = data?.data
                    try {
                        profile.setImageURI(galleryImageUri)
                        encodeTobase64(profile.drawToBitmap())
                    } catch (ex: Exception) {
                        Log.println(Log.ASSERT,"Exception",ex.toString())
                    }
                }
            }
        }
    }

    private fun decodeBase64(input: String?): Bitmap? {
        val decodedByte: ByteArray =decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    private fun encodeTobase64(image: Bitmap) {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
        applicationData.image = imageEncoded
    }

    private fun startContinue(codePos: Int, lan: String) {
        val playerIntent = Intent(activity, PlayerActivity::class.java)
        if (codePos > -1) {
            if (offlineData.getOffline(lan, codePos.toString())) {
                playerIntent.putExtra("SINGLE_DOWNLOAD", true)
                playerIntent.putExtra("VIDEO_ID", offlineData.getOfflineL(lan, codePos.toString()))
            } else
                playerIntent.putExtra("VIDEO_ID", mainDB.getLink(lan, codePos))
            playerIntent.putExtra("VIDEO_POS", codePos)
            playerIntent.putExtra("MY_LOCATION", lan)
        } else {
            if (offlineData.getOffline(lan, "0")) {
                playerIntent.putExtra("SINGLE_DOWNLOAD", true)
                playerIntent.putExtra("VIDEO_ID", offlineData.getOfflineL(lan, "0"))
            } else
                playerIntent.putExtra("VIDEO_ID",  mainDB.getLink(lan,0))
            playerIntent.putExtra("VIDEO_POS", 0)
            playerIntent.putExtra("MY_LOCATION", lan)
        }
        if (NetWork().isConnected(activity!!))
            startActivity(playerIntent)
        else
            Toast.makeText(activity!!,"No Internet Connection", Toast.LENGTH_SHORT).show()
    }
}