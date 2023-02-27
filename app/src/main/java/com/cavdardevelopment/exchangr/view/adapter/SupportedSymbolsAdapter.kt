package com.cavdardevelopment.exchangr.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cavdardevelopment.exchangr.databinding.SupportedSymbolsRowBinding
import com.cavdardevelopment.exchangr.model.response.SupportedSymbolsResponse

class SupportedSymbolsAdapter(private val response: SupportedSymbolsResponse) : RecyclerView.Adapter<SupportedSymbolsAdapter.SupportedSymbolsViewHolder>() {

    class SupportedSymbolsViewHolder(val binding: SupportedSymbolsRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupportedSymbolsViewHolder =
        SupportedSymbolsViewHolder(SupportedSymbolsRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = this.response.codes.size

    override fun onBindViewHolder(holder: SupportedSymbolsViewHolder, position: Int) {
        holder.binding.codeTextView.text = response.codes[position]
        holder.binding.descriptionTextView.text = response.descriptions[position]
    }
}