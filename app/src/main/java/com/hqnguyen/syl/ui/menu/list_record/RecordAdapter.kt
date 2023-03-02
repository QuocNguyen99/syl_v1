package com.hqnguyen.syl.ui.menu.list_record

import android.annotation.SuppressLint
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hqnguyen.syl.data.ListLocation
import com.hqnguyen.syl.databinding.ItemRecordBinding
import com.hqnguyen.syl.utils.getDate
import com.mapbox.geojson.Point
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class RecordAdapter : ListAdapter<ListLocation, RecordAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindData(record: ListLocation) {
            binding.tvDate.text = record.startTime.getDate()
            binding.tvTotalKM.text = String.format("%.2f", (record.distance * 0.001)) + "Km"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }
}

class DiffCallback : DiffUtil.ItemCallback<ListLocation>() {
    override fun areItemsTheSame(oldItem: ListLocation, newItem: ListLocation): Boolean {
        return oldItem.startTime == newItem.startTime
    }

    override fun areContentsTheSame(oldItem: ListLocation, newItem: ListLocation): Boolean {
        return oldItem == newItem

    }
}