package com.remys.audiotest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PlaylistAdapter(private val record: List<String>?): RecyclerView.Adapter<PlaylistAdapter.Viewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return Viewholder(itemView);
    }

    override fun getItemCount(): Int = record?.size ?: 0

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
    }

    class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}