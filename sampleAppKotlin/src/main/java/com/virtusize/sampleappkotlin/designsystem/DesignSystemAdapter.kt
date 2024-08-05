package com.virtusize.sampleappkotlin.designsystem

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.virtusize.sampleappkotlin.R
import com.virtusize.sampleappkotlin.databinding.DesignSystemRowItemBinding

class DesignSystemAdapter(private val context: Context, private val dataSet: Array<String>) :
    RecyclerView.Adapter<DesignSystemAdapter.ViewHolder>() {
    private val onClickMap: Map<String, Int> =
        mutableMapOf(
            context.getString(R.string.virtusize_button) to R.id.action_designSystemFragment_to_buttonFragment,
            context.getString(R.string.virtusize_tooltip) to R.id.action_designSystemFragment_to_tooltipFragment,
        )

    class ViewHolder(binding: DesignSystemRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val textView: TextView = binding.textView
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(DesignSystemRowItemBinding.inflate(LayoutInflater.from(viewGroup.context)))
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int,
    ) {
        viewHolder.textView.text = dataSet[position]

        viewHolder.itemView.setOnClickListener { view ->
            onClickMap[dataSet[position]]?.let {
                view.findNavController().navigate(it)
            }
        }
    }

    override fun getItemCount() = dataSet.size
}
