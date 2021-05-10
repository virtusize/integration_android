package com.virtusize.android.designsystem

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.virtusize.android.databinding.DesignSystemRowItemBinding

class DesignSystemAdapter(private val dataSet: Array<String>): RecyclerView.Adapter<DesignSystemAdapter.ViewHolder>() {
    class ViewHolder(binding: DesignSystemRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val textView: TextView = binding.textView
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DesignSystemRowItemBinding.inflate(LayoutInflater.from(viewGroup.context)))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet[position]
    }

    override fun getItemCount() = dataSet.size

}