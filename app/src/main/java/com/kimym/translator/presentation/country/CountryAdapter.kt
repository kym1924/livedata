package com.kimym.translator.presentation.country

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kimym.translator.BR
import com.kimym.translator.data.entity.Country
import com.kimym.translator.databinding.ItemCountryBinding

class CountryAdapter(private val itemClick: (country: Country) -> Unit) :
    ListAdapter<Country, CountryAdapter.CountryViewHolder>(countryDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCountryBinding.inflate(inflater, parent, false)
        return CountryViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CountryViewHolder(
        private val binding: ItemCountryBinding,
        private val itemClick: (country: Country) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(country: Country) {
            with(binding) {
                setVariable(BR.country, country)
                executePendingBindings()
                root.setOnClickListener {
                    itemClick(country)
                }
            }
        }
    }

    companion object {
        private val countryDiffUtil = object : DiffUtil.ItemCallback<Country>() {
            override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
                return oldItem == newItem
            }
        }
    }
}
