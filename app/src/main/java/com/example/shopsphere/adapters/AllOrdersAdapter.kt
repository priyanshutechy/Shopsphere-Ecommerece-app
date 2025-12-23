package com.example.shopsphere.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.shopsphere.R
import com.example.shopsphere.data.order.Order
import com.example.shopsphere.data.order.OrderStatus
import com.example.shopsphere.data.order.getOrderStatus
import com.example.shopsphere.databinding.OrderItemBinding

class AllOrdersAdapter : Adapter<AllOrdersAdapter.AllOrdersViewHolder>() {
    inner class AllOrdersViewHolder(val binding: OrderItemBinding) : ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.apply {
                tvOrderId.text = order.orderId.toString()
                tvOrderDate.text = order.date
                tvOrderStatus.text = order.orderStatus
                val resources = itemView.resources

                val colourDrawable = when(getOrderStatus(order.orderStatus)){
                    is OrderStatus.Ordered -> ColorDrawable(resources.getColor(R.color.g_orange_yellow))
                    is OrderStatus.Confirmed -> ColorDrawable(resources.getColor(R.color.g_green))
                    is OrderStatus.Shipped -> ColorDrawable(resources.getColor(R.color.g_green))
                    is OrderStatus.Delivered -> ColorDrawable(resources.getColor(R.color.g_green))
                    is OrderStatus.Cancelled -> ColorDrawable(resources.getColor(R.color.g_red))
                    is OrderStatus.Returned -> ColorDrawable(resources.getColor(R.color.g_red))
                }

                imageOrderState.setImageDrawable(colourDrawable)
            }
        }

    }

    private val diffUtil = object : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.products == newItem.products
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllOrdersViewHolder {
        return AllOrdersViewHolder(
            OrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AllOrdersViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.bind(order)

        holder.itemView.setOnClickListener{
            onClick?.invoke(order)
        }

    }

    var onClick: ((Order) -> Unit)? = null

}