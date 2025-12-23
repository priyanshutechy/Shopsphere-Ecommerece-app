package com.example.shopsphere.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopsphere.data.Category
import com.example.shopsphere.data.Product
import com.example.shopsphere.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel constructor(
    private val firestore: FirebaseFirestore,
    private val category: Category
) : ViewModel() {

    private val _offerProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val offerProducts = _offerProducts.asStateFlow()

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProduct = _bestProducts.asStateFlow()

    private val categoryOfferProductPagingInfo = CategoryOfferProductPagingInfo()
    private val categoryBestProductPagingInfo = CategoryBestProductPagingInfo()

    init {
        fetchOfferProducts()
        fetchBestProducts()
    }

    fun fetchOfferProducts() {
        if (!categoryOfferProductPagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _offerProducts.emit(Resource.Loading())
            }
            firestore.collection("Products").whereEqualTo("category", category.category)
                .whereNotEqualTo("offerPercentage", 0f).limit(categoryOfferProductPagingInfo.page * 5)
                .get()
                .addOnSuccessListener {
                    val products = it.toObjects(Product::class.java)
                    categoryOfferProductPagingInfo.isPagingEnd =
                        products == categoryOfferProductPagingInfo.oldOfferProducts
                    categoryOfferProductPagingInfo.oldOfferProducts = products
                    viewModelScope.launch {
                        _offerProducts.emit(Resource.Success(products))
                    }
                    categoryOfferProductPagingInfo.page++
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _offerProducts.emit(Resource.Error(it.message.toString()))
                    }
                }
        }
    }

    fun fetchBestProducts() {
        if (!categoryBestProductPagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _bestProducts.emit(Resource.Loading())
            }
            firestore.collection("Products").whereEqualTo("category", category.category)
                .whereEqualTo("offerPercentage", 0f).limit(categoryBestProductPagingInfo.page * 5)
                .get()
                .addOnSuccessListener {
                    val products = it.toObjects(Product::class.java)
                    categoryBestProductPagingInfo.isPagingEnd =
                        products == categoryBestProductPagingInfo.oldBestProducts
                    categoryBestProductPagingInfo.oldBestProducts = products
                    viewModelScope.launch {
                        _bestProducts.emit(Resource.Success(products))
                    }
                    categoryBestProductPagingInfo.page++
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _bestProducts.emit(Resource.Error(it.message.toString()))
                    }
                }
        }
    }
}

internal data class CategoryOfferProductPagingInfo(
    var page: Long = 1,
    var oldOfferProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false
)

internal data class CategoryBestProductPagingInfo(
    var page: Long = 1,
    var oldBestProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false
)