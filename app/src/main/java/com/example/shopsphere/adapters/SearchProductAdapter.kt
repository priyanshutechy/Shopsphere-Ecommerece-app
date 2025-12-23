package com.example.shopsphere.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopsphere.data.CartProduct
import com.example.shopsphere.data.Product
import com.example.shopsphere.databinding.SearchProductItemBinding

class SearchProductAdapter : RecyclerView.Adapter<SearchProductAdapter.SearchProductViewHolder>() {
    inner class SearchProductViewHolder(val binding: SearchProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(searchProduct: Product) {
            binding.apply {
                Glide.with(itemView).load(searchProduct.images[0]).into(imgageSpecialRvItem)
                tvSpecialProductName.text = searchProduct.name
            }
        }

    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchProductViewHolder {
        return SearchProductViewHolder(
            SearchProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SearchProductViewHolder, position: Int) {
        val searchProduct = differ.currentList[position]
        holder.bind(searchProduct)

        holder.itemView.setOnClickListener {
            onProductClick?.invoke(searchProduct)
        }
    }

    var onProductClick: ((Product) -> Unit)? = null
}