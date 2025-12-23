package com.example.shopsphere.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.shopsphere.databinding.ColourRvItemBinding

class ColoursAdapter : RecyclerView.Adapter<ColoursAdapter.ColoursViewHolder>() {

    private var selectedPosition = -1

    inner class ColoursViewHolder(private val binding: ColourRvItemBinding) :
        ViewHolder(binding.root) {
        fun bind(colour: Int, position: Int) {
            val imageDrawable = ColorDrawable(colour)
            binding.imageColour.setImageDrawable(imageDrawable)
            if (position == selectedPosition) { //Colour is selected
                binding.apply {
                    imageShadow.visibility = View.VISIBLE
                    imagePicked.visibility = View.VISIBLE
                }
            } else { //Colour is not selected
                binding.apply {
                    imageShadow.visibility = View.INVISIBLE
                    imagePicked.visibility = View.INVISIBLE
                }
            }
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColoursViewHolder {
        return ColoursViewHolder(
            ColourRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ColoursViewHolder, position: Int) {
        val colour = differ.currentList[position]
        holder.bind(colour, position)

        holder.itemView.setOnClickListener {
            if (selectedPosition >= 0)
                notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
            onItemClick?.invoke(colour)
        }
    }

    var onItemClick: ((Int) -> Unit)? = null

}