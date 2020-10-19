package com.example.sensorapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * AppRecyclerAdapter that can display an installed (external) app.
 * TODO: Replace the implementation with code for your data type.
 */
class ExternalAppRecyclerViewAdapter(
    private val appClickListener: ExternalAppListener
)
    : ListAdapter<ExternalApp, ExternalAppRecyclerViewAdapter.ViewHolder>(ExternalAppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position, appClickListener)
    }

    class ViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val iconView: ImageView = view.findViewById(R.id.app_icon)
        private val nameView: TextView = view.findViewById(R.id.app_name)
        private var selectedPosition = RecyclerView.NO_POSITION

        fun bind(item: ExternalApp, position: Int, appClickListener: ExternalAppListener) {
            iconView.setImageDrawable(item.icon)
            nameView.text = item.label
            itemView.isSelected = selectedPosition == position
            itemView.setOnClickListener {
                appClickListener.onClick(item)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + nameView.text + "'"
        }

        override fun onClick(v: View?) {
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            selectedPosition = adapterPosition
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.fragment_ext_app_list_item, parent, false)
                return ViewHolder(view)
            }
        }
    }

    class ExternalAppListener(val clickListener: (app: ExternalApp) -> Unit) {
        fun onClick(app: ExternalApp) = clickListener(app)
    }

    class ExternalAppDiffCallback : DiffUtil.ItemCallback<ExternalApp>() {
        override fun areItemsTheSame(oldItem: ExternalApp, newItem: ExternalApp): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: ExternalApp, newItem: ExternalApp): Boolean {
            return oldItem == newItem
        }
    }
}