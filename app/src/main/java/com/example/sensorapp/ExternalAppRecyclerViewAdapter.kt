package com.example.sensorapp

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.DrawableCompat

import com.example.sensorapp.placeholder.PlaceholderContent.PlaceholderItem

/**
 * AppRecyclerAdapter that can display an installed (external) app.
 * TODO: Replace the implementation with code for your data type.
 */
class ExternalAppRecyclerViewAdapter(
        private val values: List<PlaceholderItem>)
    : RecyclerView.Adapter<ExternalAppRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_ext_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.iconView.setImageResource(R.drawable.canvas_icon)
        holder.nameView.text = item.content
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.app_icon)
        val nameView: TextView = view.findViewById(R.id.app_name)


        override fun toString(): String {
            return super.toString() + " '" + nameView.text + "'"
        }
    }
}