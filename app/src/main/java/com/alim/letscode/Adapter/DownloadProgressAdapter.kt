package com.alim.letscode.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alim.letscode.R
import com.bumptech.glide.Glide

class DownloadProgressAdapter(
    title: List<String>,
    thumb: List<String>,
    context: Context) : RecyclerView.Adapter<DownloadProgressAdapter.ViewHolder>(){

    private val mContext: Context = context
    private val mTitle: List<String> = title
    private val mThumb: List<String> = thumb

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val title: TextView = view.findViewById(R.id.v_title)
        val thumb: ImageView = view.findViewById(R.id.thumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.offline_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadProgressAdapter.ViewHolder, position: Int) {
        holder.title.text = mTitle[position]
        Glide.with(mContext).load(mThumb[position]).centerCrop().into(holder.thumb)
    }

    override fun getItemCount(): Int {
        return mTitle.size
    }
}