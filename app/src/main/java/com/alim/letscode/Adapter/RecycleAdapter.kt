package com.alim.letscode.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.alim.letscode.Class.NetWork
import com.alim.letscode.Database.MainDB
import com.alim.letscode.Database.OfflineData
import com.alim.letscode.PlayerActivity
import com.alim.letscode.R
import com.bumptech.glide.Glide

class RecycleAdapter(
    private val pos: Int,
    private val f: String,
    context: Context) : RecyclerView.Adapter<RecycleAdapter.SimpleViewHolder>() {

    private val mContext = context
    private val mainDB = MainDB(mContext)
    private val offlineData = OfflineData(mContext)

    inner class SimpleViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val thumb: ImageView = view.findViewById(R.id.thumb)
        val layout: LinearLayout = view.findViewById(R.id.video_layout)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): SimpleViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.video_thumb, parent, false)
        return SimpleViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SimpleViewHolder,
        position: Int
    ) {
        holder.title.text = mainDB.getTitle(f, position)
        Glide.with(mContext).load(mainDB.getThumb(f, position)).centerCrop().into(holder.thumb)
        holder.layout.setOnClickListener {

            if (NetWork().isConnected(mContext)) {
                val intent = Intent(holder.itemView.context, PlayerActivity::class.java)
                intent.putExtra("VIDEO_POS", position)
                intent.putExtra("MY_LOCATION", f)
                if (offlineData.getOffline(f, position.toString())) {
                    intent.putExtra("VIDEO_ID", offlineData.getOfflineL(f, position.toString()))
                    intent.putExtra("SINGLE_DOWNLOAD", true)
                } else
                    intent.putExtra("VIDEO_ID", mainDB.getLink(f, position))
                holder.itemView.context.startActivity(intent)
            } else
                Toast.makeText(mContext,"No Internet",Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return pos
    }
}