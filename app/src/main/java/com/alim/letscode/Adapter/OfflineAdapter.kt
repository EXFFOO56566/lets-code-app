package com.alim.letscode.Adapter

import android.annotation.SuppressLint
import android.content.Context
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
import com.alim.letscode.Interface.ClickInterface
import com.alim.letscode.R
import com.bumptech.glide.Glide
import java.lang.Exception

class OfflineAdapter(
    dark: Boolean,
    title: List<String>,
    link: List<String>,
    context: Context,
    click: ClickInterface) : RecyclerView.Adapter<OfflineAdapter.ViewHolder>(){

    private var lastPosition = -1
    private var night = dark
    private val mContext: Context = context
    private val mTitle: List<String> = title
    private val mLink: List<String> = link
    private val mClick: ClickInterface = click

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val title: TextView = view.findViewById(R.id.v_title)
        val thumb: ImageView = view.findViewById(R.id.thumbnail)
        val cardView: CardView = view.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.offline_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OfflineAdapter.ViewHolder, position: Int) {

        val id = mTitle[position].substring(0,3).toInt()+1

        holder.title.text = id.toString()+mTitle[position].substring(3)

        try {
            if (GlobalVariable().getSelectedSingle(position)) {
                if (night)
                    holder.thumb.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.check_circle_white)))
                else
                    holder.thumb.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.check_circle_black)))
            } else
                Glide.with(mContext).load(mLink[position]).centerCrop().into(holder.thumb)
        } catch (e: Exception) {
            Glide.with(mContext).load(mLink[position]).centerCrop().into(holder.thumb)
        }

        holder.cardView.setOnLongClickListener {
            mClick.Click(mLink[position],position,"LONG_CLICK")
            if (!GlobalVariable.choice) {
                GlobalVariable.choice = true
                GlobalVariable().setSelectedSingle(position, true)
                if (night)
                    holder.thumb.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.check_circle_white)))
                else
                    holder.thumb.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.check_circle_black)))
            }
            true
        }

        holder.cardView.setOnClickListener {
            click(holder, position)
        }
        if (GlobalVariable.choice)
            setAnimation(holder.cardView, position)
    }

    override fun getItemCount(): Int {
        return mTitle.size
    }

    private fun click(holder: OfflineAdapter.ViewHolder, position: Int) {
        if (GlobalVariable.choice) {
            if (GlobalVariable().getSelectedSingle(position)) {
                GlobalVariable().setSelectedSingle(position, false)
                Glide.with(mContext).load(mLink[position]).centerCrop().into(holder.thumb)
            } else {
                GlobalVariable().setSelectedSingle(position, true)
                if (night)
                    holder.thumb.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.check_circle_white)))
                else
                    holder.thumb.setImageDrawable(ContextCompat.getDrawable(mContext, (R.drawable.check_circle_black)))
            }
        } else
            mClick.Click(mLink[position], position,"PLAY")
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
            val animation: Animation = AnimationUtils.loadAnimation(
                mContext,
                android.R.anim.fade_in)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
    }
}