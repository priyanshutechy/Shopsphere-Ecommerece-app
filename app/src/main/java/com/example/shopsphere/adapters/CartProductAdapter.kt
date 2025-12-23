package com.example.shopsphere.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopsphere.data.CartProduct
import com.example.shopsphere.databinding.CartProductItemBinding
import com.example.shopsphere.helper.getProductPrice

class CartProductAdapter : RecyclerView.Adapter<CartProductAdapter.CartProductViewHolder>() {
    inner class CartProductViewHolder(val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cartProduct: CartProduct) {
            binding.apply {
                Glide.with(itemView).load(cartProduct.product.images[0]).into(imageCartProduct)
                tvCartProductName.text = cartProduct.product.name
                tvCartProductQuantity.text = cartProduct.quantity.toString()

                val priceAfterPercentage =
                    cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price)
                tvCartProductPrice.text = "$ ${String.format("%.2f", priceAfterPercentage)}"

                imageCartProductColour.setImageDrawable(
                    ColorDrawable(
                        cartProduct.selectedColour ?: Color.TRANSPARENT
                    )
                )
                tvCartProductSize.text = cartProduct.selectedSize ?:""
                if (tvCartProductSize.text.isNullOrEmpty())
                    imageCartProductSize.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
                else
                    imageCartProductSize.setImageDrawable(ColorDrawable(Color.DKGRAY))

            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<CartProduct>() {
        //Called by the DiffUtil to decide whether two object represent the same Item.
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        //Called by the DiffUtil when it wants to check whether two items have the same data. DiffUtil uses this information to detect if the contents of an item has changed.
        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductViewHolder {
        return CartProductViewHolder(
            CartProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CartProductViewHolder, position: Int) {
        val cartProduct = differ.currentList[position]
        holder.bind(cartProduct)

        holder.itemView.setOnClickListener {
            onProductClick?.invoke(cartProduct)
        }

        holder.binding.imagePlus.setOnClickListener {
            onPlusClick?.invoke(cartProduct)
        }

        holder.binding.imageMinus.setOnClickListener {
            onMinusClick?.invoke(cartProduct)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onProductClick: ((CartProduct) -> Unit)? = null
    var onPlusClick: ((CartProduct) -> Unit)? = null
    var onMinusClick: ((CartProduct) -> Unit)? = null

}