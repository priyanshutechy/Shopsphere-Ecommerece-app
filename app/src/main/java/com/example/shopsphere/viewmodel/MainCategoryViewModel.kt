package com.example.shopsphere.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopsphere.data.Product
import com.example.shopsphere.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _specialProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val specialProduct: StateFlow<Resource<List<Product>>> = _specialProducts

    private val _bestDealsProducts =
        MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestDealsProduct: StateFlow<Resource<List<Product>>> = _bestDealsProducts

    private val _bestProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestProducts: StateFlow<Resource<List<Product>>> = _bestProducts

    private val specialProductPagingInfo = SpecialProductPagingInfo()
    private val bestDealsPagingInfo = BestDealsPagingInfo()
    private val bestProductPagingInfo = BestProductPagingInfo()

    init {
        fetchSpecialProducts()
        fetchBestDeals()
        fetchBestProducts()
    }

    fun fetchSpecialProducts() {
        if (!specialProductPagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _specialProducts.emit(Resource.Loading())
            }
            firestore.collection("Products").whereEqualTo("category", "Special Products")
                .limit(specialProductPagingInfo.page * 3).get()
                .addOnSuccessListener { result ->
                    val specialProductList = result.toObjects(Product::class.java)
                    specialProductPagingInfo.isPagingEnd =
                        specialProductList == specialProductPagingInfo.oldSpecialProducts
                    specialProductPagingInfo.oldSpecialProducts = specialProductList
                    viewModelScope.launch {
                        _specialProducts.emit(Resource.Success(specialProductList))
                    }
                    specialProductPagingInfo.page++
                }.addOnFailureListener {
                    viewModelScope.launch {
                        _specialProducts.emit(Resource.Error(it.message.toString()))
                    }
                }
        }
    }

    fun fetchBestDeals() {
        if (!bestDealsPagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _bestDealsProducts.emit(Resource.Loading())
            }
            firestore.collection("Products").whereEqualTo("category", "Best Deals")
                .limit(bestDealsPagingInfo.page * 3).get()
                .addOnSuccessListener { result ->
                    val bestDealsProducts = result.toObjects(Product::class.java)
                    bestDealsPagingInfo.isPagingEnd =
                        bestDealsProducts == bestDealsPagingInfo.oldBestDeals
                    bestDealsPagingInfo.oldBestDeals = bestDealsProducts
                    viewModelScope.launch {
                        _bestDealsProducts.emit(Resource.Success(bestDealsProducts))
                    }
                    bestDealsPagingInfo.page++
                }.addOnFailureListener {
                    viewModelScope.launch {
                        _bestDealsProducts.emit(Resource.Error(it.message.toString()))
                    }
                }
        }
    }

    fun fetchBestProducts() {
        if (!bestProductPagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _bestProducts.emit(Resource.Loading())
            }
            firestore.collection("Products").limit(bestProductPagingInfo.page * 10).get()
//            .whereEqualTo("category", "Best Products").get()
                .addOnSuccessListener { result ->
                    val bestProducts = result.toObjects(Product::class.java)
                    bestProductPagingInfo.isPagingEnd =
                        bestProducts == bestProductPagingInfo.oldBestProducts
                    bestProductPagingInfo.oldBestProducts = bestProducts
                    viewModelScope.launch {
                        _bestProducts.emit(Resource.Success(bestProducts))
                    }
                    bestProductPagingInfo.page++
                }.addOnFailureListener {
                    viewModelScope.launch {
                        _bestProducts.emit(Resource.Error(it.message.toString()))
                    }
                }
        }
    }
}

internal data class SpecialProductPagingInfo(
    var page: Long = 1,
    var oldSpecialProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false
)

internal data class BestDealsPagingInfo(
    var page: Long = 1,
    var oldBestDeals: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false
)

internal data class BestProductPagingInfo(
    var page: Long = 1,
    var oldBestProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false
)


