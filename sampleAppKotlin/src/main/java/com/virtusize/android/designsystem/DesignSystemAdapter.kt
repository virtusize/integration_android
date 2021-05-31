package com.virtusize.android.designsystem

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.virtusize.android.R
import com.virtusize.android.databinding.DesignSystemRowItemBinding

class DesignSystemAdapter(private val context: Context, private val dataSet: Array<String>) :
    RecyclerView.Adapter<DesignSystemAdapter.ViewHolder>() {
    class ViewHolder(binding: DesignSystemRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val textView: TextView = binding.textView
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DesignSystemRowItemBinding.inflate(LayoutInflater.from(viewGroup.context)))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet[position]

        viewHolder.itemView.setOnClickListener { view ->
            if (dataSet[position] == context?.getString(R.string.virtusize_button)) {
                view.findNavController().navigate(R.id.action_designSystemFragment_to_buttonFragment)
            }
        }
    }

    override fun getItemCount() = dataSet.size

}