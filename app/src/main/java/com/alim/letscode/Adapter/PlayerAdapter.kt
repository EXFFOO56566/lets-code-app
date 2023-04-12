package com.alim.letscode.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alim.letscode.Class.GlobalVariable
import com.alim.letscode.Database.OfflineData
import com.alim.letscode.Interface.ClickInterface
import com.alim.letscode.R
import com.bumptech.glide.Glide

class PlayerAdapter(
    oNline: Boolean,
    learn: String,
    pos: Int,
    mThem: Int,
    title: List<String>,
    thumb: List<String>,
    link: List<String>,
    position: List<Int>,
    context: Context,
    mClick: ClickInterface) : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

    private var lastPosition = -1
    private var online = oNline
    private var them = 0
    private var mLearn: String
    private val mContext: Context
    private val click: ClickInterface
    private val mTitle: List<String>
    private val mThumb: List<String>
    private val mLink: List<String>
    private val mPosition: List<Int>
    private val offlineData: OfflineData
    private val globalVariable = GlobalVariable()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.v_title)
        val thumb: ImageView = view.findViewById(R.id.thumbnail)
        val cardView: CardView = view.findViewById(R.id.card)
        val download: ImageView = view.findViewById(R.id.download)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.player_card, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mThumb.isNotEmpty()) {
            holder.title.text = (position+1).toString()+". "+mTitle[position]
            Glide.with(mContext).load(mThumb[position]).centerCrop().into(holder.thumb)
        } else {
            val id = mTitle[position].substring(0,3).toInt()+1
            holder.title.text = id.toString()+mTitle[position].substring(3)
            Glide.with(mContext).load(mLink[position]).centerCrop().into(holder.thumb)
        }

        holder.download.setOnClickListener {
            if (online and !offlineData.getOffline(mLearn, position.toString())) {
                click.Click(mLink[position], position, "DOWNLOAD")
                holder.download.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.in_progress_black)))
                if (globalVariable.getPosition() == position)
                    holder.download.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.in_progress_white)))
            }
        }

        holder.cardView.setOnClickListener {
            if (globalVariable.getPosition()!=position) {
                globalVariable.setPosition(position)
                notifyDataSetChanged()
                if (offlineData.getOffline(mLearn, mPosition[position].toString()))
                    click.Click(offlineData.getOfflineL(mLearn, mPosition[position].toString()), position,"PLAY_OFFLINE")
                else
                    click.Click(mLink[position], position,"PLAY_ONLINE")
                Log.println(Log.ASSERT,"OFF_LINK",offlineData.getOfflineL(mLearn, position.toString()))
            }
        }
        if (globalVariable.getPosition() == position)
            setSelectedImages(holder, position)
        else
            setNonSelectedImages(holder, position)
        setAnimation(holder.cardView, position)
    }

    override fun getItemCount(): Int {
        return mTitle.size
    }

    private fun setSelectedImages(holder: ViewHolder, position: Int) {
        if (globalVariable.getPosition() == position) {
            if (them == 0)
                holder.cardView.setCardBackgroundColor(Color.parseColor("#E82040"))
            else
                holder.cardView.setCardBackgroundColor(Color.parseColor("#1e6afe"))
            holder.title.setTextColor(Color.parseColor("#ffffff"))
            if (offlineData.getOffline(mLearn, mPosition[position].toString()))
                holder.download.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.check_circle_white)))
            else
                holder.download.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.file_download_white)))
        }
    }

    private fun setNonSelectedImages(holder: ViewHolder, position: Int) {
        if (them == 0) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#ffffff"))
            holder.title.setTextColor(Color.parseColor("#000000"))
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#4E4E4E"))
            holder.title.setTextColor(Color.parseColor("#FFFFFF"))
        }
        if (them == 0) {
            if (offlineData.getOffline(mLearn, mPosition[position].toString()))
                holder.download.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.check_circle_black)))
            else
                holder.download.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.file_download_black)))
        } else {
            if (offlineData.getOffline(mLearn, mPosition[position].toString()))
                holder.download.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.check_circle_white)))
            else
                holder.download.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.file_download_white)))
        }
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation: Animation = AnimationUtils.loadAnimation(
                mContext, R.anim.fade_in)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    init {
        them = mThem
        globalVariable.setPosition(pos)
        mTitle = title
        mLearn = learn
        mThumb = thumb
        mLink = link
        mPosition = position
        click = mClick
        mContext = context
        offlineData  = OfflineData(mContext)
    }
}