package com.remys.audiotest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class PlaylistAdapter(private val files: ArrayList<File>?, val listener: OnAudioFileListener):
    RecyclerView.Adapter<PlaylistAdapter.Viewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return Viewholder(itemView);
    }

    override fun getItemCount(): Int = files?.size ?: 0

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.recordTitle.text = files?.get(position)?.name ?: ""

        holder.playButton.setOnClickListener {
            listener.playAudioFile(files?.get(position), holder.playButton)
        }
    }

    class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recordTitle: TextView = itemView.findViewById(R.id.record_title)
        val playButton: ImageView = itemView.findViewById(R.id.play_button)
    }
}