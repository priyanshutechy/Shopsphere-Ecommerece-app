package com.example.shopsphere.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.shopsphere.databinding.ViewpagerImageItemBinding

class ViewPager2ImagesAdapter :
    RecyclerView.Adapter<ViewPager2ImagesAdapter.ViewPager2ImagesViewHolder>() {
    inner class ViewPager2ImagesViewHolder(private val binding: ViewpagerImageItemBinding) :
        ViewHolder(binding.root) {
        fun bind(imagePath: String) {
            Glide.with(itemView).load(imagePath).into(binding.imageProductDetails)
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewPager2ImagesViewHolder {
        return ViewPager2ImagesViewHolder(
            ViewpagerImageItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewPager2ImagesViewHolder,
        position: Int
    ) {
        val image = differ.currentList[position]
        holder.bind(image)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}